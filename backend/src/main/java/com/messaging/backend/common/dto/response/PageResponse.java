package com.messaging.backend.common.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Standardized response format for paginated collections.
 * 
 * Purpose:
 * Safely exposes pagination metadata (totals, pages, boundaries) without leaking internal 
 * Spring Data Page implementations to the API layer.
 * 
 * Intended usage:
 * Used in Controllers to wrap list endpoints before placing inside a SuccessResponse.
 * e.g., return ResponseEntity.ok(SuccessResponse.success(PageResponse.of(springDataPage)));
 * 
 * Future extension points:
 * Can be extended to support cursor-based pagination elements if the platform shifts
 * away from strictly offset-based pagination in the future.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last,
        boolean hasNext,
        boolean hasPrevious
) {
    
    /**
     * Constructs a PageResponse safely mapped from a Spring Data Page object.
     * 
     * @param page the Spring Data Page object
     * @param <T> the type of the content elements
     * @return a mapped PageResponse
     */
    public static <T> PageResponse<T> of(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast(),
                page.hasNext(),
                page.hasPrevious()
        );
    }
}
