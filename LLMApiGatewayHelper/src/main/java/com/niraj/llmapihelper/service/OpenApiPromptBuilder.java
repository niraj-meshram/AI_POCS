package com.niraj.llmapihelper.service;

import com.niraj.llmapihelper.api.dto.GenerateSpecRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class OpenApiPromptBuilder {

    public String buildGenerationPrompt(GenerateSpecRequest request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append(
                """
                        Translate the following natural language description into an OpenAPI 3.x document expressed strictly in YAML. The YAML must include info, servers, and at least one path with operations and schemas when applicable. Respond with YAML only.

                        Description:
                        """);
        prompt.append(request.description().trim()).append("\n\n");
        if (StringUtils.hasText(request.domainContext())) {
            prompt.append("Domain context:\n").append(request.domainContext().trim()).append("\n\n");
        }
        prompt.append("Quality checklist: ensure proper schemas, HTTP response codes, reusable components when helpful.");
        return prompt.toString();
    }
}
