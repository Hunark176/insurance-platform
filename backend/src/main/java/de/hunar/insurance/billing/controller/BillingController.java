package de.hunar.insurance.billing.controller;

import de.hunar.insurance.billing.application.BillingService;
import de.hunar.insurance.billing.dto.PayoutResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Exposes staff payout queries and administrator-only payout execution.
 */
@RestController
@RequestMapping("/api/payouts")
@RequiredArgsConstructor
public class BillingController {
    private final BillingService service;
    /**
     * Lists payouts for authorized billing staff.
     *
     * @return all payout responses
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('CLERK','ADMIN')")
    public List<PayoutResponse> findAll() { return service.findAll(); }
    /**
     * Marks a created payout as executed.
     *
     * @param id payout identifier
     * @return the updated payout
     */
    @PostMapping("/{id}/pay")
    @PreAuthorize("hasRole('ADMIN')")
    public PayoutResponse markPaid(@PathVariable Long id) { return service.markPaid(id); }
}
