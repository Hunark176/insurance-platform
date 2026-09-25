package de.hunar.insurance.product.mapper;

import de.hunar.insurance.product.ProductSummary;
import de.hunar.insurance.product.domain.Product;
import de.hunar.insurance.product.dto.ProductResponse;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {
    public ProductResponse toResponse(Product p) {
        return new ProductResponse(p.getId(), p.getCode(), p.getName(), p.getCoverageLimit(), p.getPremium(), p.isActive());
    }
    public ProductSummary toSummary(Product p) {
        return new ProductSummary(p.getId(), p.getCode(), p.getName(), p.getCoverageLimit(), p.getPremium(), p.isActive());
    }
}
