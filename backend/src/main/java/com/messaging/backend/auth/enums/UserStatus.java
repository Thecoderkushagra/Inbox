package com.messaging.backend.auth.enums;

/**
 * Represents the current status of a user account.
 *
 * <p>Purpose:
 * Used to determine if a user can authenticate or perform actions within the system.
 *
 * <p>Lifecycle:
 * A user starts in PENDING_VERIFICATION, moves to ACTIVE upon email verification,
 * and may transition to LOCKED, DISABLED, or DELETED based on administrative 
 * or security actions.
 *
 * <p>Future extension points:
 * Additional statuses like SUSPENDED (temporary disable) or ARCHIVED 
 * could be added if the business rules require more nuanced account states.
 */
public enum UserStatus {
    PENDING_VERIFICATION,
    ACTIVE,
    LOCKED,
    DISABLED,
    DELETED
}
