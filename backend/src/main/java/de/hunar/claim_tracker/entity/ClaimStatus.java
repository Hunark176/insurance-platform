package de.hunar.claim_tracker.entity;

public enum ClaimStatus {
    RECEIVED,      // Schadenmeldung eingegangen
    IN_REVIEW,     // Sachbearbeiter prüft
    APPROVED,      // Genehmigt (nicht ACCEPTED)
    REJECTED       // Abgelehnt (nicht DECLINED)
}
