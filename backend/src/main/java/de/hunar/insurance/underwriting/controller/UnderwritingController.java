package de.hunar.insurance.underwriting.controller;

import de.hunar.insurance.underwriting.application.UnderwritingService;
import de.hunar.insurance.underwriting.dto.UnderwritingCheckRequest;
import de.hunar.insurance.underwriting.dto.UnderwritingCheckResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/underwriting")
@RequiredArgsConstructor
public class UnderwritingController {
    private final UnderwritingService service;

    @PostMapping("/check")
    @PreAuthorize("hasAnyRole('CLERK','ADMIN')")
    public UnderwritingCheckResponse check(@Valid @RequestBody UnderwritingCheckRequest request) {
        return service.check(request);
    }
}
