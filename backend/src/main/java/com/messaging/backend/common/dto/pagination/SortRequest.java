package com.messaging.backend.common.dto.pagination;

import com.messaging.backend.common.dto.marker.DtoMarker;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Sort;

/**
 * Represents sorting parameters independently from pagination.
 * 
 * Purpose:
 * Defines a strictly typed, immutable sorting instruction.
 * 
 * Usage:
 * Can be used in API requests where sorting is required without pagination.
 * 
 * Extension points:
 * Can be extended to support multiple sort fields or custom sort logic.
 */
public record SortRequest(
        @NotBlank(message = "Sort property must not be blank")
        String property,

        @NotNull(message = "Sort direction must not be null")
        Sort.Direction direction
) implements DtoMarker {

    /**
     * Reconstructs the state with sensible defaults if fields are null.
     */
    public SortRequest {
        if (property == null || property.isBlank()) {
            property = "createdAt";
        }
        if (direction == null) {
            direction = Sort.Direction.DESC;
        }
    }
}
