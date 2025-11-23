package com.niraj.llmapihelper.service;

import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class OpenApiValidationService {

    private final OpenAPIV3Parser parser = new OpenAPIV3Parser();

    public SpecValidationResult validate(String specContent) {
        if (!StringUtils.hasText(specContent)) {
            return new SpecValidationResult(false, List.of("Specification content cannot be empty"));
        }
        ParseOptions parseOptions = new ParseOptions();
        parseOptions.setResolve(true);
        parseOptions.setResolveFully(true);
        SwaggerParseResult result = parser.readContents(specContent, null, parseOptions);
        List<String> messages =
                result.getMessages() == null ? Collections.emptyList() : result.getMessages();
        boolean hasErrors = messages.stream().anyMatch(msg -> msg.toLowerCase().contains("error"));
        boolean valid = result.getOpenAPI() != null && !hasErrors;
        return new SpecValidationResult(valid, messages);
    }
}
