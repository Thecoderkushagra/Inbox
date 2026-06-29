package com.messaging.backend.common.dto.response;

import com.messaging.backend.common.dto.marker.DtoMarker;

/**
 * Common parent for all outgoing business response DTOs.
 * 
 * Purpose:
 * Standardizes the type definition of outbound payloads that will eventually 
 * be wrapped inside ApiResponse.
 * 
 * Usage:
 * Implemented by business response records/classes (e.g., UserResponse).
 * 
 * Extension points:
 * Ready for future extension if shared response traits (like hateoas links) 
 * are introduced application-wide.
 */
public interface BaseResponse extends DtoMarker {
}
