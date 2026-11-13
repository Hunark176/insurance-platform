package de.hunar.insurance.policy.controller;

import de.hunar.insurance.policy.application.PolicyService;
import de.hunar.insurance.policy.dto.IssuePolicyRequest;
import de.hunar.insurance.policy.dto.PolicyResponse;
import de.hunar.insurance.shared.web.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * Exposes public policy read, issuance, and cancellation operations.
 */
@RestController
@RequestMapping("/api/policies")
@RequiredArgsConstructor
public class PolicyController {
    private final PolicyService service;

    /**
     * Lists policies page by page, ordered by identifier.
     *
     * @param page zero-based page index
     * @param size page size from 1 to 100
     * @return the requested page of policies
     */
    @GetMapping
    public PageResponse<PolicyResponse> findAll(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return service.findPage(PageRequest.of(page, size, Sort.by("id")));
    }

    /**
     * Issues a policy after validating the request and underwriting result.
     *
     * @param request policy issuance details
     * @return the issued policy
     */
    @PreAuthorize("hasAnyRole('CLERK', 'ADMIN')")
    @SecurityRequirement(name = "basicAuth")
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
     * Cancels an active policy; losses up to the effective date stay covered.
     *
     * @param id policy identifier
     * @param effectiveOn last covered day (ISO date); defaults to today
     * @return the cancelled policy
     */
    @PreAuthorize("hasAnyRole('CLERK', 'ADMIN')")
    @SecurityRequirement(name = "basicAuth")
    @Operation(summary = "Police stornieren (CLERK, ADMIN)",
            description = "Schäden bis einschließlich zum Wirksamkeitsdatum bleiben gedeckt und können "
                    + "weiterhin gemeldet und reguliert werden. Rückwirkende Stornos sind nicht möglich.")
    @PostMapping("/{id}/cancel")
    public PolicyResponse cancel(
            @PathVariable Long id,
            @Parameter(description = "Letzter gedeckter Tag (Standard: heute)", example = "2026-12-31")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate effectiveOn
    ) {
        return service.cancel(id, effectiveOn);
    }
}
