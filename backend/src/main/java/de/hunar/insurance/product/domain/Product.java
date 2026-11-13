package de.hunar.insurance.product.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Defines an insurance product and the default coverage limit and premium copied to new policies.
 */
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

    /**
     * Annual aggregate as a multiple of the coverage limit ("2-fache Maximierung"): all losses of
     * one policy year together are paid up to {@code coverageLimit × factor}. {@code null} means
     * that only the per-loss coverage limit applies.
     */
    @Min(1)
    @Max(MAX_ANNUAL_AGGREGATE_FACTOR)
    @Column(name = "annual_aggregate_factor")
    private Integer annualAggregateFactor;

    /** Upper bound for the annual aggregate factor; larger values are not used in practice. */
    public static final int MAX_ANNUAL_AGGREGATE_FACTOR = 10;

    /**
     * Creates an active product without an annual aggregate.
     *
     * @param code unique product code
     * @param name display name
     * @param coverageLimit positive maximum covered amount per loss
     * @param premium non-negative premium
     * @throws IllegalArgumentException if text or monetary values violate product invariants
     */
    public Product(String code, String name, BigDecimal coverageLimit, BigDecimal premium) {
        this(code, name, coverageLimit, premium, null);
    }

    /**
     * Creates an active product with validated identifiers and monetary terms.
     *
     * @param code unique product code
     * @param name display name
     * @param coverageLimit positive maximum covered amount per loss
     * @param premium non-negative premium
     * @param annualAggregateFactor annual aggregate as multiple of the coverage limit, or {@code null}
     * @throws IllegalArgumentException if text, monetary values or the factor violate product invariants
     */
    public Product(String code, String name, BigDecimal coverageLimit, BigDecimal premium,
                   Integer annualAggregateFactor) {
        this.code = requireText(code, "code", 50);
        this.name = requireText(name, "name", 200);
        this.coverageLimit = requireAmount(coverageLimit, "coverageLimit", true);
        this.premium = requireAmount(premium, "premium", false);
        if (annualAggregateFactor != null
                && (annualAggregateFactor < 1 || annualAggregateFactor > MAX_ANNUAL_AGGREGATE_FACTOR)) {
            throw new IllegalArgumentException(
                    "annualAggregateFactor must be between 1 and " + MAX_ANNUAL_AGGREGATE_FACTOR);
        }
        this.annualAggregateFactor = annualAggregateFactor;
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
