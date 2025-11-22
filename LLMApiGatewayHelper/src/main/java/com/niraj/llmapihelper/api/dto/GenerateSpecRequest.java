package com.niraj.llmapihelper.api.dto;

import jakarta.validation.constraints.NotBlank;

public record GenerateSpecRequest(
        @NotBlank String description, String domainContext, boolean autoRepair, String targetModel) {}
