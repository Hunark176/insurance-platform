package de.hunar.insurance.billing.application;

import de.hunar.insurance.billing.BillingApi;
import de.hunar.insurance.billing.domain.PayoutRepository;
import de.hunar.insurance.billing.dto.PayoutResponse;
import de.hunar.insurance.billing.mapper.PayoutMapper;
import de.hunar.insurance.shared.web.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BillingService implements BillingApi {
    private final PayoutRepository payoutRepository;
    private final PayoutMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public boolean payoutExistsForClaim(Long claimId) {
        return payoutRepository.existsByClaimId(claimId);
    }

    @Transactional(readOnly = true)
    public List<PayoutResponse> findAll() {
        return payoutRepository.findAll().stream().map(mapper::toResponse).toList();
    }

    @Transactional
    public PayoutResponse markPaid(Long id) {
        var payout = payoutRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payout", id));
        payout.markPaid();
        return mapper.toResponse(payoutRepository.save(payout));
    }
}
