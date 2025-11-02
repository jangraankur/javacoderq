package com.reliaquest.api.service;

import com.reliaquest.api.exception.EmployeeNotFoundException;
import com.reliaquest.api.exception.EmployeeServiceException;
import com.reliaquest.api.model.ApiResponse;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.model.EmployeeInput;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Service layer for employee operations.
 * This class handles all business logic and communication with the mock employee API.
 *
 * <p>Key features:</p>
 * <ul>
 *   <li>Automatic retry mechanism for handling rate limiting and transient failures</li>
 *   <li>Exponential backoff strategy for retries</li>
 *   <li>Comprehensive error handling and logging</li>
 *   <li>Proper exception translation from HTTP errors to domain exceptions</li>
 * </ul>
 *
 * <p>The service uses RestTemplate for HTTP communication and implements
 * a retry mechanism to handle the mock server's random rate limiting behavior.</p>
 *
 * @see Employee
 * @see EmployeeInput
 * @see EmployeeNotFoundException
 * @see EmployeeServiceException
 */
@Service
public class EmployeeService {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeService.class);

    /**
     * Maximum number of retry attempts for failed requests.
     * Applied to rate-limited (429) and server error (5xx) responses.
     */
    private static final int MAX_RETRIES = 3;

    /**
     * Base delay in milliseconds between retry attempts.
     * Actual delay is calculated as: RETRY_DELAY_MS * attemptNumber
     * (exponential backoff).
     */
    private static final long RETRY_DELAY_MS = 1000;

    /**
     * RestTemplate instance for making HTTP requests.
     * Injected via constructor.
     */
    private final RestTemplate restTemplate;

    /**
     * Base URL for the mock employee API.
     * Configurable via application properties with default fallback.
     */
    private final String baseUrl;

    /**
     * Constructs a new EmployeeService with the required dependencies.
     *
     * @param restTemplate the REST client for HTTP communication
     * @param baseUrl the base URL of the employee API (configurable via properties)
     */
    public EmployeeService(
            RestTemplate restTemplate,
            @Value("${employee.api.base-url:http://localhost:8112/api/v1/employee}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    /**
     * Retrieves all employees from the mock employee API.
     *
     * <p>This method makes a GET request to the base employee endpoint and
     * returns the list of employees from the response. If the response body
     * or data is null, an empty list is returned.</p>
     *
     * <p>The request is automatically retried up to {@link #MAX_RETRIES} times
     * if rate limiting or server errors occur.</p>
     *
     * @return list of all employees, or empty list if none found
     * @throws EmployeeServiceException if unable to communicate with the API
     *                                  or if all retry attempts fail
     */
    public List<Employee> getAllEmployees() {
        logger.info("Fetching all employees");
        try {
            // Execute GET request with retry logic
            ResponseEntity<ApiResponse<List<Employee>>> response = executeWithRetry(() -> restTemplate.exchange(
                    baseUrl,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<ApiResponse<List<Employee>>>() {}));

            // Extract and return employee data from response
            if (response.getBody() != null && response.getBody().getData() != null) {
                logger.info("Successfully fetched {} employees", response.getBody().getData().size());
                return response.getBody().getData();
            }

            // Return empty list if response body is null
            logger.warn("Received empty response body");
            return Collections.emptyList();
        } catch (HttpStatusCodeException e) {
            // Handle HTTP error status codes
            logger.error("Error fetching employees: HTTP {}", e.getStatusCode(), e);
            throw new EmployeeServiceException("Failed to fetch employees: " + e.getMessage(), e);
        } catch (RestClientException e) {
            // Handle connection/communication errors
            logger.error("Error communicating with employee service", e);
            throw new EmployeeServiceException("Unable to communicate with employee service", e);
        }
    }

    /**
     * Retrieves a single employee by their unique identifier.
     *
     * <p>This method makes a GET request to the employee endpoint with the
     * specified ID. If the employee is not found (404 response), an
     * EmployeeNotFoundException is thrown.</p>
     *
     * <p>The request is automatically retried up to {@link #MAX_RETRIES} times
     * if rate limiting or server errors occur.</p>
     *
     * @param id the unique identifier of the employee
     * @return the employee with the specified ID
     * @throws EmployeeNotFoundException if no employee exists with the given ID
     * @throws EmployeeServiceException if unable to communicate with the API
     *                                  or if all retry attempts fail
     */
    public Employee getEmployeeById(String id) {
        logger.info("Fetching employee with id: {}", id);
        try {
            // Execute GET request for specific employee with retry logic
            ResponseEntity<ApiResponse<Employee>> response = executeWithRetry(() -> restTemplate.exchange(
                    baseUrl + "/" + id,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<ApiResponse<Employee>>() {}));

            // Extract and return employee data from response
            if (response.getBody() != null && response.getBody().getData() != null) {
                logger.info("Successfully fetched employee with id: {}", id);
                return response.getBody().getData();
            }

            // Throw NotFoundException if data is null
            logger.warn("Employee not found with id: {}", id);
            throw new EmployeeNotFoundException("Employee not found with id: " + id);
        } catch (HttpStatusCodeException e) {
            // Special handling for 404 Not Found status
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                logger.warn("Employee not found with id: {}", id);
                throw new EmployeeNotFoundException("Employee not found with id: " + id, e);
            }
            // Handle other HTTP error status codes
            logger.error("Error fetching employee: HTTP {}", e.getStatusCode(), e);
            throw new EmployeeServiceException("Failed to fetch employee: " + e.getMessage(), e);
        } catch (RestClientException e) {
            // Handle connection/communication errors
            logger.error("Error communicating with employee service", e);
            throw new EmployeeServiceException("Unable to communicate with employee service", e);
        }
    }

    /**
     * Creates a new employee in the system.
     *
     * <p>This method makes a POST request to the employee endpoint with the
     * provided employee data. The mock server generates the employee ID and
     * email address automatically.</p>
     *
     * <p>The request body is constructed as a Map to match the mock server's
     * expected format (name, salary, age, title).</p>
     *
     * <p>The request is automatically retried up to {@link #MAX_RETRIES} times
     * if rate limiting or server errors occur.</p>
     *
     * @param input the employee data for creation (must be validated)
     * @return the newly created employee with generated ID and email
     * @throws EmployeeServiceException if unable to create the employee,
     *                                  communicate with the API, or if all retry attempts fail
     */
    public Employee createEmployee(EmployeeInput input) {
        logger.info("Creating employee with name: {}", input.getName());
        try {
            // Set up HTTP headers for JSON content
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Build request body with employee data
            // Using Map instead of EmployeeInput to match the API's expected format
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("name", input.getName());
            requestBody.put("salary", input.getSalary());
            requestBody.put("age", input.getAge());
            requestBody.put("title", input.getTitle());

            // Create HTTP entity with body and headers
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // Execute POST request with retry logic
            ResponseEntity<ApiResponse<Employee>> response = executeWithRetry(() -> restTemplate.exchange(
                    baseUrl, HttpMethod.POST, entity, new ParameterizedTypeReference<ApiResponse<Employee>>() {}));

            // Extract and return created employee from response
            if (response.getBody() != null && response.getBody().getData() != null) {
                logger.info("Successfully created employee: {}", response.getBody().getData().getId());
                return response.getBody().getData();
            }

            // Throw exception if response body is empty
            logger.error("Failed to create employee - empty response");
            throw new EmployeeServiceException("Failed to create employee - empty response");
        } catch (HttpStatusCodeException e) {
            // Handle HTTP error status codes
            logger.error("Error creating employee: HTTP {}", e.getStatusCode(), e);
            throw new EmployeeServiceException("Failed to create employee: " + e.getMessage(), e);
        } catch (RestClientException e) {
            // Handle connection/communication errors
            logger.error("Error communicating with employee service", e);
            throw new EmployeeServiceException("Unable to communicate with employee service", e);
        }
    }

    /**
     * Deletes an employee by their unique identifier.
     *
     * <p>This method performs a two-step process:</p>
     * <ol>
     *   <li>Fetches the employee to get their name (required by delete API)</li>
     *   <li>Sends DELETE request with the employee name in the request body</li>
     * </ol>
     *
     * <p>Note: The mock API's delete endpoint requires the employee name
     * rather than the ID, so we must fetch the employee first.</p>
     *
     * <p>The delete request is automatically retried up to {@link #MAX_RETRIES}
     * times if rate limiting or server errors occur.</p>
     *
     * @param id the unique identifier of the employee to delete
     * @return the name of the deleted employee
     * @throws EmployeeNotFoundException if no employee exists with the given ID
     * @throws EmployeeServiceException if unable to delete the employee,
     *                                  communicate with the API, or if all retry attempts fail
     */
    public String deleteEmployeeById(String id) {
        logger.info("Deleting employee with id: {}", id);

        // First, get the employee to retrieve the name
        // This will throw EmployeeNotFoundException if employee doesn't exist
        Employee employee = getEmployeeById(id);
        String employeeName = employee.getEmployeeName();

        try {
            // Set up HTTP headers for JSON content
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Build request body with employee name (required by delete API)
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("name", employeeName);

            // Create HTTP entity with body and headers
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

            // Execute DELETE request with retry logic
            ResponseEntity<ApiResponse<Boolean>> response = executeWithRetry(() -> restTemplate.exchange(
                    baseUrl, HttpMethod.DELETE, entity, new ParameterizedTypeReference<ApiResponse<Boolean>>() {}));

            // Check if deletion was successful
            if (response.getBody() != null
                    && response.getBody().getData() != null
                    && response.getBody().getData()) {
                logger.info("Successfully deleted employee: {}", employeeName);
                return employeeName;
            }

            // Throw exception if deletion failed
            logger.error("Failed to delete employee with id: {}", id);
            throw new EmployeeServiceException("Failed to delete employee with id: " + id);
        } catch (HttpStatusCodeException e) {
            // Handle HTTP error status codes
            logger.error("Error deleting employee: HTTP {}", e.getStatusCode(), e);
            throw new EmployeeServiceException("Failed to delete employee: " + e.getMessage(), e);
        } catch (RestClientException e) {
            // Handle connection/communication errors
            logger.error("Error communicating with employee service", e);
            throw new EmployeeServiceException("Unable to communicate with employee service", e);
        }
    }

    /**
     * Executes an HTTP operation with automatic retry logic.
     *
     * <p>This method implements a retry mechanism with exponential backoff to handle:</p>
     * <ul>
     *   <li>Rate limiting (HTTP 429 status)</li>
     *   <li>Server errors (HTTP 5xx status codes)</li>
     *   <li>Transient network failures</li>
     * </ul>
     *
     * <p>Retry strategy:</p>
     * <ul>
     *   <li>Maximum attempts: {@link #MAX_RETRIES}</li>
     *   <li>Delay between retries: {@link #RETRY_DELAY_MS} * attempt number</li>
     *   <li>4xx errors (except 429) are not retried as they indicate client errors</li>
     * </ul>
     *
     * <p>Example delay sequence:
     * 1st retry: 1000ms, 2nd retry: 2000ms, 3rd retry: 3000ms</p>
     *
     * @param <T> the return type of the operation
     * @param operation the operation to execute (lambda expression)
     * @return the result of the successful operation
     * @throws HttpStatusCodeException if a 4xx client error occurs (no retry)
     * @throws EmployeeServiceException if all retry attempts fail
     */
    private <T> T executeWithRetry(RetryableOperation<T> operation) {
        int attempts = 0;
        Exception lastException = null;

        // Retry loop - continues until max retries reached
        while (attempts < MAX_RETRIES) {
            try {
                // Execute the operation
                return operation.execute();
            } catch (HttpStatusCodeException e) {
                lastException = e;

                // Determine if the error is retryable
                // Retry on: 429 (rate limit) or 5xx (server errors)
                // Don't retry on: 4xx client errors (except 429)
                if (e.getStatusCode().is5xxServerError() || e.getStatusCode().value() == 429) {
                    attempts++;
                    if (attempts < MAX_RETRIES) {
                        // Log retry attempt
                        logger.warn(
                                "Request failed with status {}. Retrying... (attempt {}/{})",
                                e.getStatusCode(),
                                attempts,
                                MAX_RETRIES);
                        try {
                            // Wait before retrying (exponential backoff)
                            Thread.sleep(RETRY_DELAY_MS * attempts);
                        } catch (InterruptedException ie) {
                            // Restore interrupt status and throw exception
                            Thread.currentThread().interrupt();
                            throw new EmployeeServiceException("Retry interrupted", ie);
                        }
                    }
                } else {
                    // For 4xx errors (except 429), don't retry - these are client errors
                    throw e;
                }
            } catch (RestClientException e) {
                // Handle general REST client exceptions (connection errors, timeouts, etc.)
                lastException = e;
                attempts++;
                if (attempts < MAX_RETRIES) {
                    logger.warn("Request failed. Retrying... (attempt {}/{})", attempts, MAX_RETRIES);
                    try {
                        // Wait before retrying (exponential backoff)
                        Thread.sleep(RETRY_DELAY_MS * attempts);
                    } catch (InterruptedException ie) {
                        // Restore interrupt status and throw exception
                        Thread.currentThread().interrupt();
                        throw new EmployeeServiceException("Retry interrupted", ie);
                    }
                }
            }
        }

        // All retry attempts failed
        logger.error("All retry attempts failed");
        throw new EmployeeServiceException("Failed after " + MAX_RETRIES + " attempts", lastException);
    }

    /**
     * Functional interface for operations that can be retried.
     * Used to pass lambda expressions or method references to the retry mechanism.
     *
     * @param <T> the return type of the operation
     */
    @FunctionalInterface
    private interface RetryableOperation<T> {
        /**
         * Executes the operation.
         *
         * @return the result of the operation
         * @throws RestClientException if the operation fails
         */
        T execute();
    }
}
