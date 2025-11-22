package com.niraj.llmapihelper.api.dto;

import jakarta.validation.constraints.NotBlank;

public record ValidateSpecRequest(
        @NotBlank String openApiSpec, boolean autoRepair, String description, String targetModel) {}
