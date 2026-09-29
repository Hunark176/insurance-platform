package de.hunar.insurance.claim.controller;

import de.hunar.insurance.claim.dto.ClaimPageResponse;
import de.hunar.insurance.claim.dto.ClaimResponse;
import de.hunar.insurance.claim.dto.CreateClaimRequest;
import de.hunar.insurance.claim.entity.ClaimStatus;
import de.hunar.insurance.claim.service.ClaimService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/claims")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
@Tag(name = "Claims", description = "Schadenfälle verwalten")
public class ClaimController {

    private final ClaimService claimService;

    @GetMapping
    @Operation(summary = "Paginierte Claim-Übersicht laden")
    @ApiResponse(responseCode = "200", description = "Paginierte Claim-Übersicht erfolgreich geladen")
    @PreAuthorize("isAuthenticated()")
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

    @GetMapping("/{id}")
    @Operation(summary = "Claim nach ID suchen")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Claim gefunden"),
            @ApiResponse(responseCode = "404", description = "Claim nicht gefunden")
    })
    @PreAuthorize("isAuthenticated()")
    public ClaimResponse getClaim(
            @Parameter(description = "ID des Claims", example = "1")
            @PathVariable Long id
    ) {
        return claimService.getClaimResponse(id);
    }

    @PostMapping
    @Operation(summary = "Neuen Claim erstellen")
    @ApiResponse(responseCode = "201", description = "Claim erfolgreich erstellt")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('CUSTOMER','CLERK','ADMIN')")
    public ClaimResponse createClaim(
            @Parameter(description = "Claim-Daten zum Erstellen")
            @Valid @RequestBody CreateClaimRequest request
    ) {
        return claimService.createClaimResponse(request);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Status eines Claims aktualisieren")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status erfolgreich aktualisiert"),
            @ApiResponse(responseCode = "404", description = "Claim nicht gefunden")
    })
    @PreAuthorize("hasAnyRole('CLERK','ADMIN')")
    public ClaimResponse updateStatus(
            @Parameter(description = "ID des Claims", example = "1")
            @PathVariable Long id,
            @Parameter(description = "Neuer Status", example = "IN_PROGRESS")
            @RequestParam ClaimStatus newStatus
    ) {
        return claimService.updateClaimResponse(id, newStatus);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Claim löschen")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Claim erfolgreich gelöscht"),
            @ApiResponse(responseCode = "404", description = "Claim nicht gefunden")
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('CLERK','ADMIN')")
    public void deleteClaim(
            @Parameter(description = "ID des Claims", example = "1")
            @PathVariable Long id
    ) {
        claimService.deleteClaim(id);
    }
}