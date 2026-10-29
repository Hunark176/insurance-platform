package de.hunar.insurance.customer.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "customers")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank
    @Size(max = 50)
    @Column(name = "customer_number", nullable = false, unique = true, length = 50)
    private String customerNumber;
    @NotBlank
    @Size(max = 200)
    @Column(name = "display_name", nullable = false, length = 200)
    private String displayName;
    @NotBlank
    @Email
    @Size(max = 320)
    @Column(nullable = false, unique = true, length = 320)
    private String email;
    @Column(nullable = false)
    private boolean active = true;

    public Customer(String customerNumber, String displayName, String email) {
        this.customerNumber = requireText(customerNumber, "customerNumber", 50);
        this.displayName = requireText(displayName, "displayName", 200);
        this.email = requireText(email, "email", 320);
    }

    private static String requireText(String value, String field, int maxLength) {
        if (value == null || value.isBlank() || value.length() > maxLength) {
            throw new IllegalArgumentException(field + " must be non-blank and at most " + maxLength + " characters");
        }
        return value;
    }
}
