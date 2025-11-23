package com.niraj.llmapihelper.service;

import com.niraj.llmapihelper.api.dto.GenerateSpecRequest;
import com.niraj.llmapihelper.api.dto.GenerateSpecResponse;
import com.niraj.llmapihelper.api.dto.PolicySuggestionRequest;
import com.niraj.llmapihelper.api.dto.PolicySuggestionResponse;
import com.niraj.llmapihelper.api.dto.ValidateSpecRequest;
import com.niraj.llmapihelper.api.dto.ValidateSpecResponse;
import com.niraj.llmapihelper.config.OpenAiProperties;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Service
public class OpenApiSpecificationService {

    private final OpenAiGatewayClient openAiGatewayClient;
    private final OpenApiValidationService validationService;
    private final OpenApiPromptBuilder promptBuilder;
    private final OpenAiProperties properties;

    public OpenApiSpecificationService(
            OpenAiGatewayClient openAiGatewayClient,
            OpenApiValidationService validationService,
            OpenApiPromptBuilder promptBuilder,
            OpenAiProperties properties) {
        this.openAiGatewayClient = openAiGatewayClient;
        this.validationService = validationService;
        this.promptBuilder = promptBuilder;
        this.properties = properties;
    }

    public GenerateSpecResponse generateSpecification(GenerateSpecRequest request) {
        String prompt = promptBuilder.buildGenerationPrompt(request);
        String modelUsed = resolveModel(request.targetModel());
        String initialSpec = openAiGatewayClient.generateSpecYaml(prompt, request.targetModel());
        SpecValidationResult validation = validationService.validate(initialSpec);
        boolean repaired = false;
        String workingSpec = initialSpec;

        if (!validation.valid() && request.autoRepair()) {
            String summary = summarizeValidation(validation.messages());
            workingSpec =
                    openAiGatewayClient.repairSpecYaml(
                            initialSpec, summary, request.description(), request.targetModel());
            validation = validationService.validate(workingSpec);
            repaired = true;
        }

        return new GenerateSpecResponse(
                workingSpec,
                validation.valid(),
                repaired && validation.valid(),
                validation.messages(),
                modelUsed);
    }

    public ValidateSpecResponse validateSpecification(ValidateSpecRequest request) {
        SpecValidationResult validation = validationService.validate(request.openApiSpec());
        boolean repaired = false;
        String workingSpec = request.openApiSpec();

        if (!validation.valid() && request.autoRepair()) {
            workingSpec =
                    openAiGatewayClient.repairSpecYaml(
                            request.openApiSpec(),
                            summarizeValidation(validation.messages()),
                            request.description(),
                            request.targetModel());
            validation = validationService.validate(workingSpec);
            repaired = true;
        }

        return new ValidateSpecResponse(
                validation.valid(), repaired && validation.valid(), validation.messages(), workingSpec);
    }

    public PolicySuggestionResponse suggestPolicies(PolicySuggestionRequest request) {
        String gatewayTargets =
                CollectionUtils.isEmpty(request.gateways())
                        ? "Apigee, Spring Cloud Gateway, Kong"
                        : String.join(", ", request.gateways());
        String rawSuggestions =
                openAiGatewayClient.suggestGatewayPolicies(
                        request.openApiSpec(), gatewayTargets, request.targetModel());
        Map<String, String> parsed = parseGatewaySections(rawSuggestions);
        if (parsed.isEmpty()) {
            parsed.put("general", rawSuggestions);
        }
        return new PolicySuggestionResponse(parsed, resolveModel(request.targetModel()));
    }

    private String resolveModel(String override) {
        return StringUtils.hasText(override) ? override : properties.getDefaultModel();
    }

    private String summarizeValidation(List<String> errors) {
        if (errors == null || errors.isEmpty()) {
            return "Specification failed to parse, no parser messages were returned.";
        }
        StringJoiner joiner = new StringJoiner("; ");
        errors.forEach(joiner::add);
        return joiner.toString();
    }

    private Map<String, String> parseGatewaySections(String markdown) {
        Map<String, String> sections = new LinkedHashMap<>();
        String currentKey = "general";
        StringBuilder builder = new StringBuilder();
        for (String line : markdown.split("\\r?\\n")) {
            if (line.startsWith("## ")) {
                String previous = builder.toString().trim();
                if (StringUtils.hasText(previous)) {
                    sections.put(currentKey, previous);
                }
                builder = new StringBuilder();
                currentKey = line.substring(3).trim();
            } else {
                builder.append(line).append(System.lineSeparator());
            }
        }
        String last = builder.toString().trim();
        if (StringUtils.hasText(last)) {
            sections.put(currentKey, last);
        }
        return sections;
    }
}
