package com.messaging.backend.common.dto.marker;

import java.io.Serializable;

/**
 * Marker interface for all Data Transfer Objects (DTOs) in the application.
 * 
 * Purpose:
 * Enforces a strict type boundary for payloads passing through the service layer.
 * 
 * Usage:
 * Every request or response DTO must implement this interface directly or indirectly.
 * 
 * Extension points:
 * Can be used by AOP pointcuts or global validation logic to target all DTOs cleanly.
 */
public interface DtoMarker extends Serializable {
}
