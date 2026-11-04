package de.hunar.insurance.underwriting.controller;

import de.hunar.insurance.underwriting.application.UnderwritingService;
import de.hunar.insurance.underwriting.dto.UnderwritingCheckRequest;
import de.hunar.insurance.underwriting.dto.UnderwritingCheckResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
    @PostMapping("/check")
    public UnderwritingCheckResponse check(@Valid @RequestBody UnderwritingCheckRequest request) {
        return service.check(request);
    }
}
