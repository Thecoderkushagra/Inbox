package com.messaging.backend.common.validation.annotation;

import com.messaging.backend.common.validation.validator.StrongPasswordValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validates that a string satisfies strong password requirements.
 * 
 * Purpose:
 * Enforces security policies universally across any password-related DTOs.
 * 
 * Usage:
 * Place on a String field inside a DTO. 
 * Allows configuration of minimum length and character classes.
 * 
 * Limitations:
 * Does not check against dictionaries of compromised passwords.
 * 
 * Extension points:
 * Configurable via annotation properties. Further logic can be augmented in StrongPasswordValidator.
 */
@Documented
@Constraint(validatedBy = StrongPasswordValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface StrongPassword {

    String message() default "Password must be strong: minimum 8 characters, at least 1 uppercase, 1 lowercase, 1 digit, and 1 special character";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    int minLength() default 8;
    boolean requireUppercase() default true;
    boolean requireLowercase() default true;
    boolean requireDigit() default true;
    boolean requireSpecial() default true;
}
