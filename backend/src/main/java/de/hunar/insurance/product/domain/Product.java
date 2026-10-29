package de.hunar.insurance.product.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "products")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank
    @Size(max = 50)
    @Column(nullable = false, unique = true, length = 50)
    private String code;
    @NotBlank
    @Size(max = 200)
    @Column(nullable = false, length = 200)
    private String name;
    @NotNull
    @DecimalMin("0.01")
    @Digits(integer = 17, fraction = 2)
    @Column(name = "coverage_limit", nullable = false, precision = 19, scale = 2)
    private BigDecimal coverageLimit;
    @NotNull
    @DecimalMin("0.00")
    @Digits(integer = 17, fraction = 2)
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal premium;
    @Column(nullable = false)
    private boolean active = true;

    public Product(String code, String name, BigDecimal coverageLimit, BigDecimal premium) {
        this.code = requireText(code, "code", 50);
        this.name = requireText(name, "name", 200);
        this.coverageLimit = requireAmount(coverageLimit, "coverageLimit", true);
        this.premium = requireAmount(premium, "premium", false);
    }

    private static String requireText(String value, String field, int maxLength) {
        if (value == null || value.isBlank() || value.length() > maxLength) {
            throw new IllegalArgumentException(field + " must be non-blank and at most " + maxLength + " characters");
        }
        return value;
    }

    private static BigDecimal requireAmount(BigDecimal value, String field, boolean mustBePositive) {
        if (value == null || (mustBePositive ? value.signum() <= 0 : value.signum() < 0)
                || value.scale() > 2 || value.precision() - value.scale() > 17) {
            throw new IllegalArgumentException(field + " must fit a non-negative decimal with at most 2 decimal places");
        }
        return value;
    }
}
