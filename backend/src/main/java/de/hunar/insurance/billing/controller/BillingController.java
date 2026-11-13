package de.hunar.insurance.billing.controller;

import de.hunar.insurance.billing.application.BillingService;
import de.hunar.insurance.billing.dto.PayoutResponse;
import de.hunar.insurance.shared.web.PageResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


/**
 * Exposes public payout queries and payout execution for demonstration purposes.
 */
@RestController
@RequestMapping("/api/payouts")
@RequiredArgsConstructor
public class BillingController {
    private final BillingService service;
    /**
     * Lists payouts page by page, newest first.
     *
     * @param page zero-based page index
     * @param size page size from 1 to 100
     * @return the requested page of payouts
     */
    @GetMapping
    public PageResponse<PayoutResponse> findAll(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return service.findPayouts(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id")));
    }
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
