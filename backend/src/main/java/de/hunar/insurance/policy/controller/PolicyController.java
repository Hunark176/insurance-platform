package de.hunar.insurance.policy.controller;

import de.hunar.insurance.policy.application.PolicyService;
import de.hunar.insurance.policy.dto.IssuePolicyRequest;
import de.hunar.insurance.policy.dto.PolicyResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/policies")
@RequiredArgsConstructor
public class PolicyController {
    private final PolicyService service;
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('CLERK','ADMIN')")
    public PolicyResponse issue(@Valid @RequestBody IssuePolicyRequest request) { return service.issue(request); }
    @GetMapping("/{id}") @PreAuthorize("isAuthenticated()")
    public PolicyResponse find(@PathVariable Long id) { return service.findByIdResponse(id); }
    @PostMapping("/{id}/cancel") @PreAuthorize("hasAnyRole('CLERK','ADMIN')")
    public PolicyResponse cancel(@PathVariable Long id) { return service.cancel(id); }
}
