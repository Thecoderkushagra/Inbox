package com.messaging.backend.users.enums;

/**
 * Determines the visibility of a user's profile to other users.
 *
 * <p>Purpose:
 * Enables users to control who can view their profile details, ensuring privacy.
 * 
 * <p>Values:
 * PUBLIC: Visible to everyone on the platform.
 * PRIVATE: Visible only to the user themselves.
 * FRIENDS_ONLY: Visible only to users on the owner's friend list.
 */
public enum ProfileVisibility {
    PUBLIC,
    PRIVATE,
    FRIENDS_ONLY
}
