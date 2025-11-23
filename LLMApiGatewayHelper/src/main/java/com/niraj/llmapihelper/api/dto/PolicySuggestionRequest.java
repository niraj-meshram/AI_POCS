package com.niraj.llmapihelper.api.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record PolicySuggestionRequest(
        @NotBlank String openApiSpec, List<String> gateways, String targetModel) {}
