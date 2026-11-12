package de.hunar.insurance.billing.controller;

import de.hunar.insurance.billing.application.BillingService;
import de.hunar.insurance.billing.dto.PayoutResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Exposes public payout queries and payout execution for demonstration purposes.
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
    public List<PayoutResponse> findAll() { return service.findAll(); }
    /**
     * Marks a created payout as executed.
     *
     * @param id payout identifier
     * @return the updated payout
     */
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "basicAuth")
    @PostMapping("/{id}/pay")
    public PayoutResponse markPaid(@PathVariable Long id) { return service.markPaid(id); }
}
