package de.hunar.insurance.customer.dto;

public record CustomerResponse(Long id, String customerNumber, String displayName, String email, boolean active) {
}
