package com.niraj.llmapihelper.service;

public interface OpenAiGatewayClient {

    String generateSpecYaml(String prompt, String modelOverride);

    String repairSpecYaml(
            String invalidSpecification, String validationSummary, String context, String modelOverride);

    String suggestGatewayPolicies(String specification, String gateways, String modelOverride);
}
