package com.reliaquest.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Generic wrapper class for API responses from the mock employee server.
 * This class follows the response structure defined by the mock API contract.
 *
 * <p>The response structure includes:</p>
 * <ul>
 *   <li>data: The actual response payload (can be an object, list, or primitive)</li>
 *   <li>status: A status message indicating success or failure</li>
 *   <li>error: An error message if the request failed (null on success)</li>
 * </ul>
 *
 * <p>Note: Null fields are excluded from JSON serialization.</p>
 *
 * @param <T> The type of data contained in the response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    /**
     * The actual response data.
     * Type varies based on the endpoint (Employee, List&lt;Employee&gt;, Boolean, etc.)
     */
    private T data;

    /**
     * Status message from the server.
     * Typically "Successfully processed request." for successful operations.
     */
    private String status;

    /**
     * Error message if the request failed.
     * Null or empty for successful operations.
     */
    private String error;
}

