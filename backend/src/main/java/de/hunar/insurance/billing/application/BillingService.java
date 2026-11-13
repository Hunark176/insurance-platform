package de.hunar.insurance.billing.application;

import de.hunar.insurance.billing.domain.CreditNote;
import de.hunar.insurance.billing.domain.Payout;
import de.hunar.insurance.shared.web.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import de.hunar.insurance.audit.AuditTrailApi;
import de.hunar.insurance.billing.domain.CreditNoteRepository;
import de.hunar.insurance.billing.domain.PayoutRepository;
import de.hunar.insurance.billing.dto.CreditNoteResponse;
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
    private final CreditNoteRepository creditNoteRepository;
    private final PayoutMapper mapper;
    private final AuditTrailApi auditTrail;

    /**
     * Lists one page of payouts.
     *
     * @param pageable requested page and sort order
     * @return the requested page
     */
    @Transactional(readOnly = true)
    public PageResponse<PayoutResponse> findPayouts(Pageable pageable) {
        Page<Payout> page = payoutRepository.findAll(pageable);
        return PageResponse.of(page, page.getContent().stream().map(mapper::toResponse).toList());
    }

    /**
     * Lists one page of credit notes created after policy cancellations.
     *
     * @param pageable requested page and sort order
     * @return the requested page
     */
    @Transactional(readOnly = true)
    public PageResponse<CreditNoteResponse> findCreditNotes(Pageable pageable) {
        Page<CreditNote> page = creditNoteRepository.findAll(pageable);
        return PageResponse.of(page, page.getContent().stream()
                .map(note -> new CreditNoteResponse(note.getId(), note.getPolicyId(), note.getCustomerId(),
                        note.getAmount(), note.getCurrency(), note.getCreditedDays(), note.getTermDays(),
                        note.getCreatedAt()))
                .toList());
    }

    /**
     * Marks a newly created payout as executed and records the status transition.
     *
     * @param id payout identifier
     * @return the updated payout response
     * @throws ResourceNotFoundException if the payout does not exist
     * @throws de.hunar.insurance.shared.domain.DomainException if the payout is not in the {@code CREATED} state
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
