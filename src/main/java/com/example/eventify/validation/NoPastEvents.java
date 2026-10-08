package com.example.eventify.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Custom constraint annotation to validate that event dates are not in the past.
 * Ensures business rule: events must be scheduled for today or future dates.
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NoPastEventsValidator.class)
@Documented
public @interface NoPastEvents {
    String message() default "La fecha del evento no puede ser anterior a hoy";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
