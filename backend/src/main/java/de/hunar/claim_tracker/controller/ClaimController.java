package de.hunar.claim_tracker.controller;

import de.hunar.claim_tracker.entity.Claim;
import de.hunar.claim_tracker.entity.ClaimStatus;
import de.hunar.claim_tracker.service.ClaimService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/claims")
@CrossOrigin(origins = "http://localhost:5173") // Erlaubt Zugriff vom React-Vite-Server
@RequiredArgsConstructor
public class ClaimController {

    private final ClaimService claimService;


    @GetMapping
    public List<Claim> getAllClaims() {
        return claimService.getAllClaims();
    }

    // GET /api/claims/1
    @GetMapping("/{id}")
    public Claim getClaim(@PathVariable Long id) {
        return claimService.getClaimById(id);
    }

    // POST /api/claims
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Claim createClaim(@Valid @RequestBody Claim claim) {
        return claimService.createClaim(claim);
    }

    // PATCH /api/claims/1/status
    @PatchMapping("/{id}/status")
    public Claim updateStatus(@PathVariable Long id, @RequestParam ClaimStatus newStatus) {
        return claimService.updateClaim(id, newStatus);
    }

    // DELETE /api/claims/1
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteClaim(@PathVariable Long id) {
        claimService.deleteClaim(id);
    }
}