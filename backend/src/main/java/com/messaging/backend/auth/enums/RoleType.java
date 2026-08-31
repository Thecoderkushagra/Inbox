package com.messaging.backend.auth.enums;

/**
 * Defines the allowed role types within the application.
 *
 * <p>Purpose:
 * Restricts roles to a predefined set of constants to prevent invalid roles 
 * and facilitate type-safe role assignments.
 *
 * <p>Lifecycle:
 * Managed as static constants throughout the lifetime of the application.
 *
 * <p>Future extension points:
 * Can be expanded to include more granular roles such as MODERATOR, 
 * SUPPORT, or SYSTEM as the application's permission model evolves.
 */
public enum RoleType {
    USER,
    ADMIN
}
