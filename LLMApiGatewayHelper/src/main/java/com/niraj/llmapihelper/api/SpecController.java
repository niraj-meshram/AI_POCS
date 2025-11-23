package com.niraj.llmapihelper.api;

import com.niraj.llmapihelper.api.dto.GenerateSpecRequest;
import com.niraj.llmapihelper.api.dto.GenerateSpecResponse;
import com.niraj.llmapihelper.api.dto.PolicySuggestionRequest;
import com.niraj.llmapihelper.api.dto.PolicySuggestionResponse;
import com.niraj.llmapihelper.api.dto.ValidateSpecRequest;
import com.niraj.llmapihelper.api.dto.ValidateSpecResponse;
import com.niraj.llmapihelper.service.OpenApiSpecificationService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/specs", produces = MediaType.APPLICATION_JSON_VALUE)
public class SpecController {

    private final OpenApiSpecificationService specificationService;

    public SpecController(OpenApiSpecificationService specificationService) {
        this.specificationService = specificationService;
    }

    @PostMapping(path = "/generate", consumes = MediaType.APPLICATION_JSON_VALUE)
    public GenerateSpecResponse generate(@Valid @RequestBody GenerateSpecRequest request) {
        return specificationService.generateSpecification(request);
    }

    @PostMapping(path = "/validate", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ValidateSpecResponse validate(@Valid @RequestBody ValidateSpecRequest request) {
        return specificationService.validateSpecification(request);
    }

    @PostMapping(path = "/policies", consumes = MediaType.APPLICATION_JSON_VALUE)
    public PolicySuggestionResponse suggestPolicies(
            @Valid @RequestBody PolicySuggestionRequest request) {
        return specificationService.suggestPolicies(request);
    }
}
