package de.hunar.insurance.shared.web;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Stable JSON shape for paginated lists; the same fields as the claim overview.
 *
 * <p>Spring's {@code Page} is not serialized directly because its JSON layout is not a stable
 * API contract.</p>
 *
 * @param content entries of the requested page
 * @param page zero-based page index
 * @param size requested page size
 * @param totalElements number of entries across all pages
 * @param totalPages number of pages
 * @param first whether this is the first page
 * @param last whether this is the last page
 * @param <T> entry type
 */
@Schema(description = "Eine Seite einer paginierten Liste")
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {
    /**
     * @param page Spring Data page whose metadata is used
     * @param content already mapped entries of that page
     */
    public static <T> PageResponse<T> of(Page<?> page, List<T> content) {
        return new PageResponse<>(content, page.getNumber(), page.getSize(), page.getTotalElements(),
                page.getTotalPages(), page.isFirst(), page.isLast());
    }
}
