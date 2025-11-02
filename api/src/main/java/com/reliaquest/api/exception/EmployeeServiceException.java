package com.reliaquest.api.exception;

/**
 * Exception thrown when a general service error occurs while processing employee operations.
 * This exception is used for various service-level failures such as:
 * <ul>
 *   <li>Communication failures with the mock employee API</li>
 *   <li>Invalid responses from the backend service</li>
 *   <li>Retry exhaustion after multiple attempts</li>
 *   <li>Unexpected errors during business logic execution</li>
 * </ul>
 *
 * <p>This exception is mapped to HTTP 500 (Internal Server Error) status by the
 * GlobalExceptionHandler.</p>
 *
 * @see com.reliaquest.api.exception.GlobalExceptionHandler
 */
public class EmployeeServiceException extends RuntimeException {

    /**
     * Constructs a new EmployeeServiceException with the specified detail message.
     *
     * @param message the detail message explaining the service error
     */
    public EmployeeServiceException(String message) {
        super(message);
    }

    /**
     * Constructs a new EmployeeServiceException with the specified detail message
     * and cause.
     *
     * @param message the detail message explaining the service error
     * @param cause the underlying cause of this exception (e.g., RestClientException)
     */
    public EmployeeServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}

