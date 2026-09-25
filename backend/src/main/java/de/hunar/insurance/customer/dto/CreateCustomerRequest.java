package de.hunar.insurance.customer.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCustomerRequest(
        @NotBlank @Size(max = 50) String customerNumber,
        @NotBlank @Size(max = 200) String displayName,
        @NotBlank @Email @Size(max = 320) String email) {
}
