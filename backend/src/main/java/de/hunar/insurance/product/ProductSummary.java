package de.hunar.insurance.product;

import java.math.BigDecimal;

/**
 * Product terms shared with other modules, without exposing the product entity.
 *
 * @param id persistent product identifier
 * @param code unique product code
 * @param name display name
 * @param coverageLimit maximum covered amount
 * @param premium default premium
 * @param active whether the product can currently be issued
 */
public record ProductSummary(Long id, String code, String name, BigDecimal coverageLimit, BigDecimal premium, boolean active) {
}
