package com.messaging.backend.common.validation.annotation;

import com.messaging.backend.common.validation.validator.NoHtmlValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validates that a string does not contain HTML tags.
 * 
 * Purpose:
 * Basic security guard against simple XSS payloads in generic text fields.
 * 
 * Usage:
 * Place on String fields (like names, bios, or raw messages) to prevent markup injection.
 * 
 * Limitations:
 * Extremely lightweight string check. Does not sanitize; simply rejects.
 * 
 * Extension points:
 * N/A. Should remain a strict preventative check.
 */
@Documented
@Constraint(validatedBy = NoHtmlValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface NoHtml {

    String message() default "Input must not contain HTML tags";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
