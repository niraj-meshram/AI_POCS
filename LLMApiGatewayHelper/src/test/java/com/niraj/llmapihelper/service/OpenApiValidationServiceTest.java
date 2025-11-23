package com.niraj.llmapihelper.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class OpenApiValidationServiceTest {

    private final OpenApiValidationService service = new OpenApiValidationService();

    @Test
    void validSpecificationPassesValidation() {
        String yaml =
                """
                        openapi: 3.0.1
                        info:
                          title: Sample API
                          version: '1.0'
                        paths:
                          /hello:
                            get:
                              responses:
                                '200':
                                  description: ok
                        """;

        SpecValidationResult result = service.validate(yaml);

        assertThat(result.valid()).isTrue();
        assertThat(result.messages()).isEmpty();
    }

    @Test
    void invalidSpecificationReturnsMessages() {
        String yaml =
                """
                        openapi: 3.0.1
                        info:
                          title: Broken API
                          version: '1.0'
                        paths:
                          /hello:
                            get:
                              responses:
                                '200':
                                  description:
                        """;

        SpecValidationResult result = service.validate(yaml);

        assertThat(result.valid()).isFalse();
        assertThat(result.messages()).isNotEmpty();
    }
}
