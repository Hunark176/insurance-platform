package de.hunar.insurance.claim.controller;


// ← HIER die Swagger-Imports einfügen
import de.hunar.insurance.claim.entity.ClaimStatus;
import de.hunar.insurance.claim.dto.ClaimResponse;
import de.hunar.insurance.claim.dto.CreateClaimRequest;
import de.hunar.insurance.claim.service.ClaimService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/claims")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
@Tag(name = "Claims", description = "Schadenfälle verwalten")
public class ClaimController {

    private final ClaimService claimService;

    @GetMapping
    @Operation(summary = "Alle Claims laden")
    @ApiResponse(responseCode = "200", description = "Liste aller Claims erfolgreich geladen")
    @PreAuthorize("isAuthenticated()")
    public List<ClaimResponse> getAllClaims() {
        return claimService.getAllClaimResponses();
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