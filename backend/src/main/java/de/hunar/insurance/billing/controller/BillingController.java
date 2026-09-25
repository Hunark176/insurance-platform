package de.hunar.insurance.billing.controller;

import de.hunar.insurance.billing.application.BillingService;
import de.hunar.insurance.billing.dto.PayoutResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payouts")
@RequiredArgsConstructor
public class BillingController {
    private final BillingService service;
    @GetMapping
    @PreAuthorize("hasAnyRole('CLERK','ADMIN')")
    public List<PayoutResponse> findAll() { return service.findAll(); }
    @PostMapping("/{id}/pay")
    @PreAuthorize("hasRole('ADMIN')")
    public PayoutResponse markPaid(@PathVariable Long id) { return service.markPaid(id); }
}
