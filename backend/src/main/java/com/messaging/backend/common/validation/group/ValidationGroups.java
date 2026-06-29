package com.messaging.backend.common.validation.group;

/**
 * Marker interfaces for Bean Validation grouping.
 * 
 * Purpose:
 * Allows developers to apply different validation rules to the same DTO depending on the operation.
 * 
 * Usage:
 * Use with @Validated(ValidationGroups.Create.class) in Controllers or service parameters.
 * 
 * Limitations:
 * Only defines basic CRUD groups. Complex business states should be validated programmatically.
 * 
 * Extension points:
 * Can be extended with more specific operational markers if needed.
 */
public interface ValidationGroups {
    interface Default {}
    interface Create extends Default {}
    interface Update extends Default {}
    interface Delete extends Default {}
}
