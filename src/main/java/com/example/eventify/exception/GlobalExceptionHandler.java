package com.example.eventify.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * GlobalExceptionHandler - Centralized exception handling for the REST API.
 * Implements RFC 7807 (Problem Details for HTTP APIs) standard.
 * All exceptions are transformed into ProblemDetail responses with consistent structure:
 * - type: URI identifying the problem type
 * - title: Short human-readable summary
 * - status: HTTP status code
 * - detail: Explanation specific to this occurrence
 * - instance: URI to the problem instance (the request path)
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handle validation errors from @Valid annotation.
     * Accumulates field-level constraint violations into a map for client understanding.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidationException(
            MethodArgumentNotValidException ex,
            WebRequest request
    ) {
        logger.warn("Validation error: {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setType(URI.create("https://eventify.example.com/problems/validation-error"));
        problemDetail.setTitle("Validation Error");
        problemDetail.setDetail("Validation failed for the provided input");
        problemDetail.setInstance(URI.create(request.getDescription(false).replace("uri=", "")));
        problemDetail.setProperty("errors", errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }

    /**
     * Handle resource not found exceptions (404).
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleResourceNotFoundException(
            ResourceNotFoundException ex,
            WebRequest request
    ) {
        logger.warn("Resource not found: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problemDetail.setType(URI.create("https://eventify.example.com/problems/resource-not-found"));
        problemDetail.setTitle("Resource Not Found");
        problemDetail.setDetail(ex.getMessage());
        problemDetail.setInstance(URI.create(request.getDescription(false).replace("uri=", "")));

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail);
    }

    /**
     * Handle duplicate resource exceptions (409).
     */
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ProblemDetail> handleDuplicateResourceException(
            DuplicateResourceException ex,
            WebRequest request
    ) {
        logger.warn("Duplicate resource: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problemDetail.setType(URI.create("https://eventify.example.com/problems/duplicate-resource"));
        problemDetail.setTitle("Duplicate Resource");
        problemDetail.setDetail(ex.getMessage());
        problemDetail.setInstance(URI.create(request.getDescription(false).replace("uri=", "")));

        return ResponseEntity.status(HttpStatus.CONFLICT).body(problemDetail);
    }

    /**
     * Handle business rule violations (422 or 400).
     */
    @ExceptionHandler(BusinessRuleViolationException.class)
    public ResponseEntity<ProblemDetail> handleBusinessRuleViolationException(
            BusinessRuleViolationException ex,
            WebRequest request
    ) {
        logger.warn("Business rule violation: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setType(URI.create("https://eventify.example.com/problems/business-rule-violation"));
        problemDetail.setTitle("Business Rule Violation");
        problemDetail.setDetail(ex.getMessage());
        problemDetail.setInstance(URI.create(request.getDescription(false).replace("uri=", "")));
        if (ex.getRule() != null) {
            problemDetail.setProperty("rule", ex.getRule());
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }

    /**
     * Legacy exception handlers for backward compatibility
     */
    @ExceptionHandler(InvalidEventException.class)
    public ResponseEntity<ProblemDetail> handleInvalidEventException(
            InvalidEventException ex,
            WebRequest request
    ) {
        logger.warn("Invalid event: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setType(URI.create("https://eventify.example.com/problems/invalid-event"));
        problemDetail.setTitle("Invalid Event");
        problemDetail.setDetail(ex.getMessage());
        problemDetail.setInstance(URI.create(request.getDescription(false).replace("uri=", "")));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }

    @ExceptionHandler(InvalidVenueException.class)
    public ResponseEntity<ProblemDetail> handleInvalidVenueException(
            InvalidVenueException ex,
            WebRequest request
    ) {
        logger.warn("Invalid venue: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setType(URI.create("https://eventify.example.com/problems/invalid-venue"));
        problemDetail.setTitle("Invalid Venue");
        problemDetail.setDetail(ex.getMessage());
        problemDetail.setInstance(URI.create(request.getDescription(false).replace("uri=", "")));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }

    /**
     * Catch-all handler for unexpected exceptions (500).
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGenericException(
            Exception ex,
            WebRequest request
    ) {
        logger.error("Unexpected error", ex);

        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        problemDetail.setType(URI.create("https://eventify.example.com/problems/internal-server-error"));
        problemDetail.setTitle("Internal Server Error");
        problemDetail.setDetail("An unexpected error occurred. Please try again later.");
        problemDetail.setInstance(URI.create(request.getDescription(false).replace("uri=", "")));

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problemDetail);
    }
}
