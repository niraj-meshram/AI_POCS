package com.niraj.llmapihelper.config;

import com.niraj.llmapihelper.service.OpenAiGatewayClient;
import com.niraj.llmapihelper.service.OpenAiSdkClient;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
@EnableConfigurationProperties(OpenAiProperties.class)
public class OpenAiClientConfiguration {

    @Bean
    @ConditionalOnClass(OpenAIOkHttpClient.class)
    @ConditionalOnProperty(prefix = "openai", name = "api-key")
    @ConditionalOnMissingBean(OpenAIClient.class)
    public OpenAIClient openAiClient(OpenAiProperties properties) {
        OpenAIOkHttpClient.Builder builder =
                OpenAIOkHttpClient.builder()
                        .apiKey(properties.getApiKey())
                        .maxRetries(properties.getMaxRetries());

        if (properties.getRequestTimeoutSeconds() != null) {
            builder.timeout(properties.getRequestTimeoutSeconds());
        }
        if (StringUtils.hasText(properties.getBaseUrl())) {
            builder.baseUrl(properties.getBaseUrl());
        }
        if (StringUtils.hasText(properties.getOrganizationId())) {
            builder.organization(properties.getOrganizationId());
        }
        return builder.build();
    }

    @Bean
    @ConditionalOnMissingBean(OpenAiGatewayClient.class)
    public OpenAiGatewayClient openAiGatewayClient(
            ObjectProvider<OpenAIClient> openAiClientProvider, OpenAiProperties properties) {
        return new OpenAiSdkClient(openAiClientProvider.getIfAvailable(), properties);
    }
}
