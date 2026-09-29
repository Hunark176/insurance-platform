package de.hunar.insurance.claim.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Eine paginierte Übersicht von Schadenfällen")
public record ClaimPageResponse(
        List<ClaimResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {
}
