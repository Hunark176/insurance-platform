package de.hunar.insurance.claim.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Status eines Claims", example = "RECEIVED")
public enum ClaimStatus {
    RECEIVED,      // Schadenmeldung eingegangen
    IN_REVIEW,     // Sachbearbeiter prüft
    APPROVED,      // Genehmigt (nicht ACCEPTED)
    REJECTED       // Abgelehnt (nicht DECLINED)
}
