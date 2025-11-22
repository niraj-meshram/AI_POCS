package com.niraj.llmapihelper.api.dto;

import java.util.Map;

public record PolicySuggestionResponse(Map<String, String> suggestions, String modelUsed) {}
