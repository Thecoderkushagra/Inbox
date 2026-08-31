package com.messaging.backend.common.validation.validator;

import com.messaging.backend.common.validation.annotation.StrongPassword;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Implementation of the @StrongPassword constraint.
 * 
 * Purpose:
 * Executes the actual thread-safe logic to verify password complexity.
 * 
 * Usage:
 * Hooked automatically by Bean Validation API when @StrongPassword is used.
 * 
 * Limitations:
 * Designed to evaluate standard latin character sets robustly; regex is avoided
 * to prevent catastrophic backtracking on malicious large inputs.
 * 
 * Extension points:
 * Extend logic in isValid if organizational requirements shift (e.g. demanding 2 special chars).
 */
public class StrongPasswordValidator implements ConstraintValidator<StrongPassword, String> {

    private int minLength;
    private boolean requireUppercase;
    private boolean requireLowercase;
    private boolean requireDigit;
    private boolean requireSpecial;

    @Override
    public void initialize(StrongPassword constraintAnnotation) {
        this.minLength = constraintAnnotation.minLength();
        this.requireUppercase = constraintAnnotation.requireUppercase();
        this.requireLowercase = constraintAnnotation.requireLowercase();
        this.requireDigit = constraintAnnotation.requireDigit();
        this.requireSpecial = constraintAnnotation.requireSpecial();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }

        if (value.length() < minLength) {
            return false;
        }

        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;

        for (char c : value.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else hasSpecial = true;
        }

        if (requireUppercase && !hasUpper) return false;
        if (requireLowercase && !hasLower) return false;
        if (requireDigit && !hasDigit) return false;
        if (requireSpecial && !hasSpecial) return false;

        return true;
    }
}
