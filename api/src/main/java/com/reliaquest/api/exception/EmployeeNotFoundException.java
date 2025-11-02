package com.reliaquest.api.exception;

/**
 * Exception thrown when an employee is not found in the system.
 * This exception is typically thrown when attempting to retrieve or delete
 * an employee by ID that doesn't exist in the database.
 *
 * <p>This exception is mapped to HTTP 404 (Not Found) status by the
 * GlobalExceptionHandler.</p>
 *
 * @see com.reliaquest.api.exception.GlobalExceptionHandler
 */
public class EmployeeNotFoundException extends RuntimeException {

    /**
     * Constructs a new EmployeeNotFoundException with the specified detail message.
     *
     * @param message the detail message explaining why the exception occurred
     */
    public EmployeeNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructs a new EmployeeNotFoundException with the specified detail message
     * and cause.
     *
     * @param message the detail message explaining why the exception occurred
     * @param cause the cause of this exception (e.g., underlying HTTP exception)
     */
    public EmployeeNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

