package com.reliaquest.api.exception;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

/**
 * Global exception handler for the Employee API.
 * This class provides centralized exception handling across all controllers
 * using Spring's @RestControllerAdvice annotation.
 *
 * <p>It handles various types of exceptions and maps them to appropriate
 * HTTP status codes with meaningful error messages. All exceptions are
 * logged for debugging and monitoring purposes.</p>
 *
 * <p>Handled exception types:</p>
 * <ul>
 *   <li>EmployeeNotFoundException - 404 Not Found</li>
 *   <li>EmployeeServiceException - 500 Internal Server Error</li>
 *   <li>HttpClientErrorException - Status code from the exception</li>
 *   <li>ResourceAccessException - 503 Service Unavailable</li>
 *   <li>MethodArgumentNotValidException - 400 Bad Request</li>
 *   <li>General Exception - 500 Internal Server Error</li>
 * </ul>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles EmployeeNotFoundException.
     * This exception is thrown when an employee is not found by ID.
     *
     * @param ex the EmployeeNotFoundException that was thrown
     * @return ResponseEntity with error message and HTTP 404 status
     */
    @ExceptionHandler(EmployeeNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleEmployeeNotFoundException(
            EmployeeNotFoundException ex) {
        // Log the error for debugging purposes
        logger.error("Employee not found: {}", ex.getMessage());

        // Create error response with descriptive message
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", ex.getMessage());

        // Return 404 Not Found status
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * Handles EmployeeServiceException.
     * This exception is thrown when a general service error occurs during
     * employee operations such as communication failures with the backend.
     *
     * @param ex the EmployeeServiceException that was thrown
     * @return ResponseEntity with error message and HTTP 500 status
     */
    @ExceptionHandler(EmployeeServiceException.class)
    public ResponseEntity<Map<String, String>> handleEmployeeServiceException(EmployeeServiceException ex) {
        // Log the full stack trace for service errors
        logger.error("Employee service error: {}", ex.getMessage(), ex);

        // Create error response
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", ex.getMessage());

        // Return 500 Internal Server Error status
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * Handles HttpClientErrorException.
     * This exception is thrown when the HTTP client receives an error status code
     * from the backend service (4xx or 5xx).
     *
     * @param ex the HttpClientErrorException that was thrown
     * @return ResponseEntity with error message and the original HTTP status code
     */
    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<Map<String, String>> handleHttpClientErrorException(HttpClientErrorException ex) {
        // Log the HTTP error
        logger.error("HTTP client error: {}", ex.getMessage());

        // Create error response with status code information
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "Error communicating with employee service: " + ex.getStatusCode());

        // Return the same status code that was received
        return ResponseEntity.status(ex.getStatusCode()).body(errorResponse);
    }

    /**
     * Handles ResourceAccessException.
     * This exception is thrown when the application cannot connect to the backend service,
     * typically due to network issues, service downtime, or timeout.
     *
     * @param ex the ResourceAccessException that was thrown
     * @return ResponseEntity with error message and HTTP 503 status
     */
    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<Map<String, String>> handleResourceAccessException(ResourceAccessException ex) {
        // Log the connection error with full stack trace
        logger.error("Resource access error: {}", ex.getMessage(), ex);

        // Create error response with user-friendly message
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "Unable to connect to employee service");

        // Return 503 Service Unavailable status
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
    }

    /**
     * Handles MethodArgumentNotValidException.
     * This exception is thrown when request body validation fails (e.g., @Valid annotation).
     * Returns a map of field names to error messages for all validation failures.
     *
     * @param ex the MethodArgumentNotValidException that was thrown
     * @return ResponseEntity with validation error messages and HTTP 400 status
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        // Collect all validation errors
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            // Extract field name and error message for each validation failure
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        // Log all validation errors
        logger.error("Validation errors: {}", errors);

        // Return 400 Bad Request status with detailed validation errors
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    /**
     * Handles all other unhandled exceptions.
     * This is a catch-all handler for any unexpected exceptions that occur
     * during request processing. It prevents sensitive error details from
     * being exposed to clients while logging full details for debugging.
     *
     * @param ex the Exception that was thrown
     * @return ResponseEntity with generic error message and HTTP 500 status
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneralException(Exception ex) {
        // Log the unexpected error with full stack trace
        logger.error("Unexpected error: {}", ex.getMessage(), ex);

        // Create generic error response (don't expose internal details to client)
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "An unexpected error occurred");

        // Return 500 Internal Server Error status
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}

