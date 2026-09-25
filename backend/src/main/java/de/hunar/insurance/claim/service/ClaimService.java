package de.hunar.insurance.claim.service;

import de.hunar.insurance.claim.entity.Claim;
import de.hunar.insurance.claim.entity.ClaimStatus;
import de.hunar.insurance.claim.repository.ClaimRepository;
import de.hunar.insurance.claim.events.ClaimApprovedEvent;
import de.hunar.insurance.shared.domain.Money;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ClaimService {

    private final ClaimRepository claimRepository;
    private final ApplicationEventPublisher eventPublisher;

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
        Claim saved = claimRepository.save(claim);
        if (newStatus == ClaimStatus.APPROVED) {
            eventPublisher.publishEvent(new ClaimApprovedEvent(
                    saved.getId(),
                    saved.getPolicyId(),
                    Money.eur(saved.getAmount())
            ));
        }
        return saved;
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
