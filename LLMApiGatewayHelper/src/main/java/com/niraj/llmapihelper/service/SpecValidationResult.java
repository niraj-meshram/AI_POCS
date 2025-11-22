package com.niraj.llmapihelper.service;

import java.util.List;

public record SpecValidationResult(boolean valid, List<String> messages) {}
