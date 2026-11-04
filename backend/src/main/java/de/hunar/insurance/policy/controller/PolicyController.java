package de.hunar.insurance.policy.controller;

import de.hunar.insurance.policy.application.PolicyService;
import de.hunar.insurance.policy.dto.IssuePolicyRequest;
import de.hunar.insurance.policy.dto.PolicyResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Exposes public policy read, issuance, and cancellation operations.
 */
@RestController
@RequestMapping("/api/policies")
@RequiredArgsConstructor
public class PolicyController {
    private final PolicyService service;

    /**
     * Lists policy responses for authenticated callers.
     *
     * @return all policies
     */
    @GetMapping
    public List<PolicyResponse> findAll() { return service.findAllResponses(); }

    /**
     * Issues a policy after validating the request and underwriting result.
     *
     * @param request policy issuance details
     * @return the issued policy
     */
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public PolicyResponse issue(@Valid @RequestBody IssuePolicyRequest request) { return service.issue(request); }
    /**
     * Retrieves a policy by identifier.
     *
     * @param id policy identifier
     * @return the requested policy
     */
    @GetMapping("/{id}")
    public PolicyResponse find(@PathVariable Long id) { return service.findByIdResponse(id); }
    /**
     * Cancels an active policy.
     *
     * @param id policy identifier
     * @return the cancelled policy
     */
    @PostMapping("/{id}/cancel")
    public PolicyResponse cancel(@PathVariable Long id) { return service.cancel(id); }
}
