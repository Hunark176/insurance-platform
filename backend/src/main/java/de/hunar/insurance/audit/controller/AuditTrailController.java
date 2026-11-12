package de.hunar.insurance.audit.controller;

import de.hunar.insurance.audit.application.AuditTrailService;
import de.hunar.insurance.audit.dto.AuditEntryResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes public, paginated audit history queries for the synthetic portfolio dataset.
 */
@RestController
@RequestMapping("/api/audit-trail")
@RequiredArgsConstructor
public class AuditTrailController {
    private final AuditTrailService service;

    /**
     * Returns the newest audit entries for a single entity.
     *
     * @param entityType entity type, normalized to uppercase
     * @param entityId entity identifier
     * @param page zero-based page index
     * @param size page size from 1 to 100
     * @return paginated audit entries ordered by occurrence time descending
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "basicAuth")
    public Page<AuditEntryResponse> findForEntity(
            @RequestParam @NotBlank String entityType,
            @RequestParam @NotBlank String entityId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "50") @Min(1) @Max(100) int size
    ) {
        return service.findForEntity(
                entityType.trim().toUpperCase(),
                entityId.trim(),
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "occurredAt"))
        );
    }
}
