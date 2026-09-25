package de.hunar.insurance.product.dto;

import java.math.BigDecimal;

public record ProductResponse(Long id, String code, String name, BigDecimal coverageLimit, BigDecimal premium,
                              boolean active) {
}
