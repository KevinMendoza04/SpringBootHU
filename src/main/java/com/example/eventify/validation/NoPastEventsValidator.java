package com.example.eventify.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDate;

/**
 * Validator implementation for @NoPastEvents constraint.
 * Validates that a LocalDate is not in the past (must be today or future).
 */
public class NoPastEventsValidator implements ConstraintValidator<NoPastEvents, LocalDate> {

    @Override
    public void initialize(NoPastEvents constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(LocalDate date, ConstraintValidatorContext context) {
        // null values are handled by @NotNull
        if (date == null) {
            return true;
        }

        // Check if date is today or in the future
        return !date.isBefore(LocalDate.now());
    }
}
