package de.hunar.insurance.underwriting.controller;

import de.hunar.insurance.underwriting.application.UnderwritingService;
import de.hunar.insurance.underwriting.dto.UnderwritingCheckRequest;
import de.hunar.insurance.underwriting.dto.UnderwritingCheckResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Exposes the public policy eligibility check.
 */
@RestController
@RequestMapping("/api/underwriting")
@RequiredArgsConstructor
public class UnderwritingController {
    private final UnderwritingService service;

    /**
     * Evaluates whether a customer and product combination is eligible for policy issuance.
     *
     * @param request customer and product identifiers
     * @return eligibility outcome and explanatory reason
     */
    @PreAuthorize("hasAnyRole('CLERK', 'ADMIN')")
    @SecurityRequirement(name = "basicAuth")
    @PostMapping("/check")
    public UnderwritingCheckResponse check(@Valid @RequestBody UnderwritingCheckRequest request) {
        return service.check(request);
    }
}
