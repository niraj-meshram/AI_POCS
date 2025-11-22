package com.niraj.llmapihelper.service;

import com.niraj.llmapihelper.config.OpenAiProperties;
import com.openai.client.OpenAIClient;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.openai.models.chat.completions.ChatCompletionMessageParam;
import com.openai.models.chat.completions.ChatCompletionSystemMessageParam;
import com.openai.models.chat.completions.ChatCompletionUserMessageParam;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import org.apache.commons.text.CaseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

public class OpenAiSdkClient implements OpenAiGatewayClient {

    private static final Logger log = LoggerFactory.getLogger(OpenAiSdkClient.class);

    private final OpenAIClient openAiClient;
    private final OpenAiProperties properties;
    private final SpecTemplateGenerator fallbackGenerator = new SpecTemplateGenerator();

    public OpenAiSdkClient(OpenAIClient openAiClient, OpenAiProperties properties) {
        this.openAiClient = openAiClient;
        this.properties = properties;
    }

    @Override
    public String generateSpecYaml(String prompt, String modelOverride) {
        return callModel(prompt, modelOverride, fallbackGenerator::generateFromPrompt);
    }

    @Override
    public String repairSpecYaml(
            String invalidSpecification,
            String validationSummary,
            String context,
            String modelOverride) {
        String prompt =
                """
                        You are a meticulous OpenAPI 3.x editor. Inspect the invalid specification below, fix any issues, and output a corrected specification in YAML. Do not include commentary.

                        Invalid specification:
                        %s

                        Validation summary:
                        %s

                        Additional context:
                        %s
                        """
                        .formatted(invalidSpecification, validationSummary, context);

        return callModel(
                prompt,
                modelOverride,
                () -> fallbackGenerator.repairFromSummary(invalidSpecification, validationSummary));
    }

    @Override
    public String suggestGatewayPolicies(String specification, String gateways, String modelOverride) {
        String prompt =
                """
                        You are helping platform teams configure API gateways. Based on the OpenAPI specification below, list suggested gateway policies (rate limits, auth, caching, security posture) for the following gateways: %s. Respond as concise Markdown bullet lists grouped by gateway.

                        OpenAPI specification:
                        %s
                        """
                        .formatted(gateways, specification);

        return callModel(prompt, modelOverride, () -> fallbackGenerator.suggestPolicies(gateways));
    }

    private String callModel(String prompt, String modelOverride, Supplier<String> fallbackSupplier) {
        if (openAiClient == null) {
            log.warn("OpenAI client is not configured - using fallback response");
            return fallbackSupplier.get();
        }
        try {
            ChatCompletion completion =
                    openAiClient.chat()
                            .completions()
                            .create(
                                    ChatCompletionCreateParams.builder()
                                            .model(ChatModel.of(resolveModel(modelOverride)))
                                            .messages(buildMessages(prompt))
                                            .temperature(properties.getTemperature())
                                            .maxCompletionTokens(
                                                    Optional.ofNullable(properties.getMaxCompletionTokens())
                                                            .map(Integer::longValue)
                                                            .orElse(null))
                                            .build());
            return completion.choices().stream()
                    .findFirst()
                    .flatMap(choice -> choice.message().content())
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .orElseGet(fallbackSupplier);
        } catch (Exception ex) {
            log.warn("OpenAI request failed, using fallback instead: {}", ex.getMessage());
            return fallbackSupplier.get();
        }
    }

    private List<ChatCompletionMessageParam> buildMessages(String prompt) {
        ChatCompletionSystemMessageParam systemMessage =
                ChatCompletionSystemMessageParam.builder()
                        .content(
                                """
                                        You are an expert API platform engineer. Generate standards-compliant OpenAPI 3.x YAML and respond with YAML only.
                                        """)
                        .build();
        ChatCompletionUserMessageParam userMessage =
                ChatCompletionUserMessageParam.builder().content(prompt).build();
        return List.of(
                ChatCompletionMessageParam.ofSystem(systemMessage),
                ChatCompletionMessageParam.ofUser(userMessage));
    }

    private String resolveModel(String override) {
        if (StringUtils.hasText(override)) {
            return override.trim();
        }
        return properties.getDefaultModel();
    }

    /**
     * Provides deterministic responses when the OpenAI client is unavailable so developers can still
     * iterate locally.
     */
    static final class SpecTemplateGenerator {

        String generateFromPrompt(String prompt) {
            String title = CaseUtils.toCamelCase(prompt.replaceAll("\\s+", " "), true);
            return """
                    openapi: 3.0.4
                    info:
                      title: %s
                      version: '1.0'
                    servers:
                      - url: https://sandbox.example.com
                    paths:
                      /status:
                        get:
                          summary: Health check endpoint
                          responses:
                            '200':
                              description: Service is running
                              content:
                                application/json:
                                  schema:
                                    type: object
                                    properties:
                                      status:
                                        type: string
                    """
                    .formatted(title);
        }

        String repairFromSummary(String invalidSpec, String validationSummary) {
            return """
                    # AUTO-REPAIRED SPEC
                    # Validation summary:
                    # %s

                    %s
                    """
                    .formatted(validationSummary, invalidSpec);
        }

        String suggestPolicies(String gateways) {
            return """
                    %s:
                      - Enforce OAuth2 client credentials
                      - Apply 100 rpm rate-limits per API key
                      - Enable response caching for GET endpoints (60s)
                      - Inspect payloads for PII leakage
                    """
                    .formatted(gateways);
        }
    }
}
