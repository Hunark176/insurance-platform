package de.hunar.insurance.product;

import java.math.BigDecimal;

public record ProductSummary(Long id, String code, String name, BigDecimal coverageLimit, BigDecimal premium, boolean active) {
}
