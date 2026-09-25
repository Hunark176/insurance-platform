package de.hunar.insurance.product.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "products")
@Getter
@NoArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 50)
    private String code;
    @Column(nullable = false, length = 200)
    private String name;
    @Column(name = "coverage_limit", nullable = false, precision = 19, scale = 2)
    private BigDecimal coverageLimit;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal premium;
    @Column(nullable = false)
    private boolean active = true;

    public Product(String code, String name, BigDecimal coverageLimit, BigDecimal premium) {
        this.code = code;
        this.name = name;
        this.coverageLimit = coverageLimit;
        this.premium = premium;
    }
}
