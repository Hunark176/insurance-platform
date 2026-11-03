package de.hunar.insurance.billing.application;

import de.hunar.insurance.audit.AuditTrailApi;
import de.hunar.insurance.billing.domain.PayoutRepository;
import de.hunar.insurance.billing.dto.PayoutResponse;
import de.hunar.insurance.billing.mapper.PayoutMapper;
import de.hunar.insurance.shared.web.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Provides billing read operations and controlled payout status transitions.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BillingService {
    private final PayoutRepository payoutRepository;
    private final PayoutMapper mapper;
    private final AuditTrailApi auditTrail;

    /**
     * Lists payouts for staff billing workflows.
     *
     * @return all payout responses
     */
    @Transactional(readOnly = true)
    public List<PayoutResponse> findAll() {
        return payoutRepository.findAll().stream().map(mapper::toResponse).toList();
    }

    /**
     * Marks a newly created payout as executed and records the status transition.
     *
     * @param id payout identifier
     * @return the updated payout response
     * @throws ResourceNotFoundException if the payout does not exist
     * @throws IllegalStateException if the payout is not in the {@code CREATED} state
     */
    @Transactional
    public PayoutResponse markPaid(Long id) {
        var payout = payoutRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payout", id));
        var previousStatus = payout.getStatus();
        payout.markPaid();
        var paidPayout = payoutRepository.save(payout);
        auditTrail.record("STATUS_CHANGED", "PAYOUT", id.toString(),
                "from=" + previousStatus + ",to=" + paidPayout.getStatus(), auditTrail.captureContext());
        log.info("Payout status changed: payoutId={} claimId={} fromStatus={} toStatus={}",
                id, paidPayout.getClaimId(), previousStatus, paidPayout.getStatus());
        return mapper.toResponse(paidPayout);
    }
}
