package de.hunar.insurance.billing.controller;

import de.hunar.insurance.billing.application.BillingService;
import de.hunar.insurance.billing.dto.CreditNoteResponse;
import de.hunar.insurance.shared.web.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


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
    public PageResponse<CreditNoteResponse> findAll(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return service.findCreditNotes(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id")));
    }
}
