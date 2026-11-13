package de.hunar.insurance.billing.controller;

import de.hunar.insurance.billing.application.BillingService;
import de.hunar.insurance.billing.dto.CreditNoteResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Exposes credit notes created after policy cancellations.
 */
@RestController
@RequestMapping("/api/credit-notes")
@RequiredArgsConstructor
public class CreditNoteController {
    private final BillingService service;

    @GetMapping
    @Operation(summary = "Gutschriften nach Policenstorno auflisten",
            description = "Anteilige Prämie für den nicht mehr gedeckten Zeitraum (§ 39 VVG).")
    public List<CreditNoteResponse> findAll() {
        return service.findCreditNotes();
    }
}
