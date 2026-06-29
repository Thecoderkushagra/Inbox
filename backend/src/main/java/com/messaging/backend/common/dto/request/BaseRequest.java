package com.messaging.backend.common.dto.request;

import com.messaging.backend.common.dto.marker.DtoMarker;

/**
 * Common parent for all incoming request DTOs.
 * 
 * Purpose:
 * Establishes a foundation for inbound payload abstraction.
 * 
 * Usage:
 * Implemented by business request records/classes (e.g., RegisterRequest).
 * 
 * Extension points:
 * Can be enriched in the future with request-scoped metadata fields like tracking IDs 
 * or locale identifiers that are automatically populated via interceptors.
 */
public interface BaseRequest extends DtoMarker {
}
