package de.hunar.insurance.customer.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "customers")
@Getter
@NoArgsConstructor
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "customer_number", nullable = false, unique = true, length = 50)
    private String customerNumber;
    @Column(name = "display_name", nullable = false, length = 200)
    private String displayName;
    @Column(nullable = false, unique = true, length = 320)
    private String email;
    @Column(nullable = false)
    private boolean active = true;

    public Customer(String customerNumber, String displayName, String email) {
        this.customerNumber = customerNumber;
        this.displayName = displayName;
        this.email = email;
    }
}
