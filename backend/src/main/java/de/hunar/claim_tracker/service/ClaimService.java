package de.hunar.claim_tracker.service;

import de.hunar.claim_tracker.entity.Claim;
import de.hunar.claim_tracker.entity.ClaimStatus;
import de.hunar.claim_tracker.repository.ClaimRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ClaimService {

    private final ClaimRepository claimRepository;

    @Transactional(readOnly = true)
    public List<Claim> getAllClaims() {
        return claimRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Claim getClaimById(Long id) {
        return claimRepository.findById(id).
                orElseThrow(() -> new IllegalArgumentException("Claim not found: " + id));
    }
    public Claim createClaim(Claim claim) {
        claim.setStatus(ClaimStatus.RECEIVED);
        return claimRepository.save(claim);
    }

    public Claim updateClaim(Long id, ClaimStatus newStatus) {
        Claim claim = getClaimById(id);
        validateStatusTransition(claim.getStatus(), newStatus);
        claim.setStatus(newStatus);
        return claimRepository.save(claim);
    }

    public void deleteClaim(Long id) {
        if (!claimRepository.existsById(id)) {
            throw new IllegalArgumentException("Claim not found: " + id);
        }
        claimRepository.deleteById(id);
    }

    private void validateStatusTransition(ClaimStatus current, ClaimStatus target) {
        boolean valid = switch (current) {
            case RECEIVED -> target == ClaimStatus.IN_REVIEW;
            case IN_REVIEW -> target == ClaimStatus.APPROVED || target == ClaimStatus.REJECTED;
            case APPROVED, REJECTED -> false;
        };
        if (!valid) {
            throw new IllegalStateException(
                    "Ungültiger Status-Übergang: " + current + " → " + target
            );
        }
    }

    public Claim saveClaim(Claim claim) {
        return claimRepository.save(claim);
    }

}
