package com.niraj.llmapihelper.api.dto;

import java.util.List;

public record GenerateSpecResponse(
        String openApiSpec, boolean valid, boolean repaired, List<String> validationErrors, String modelUsed) {}
