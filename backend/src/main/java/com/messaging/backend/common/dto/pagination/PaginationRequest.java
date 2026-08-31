package com.messaging.backend.common.dto.pagination;

import com.messaging.backend.common.dto.request.BaseRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Standard request object for paginated API endpoints.
 * 
 * Purpose:
 * Centralizes pagination logic, constraints, and default values across all domain controllers.
 * 
 * Usage:
 * Bound directly in REST Controllers using Spring Web data binding.
 * 
 * Extension points:
 * The toPageable() method transparently translates the DTO into the Spring Data Pageable interface.
 */
public record PaginationRequest(
        @Min(value = 0, message = "Page number cannot be less than 0")
        Integer page,

        @Min(value = 1, message = "Page size must be at least 1")
        @Max(value = 100, message = "Page size cannot exceed 100")
        Integer size,

        String sortBy,

        Sort.Direction direction
) implements BaseRequest {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final String DEFAULT_SORT_BY = "createdAt";
    private static final Sort.Direction DEFAULT_DIRECTION = Sort.Direction.DESC;

    /**
     * Reconstructs the state with sensible defaults if fields are null.
     */
    public PaginationRequest {
        if (page == null) page = DEFAULT_PAGE;
        if (size == null) size = DEFAULT_SIZE;
        if (sortBy == null || sortBy.isBlank()) sortBy = DEFAULT_SORT_BY;
        if (direction == null) direction = DEFAULT_DIRECTION;
    }

    /**
     * Converts this DTO into a Spring Data Pageable instance.
     * 
     * @return a strictly typed Pageable for JPA repositories
     */
    public Pageable toPageable() {
        return PageRequest.of(page, size, Sort.by(direction, sortBy));
    }
}
