package com.messaging.backend.common.validation.validator;

import com.messaging.backend.common.validation.annotation.NoHtml;
import com.messaging.backend.common.validation.util.ValidationUtils;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Implementation of the @NoHtml constraint.
 * 
 * Purpose:
 * Thread-safely rejects inputs containing suspected HTML tag characters.
 * 
 * Usage:
 * Hooked automatically by Bean Validation API when @NoHtml is used.
 * 
 * Limitations:
 * This is a lightweight preventative check utilizing pattern matching, not a comprehensive 
 * HTML sanitizer. Complex XSS mitigation should be managed at the frontend boundaries.
 * 
 * Extension points:
 * Can adopt a deeper parser (like OWASP Java HTML Sanitizer) if strict validation is required.
 */
public class NoHtmlValidator implements ConstraintValidator<NoHtml, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // Nulls should be checked by @NotNull
        }
        return !ValidationUtils.containsHtml(value);
    }
}
