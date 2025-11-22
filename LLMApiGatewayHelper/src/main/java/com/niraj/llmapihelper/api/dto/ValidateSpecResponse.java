package com.niraj.llmapihelper.api.dto;

import java.util.List;

public record ValidateSpecResponse(
        boolean valid, boolean repaired, List<String> validationErrors, String repairedSpec) {}
