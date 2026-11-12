package de.hunar.insurance.claim.controller;

import de.hunar.insurance.claim.dto.ClaimPageResponse;
import de.hunar.insurance.claim.dto.ClaimResponse;
import de.hunar.insurance.claim.dto.CreateClaimRequest;
import de.hunar.insurance.claim.application.ClaimService;
import de.hunar.insurance.claim.domain.ClaimStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

/**
 * Exposes public claim operations; deletion is disabled by the security filter chain.
 */
@RestController
@RequestMapping("/api/claims")
@RequiredArgsConstructor
@Tag(name = "Claims", description = "Schadenfälle verwalten")
public class ClaimController {

    private final ClaimService claimService;

    /**
     * Returns claims ordered by creation time and identifier, with optional policy filtering.
     *
     * @param page zero-based page index
     * @param size page size from 1 to 100
     * @param policyId optional policy identifier
     * @return the requested page of claims
     */
    @GetMapping
    @Operation(summary = "Paginierte Claim-Übersicht laden")
    @ApiResponse(responseCode = "200", description = "Paginierte Claim-Übersicht erfolgreich geladen")
    public ClaimPageResponse getAllClaims(
            @Parameter(description = "0-basierte Seitennummer", example = "0")
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Anzahl der Claims pro Seite (maximal 100)", example = "20")
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @Parameter(description = "Optionaler Filter für die zugehörige Police", example = "12345")
            @RequestParam(required = false) @Positive Long policyId
    ) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.DESC, "id"));
        return claimService.getClaimResponses(PageRequest.of(page, size, sort), policyId);
    }

    /**
     * Retrieves a claim and its policy details.
     *
     * @param id claim identifier
     * @return claim and associated policy details
     */
    @GetMapping("/{id}")
    @Operation(summary = "Claim nach ID suchen")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Claim gefunden"),
            @ApiResponse(responseCode = "404", description = "Claim nicht gefunden")
    })
    public ClaimResponse getClaim(
            @Parameter(description = "ID des Claims", example = "1")
            @PathVariable Long id
    ) {
        return claimService.getClaimResponse(id);
    }

    /**
     * Reports a claim after validating the submitted request.
     *
     * @param request claim details
     * @return the created claim
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('CUSTOMER', 'CLERK', 'ADMIN')")
    @SecurityRequirement(name = "basicAuth")
    @Operation(summary = "Neuen Claim erstellen")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Claim erfolgreich erstellt"),
            @ApiResponse(responseCode = "401", description = "Anmeldung erforderlich")
    })
    @ResponseStatus(HttpStatus.CREATED)
    public ClaimResponse createClaim(
            @Parameter(description = "Claim-Daten zum Erstellen")
            @Valid @RequestBody CreateClaimRequest request
    ) {
        return claimService.createClaimResponse(request);
    }

    /**
     * Moves a claim to the requested next lifecycle status.
     *
     * @param id claim identifier
     * @param newStatus requested status
     * @return the updated claim
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('CLERK', 'ADMIN')")
    @SecurityRequirement(name = "basicAuth")
    @Operation(summary = "Status eines Claims aktualisieren (CLERK, ADMIN)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status erfolgreich aktualisiert"),
            @ApiResponse(responseCode = "401", description = "Anmeldung erforderlich"),
            @ApiResponse(responseCode = "403", description = "Rolle CLERK oder ADMIN erforderlich"),
            @ApiResponse(responseCode = "404", description = "Claim nicht gefunden")
    })
    public ClaimResponse updateStatus(
            @Parameter(description = "ID des Claims", example = "1")
            @PathVariable Long id,
            @Parameter(description = "Neuer Status", example = "IN_REVIEW")
            @RequestParam ClaimStatus newStatus
    ) {
        return claimService.updateClaimResponse(id, newStatus);
    }

    /**
     * Deletes a claim and records the deletion in the audit trail.
     *
     * @param id claim identifier
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Claim löschen")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Claim erfolgreich gelöscht"),
            @ApiResponse(responseCode = "404", description = "Claim nicht gefunden")
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteClaim(
            @Parameter(description = "ID des Claims", example = "1")
            @PathVariable Long id
    ) {
        claimService.deleteClaim(id);
    }
}