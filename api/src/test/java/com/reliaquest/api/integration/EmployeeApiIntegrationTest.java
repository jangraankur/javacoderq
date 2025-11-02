package com.reliaquest.api.integration;

import static org.junit.jupiter.api.Assertions.*;

import com.reliaquest.api.model.Employee;
import com.reliaquest.api.model.EmployeeInput;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Integration tests for the Employee API.
 *
 * <p>These tests verify the end-to-end functionality of the API by starting
 * the full Spring Boot application and making real HTTP requests.</p>
 *
 * <p><b>Important Note:</b> These tests require the Mock Employee Server to be
 * running on port 8112 before execution. Start the server with:</p>
 * <pre>./gradlew server:bootRun</pre>
 *
 * <p>The tests are commented out by default to prevent failures in CI/CD
 * environments where the mock server may not be available. Remove the comment
 * markers (//) to enable the tests when running locally with the server.</p>
 *
 * <p>Test Coverage:</p>
 * <ul>
 *   <li>Complete request/response cycles through all layers</li>
 *   <li>Actual HTTP communication with the mock server</li>
 *   <li>JSON serialization/deserialization in real scenarios</li>
 *   <li>End-to-end validation and error handling</li>
 * </ul>
 *
 * @see com.reliaquest.api.controller.EmployeeController
 * @see com.reliaquest.api.service.EmployeeService
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class EmployeeApiIntegrationTest {

    /**
     * Random port assigned to the test application by Spring Boot.
     */
    @LocalServerPort
    private int port;

    /**
     * TestRestTemplate for making HTTP requests to the test application.
     */
    @Autowired
    private TestRestTemplate restTemplate;

    /**
     * Constructs the base URL for API requests using the randomly assigned port.
     *
     * @return the base URL for employee API endpoints
     */
    private String getBaseUrl() {
        return "http://localhost:" + port + "/api/v1/employee";
    }

    @Test
    void contextLoads() {
        // Verify that the application context loads successfully
        assertNotNull(restTemplate);
    }

    // Note: The following tests will work only when the Mock Employee Server is running
    // They are disabled by default to prevent test failures in CI/CD environments

    // @Test
    void testGetAllEmployees() {
        // Arrange & Act
        ResponseEntity<List<Employee>> response = restTemplate.exchange(
                getBaseUrl(), HttpMethod.GET, null, new ParameterizedTypeReference<List<Employee>>() {});

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().size() > 0);
    }

    // @Test
    void testGetEmployeesByNameSearch() {
        // Arrange
        String searchString = "Tiger";

        // Act
        ResponseEntity<List<Employee>> response = restTemplate.exchange(
                getBaseUrl() + "/search/" + searchString,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Employee>>() {});

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    // @Test
    void testGetEmployeeById() {
        // First, get all employees to get a valid ID
        ResponseEntity<List<Employee>> allEmployeesResponse = restTemplate.exchange(
                getBaseUrl(), HttpMethod.GET, null, new ParameterizedTypeReference<List<Employee>>() {});

        assertNotNull(allEmployeesResponse.getBody());
        assertTrue(allEmployeesResponse.getBody().size() > 0);

        String employeeId = allEmployeesResponse.getBody().get(0).getId();

        // Act
        ResponseEntity<Employee> response = restTemplate.getForEntity(getBaseUrl() + "/" + employeeId, Employee.class);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(employeeId, response.getBody().getId());
    }

    // @Test
    void testGetHighestSalary() {
        // Arrange & Act
        ResponseEntity<Integer> response = restTemplate.getForEntity(getBaseUrl() + "/highestSalary", Integer.class);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() > 0);
    }

    // @Test
    void testGetTopTenHighestEarningEmployeeNames() {
        // Arrange & Act
        ResponseEntity<List<String>> response = restTemplate.exchange(
                getBaseUrl() + "/topTenHighestEarningEmployeeNames",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<String>>() {});

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().size() <= 10);
    }

    // @Test
    void testCreateEmployee() {
        // Arrange
        EmployeeInput input = EmployeeInput.builder()
                .name("Test Employee")
                .salary(75000)
                .age(28)
                .title("QA Engineer")
                .build();

        // Act
        ResponseEntity<Employee> response =
                restTemplate.postForEntity(getBaseUrl(), input, Employee.class);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Test Employee", response.getBody().getEmployeeName());
        assertEquals(75000, response.getBody().getEmployeeSalary());
    }

    // @Test
    void testDeleteEmployee() {
        // First, create an employee to delete
        EmployeeInput input = EmployeeInput.builder()
                .name("Employee To Delete")
                .salary(50000)
                .age(30)
                .title("Temporary")
                .build();

        ResponseEntity<Employee> createResponse =
                restTemplate.postForEntity(getBaseUrl(), input, Employee.class);
        assertNotNull(createResponse.getBody());
        String employeeId = createResponse.getBody().getId();

        // Act
        ResponseEntity<String> deleteResponse =
                restTemplate.exchange(getBaseUrl() + "/" + employeeId, HttpMethod.DELETE, null, String.class);

        // Assert
        assertEquals(HttpStatus.OK, deleteResponse.getStatusCode());
        assertEquals("Employee To Delete", deleteResponse.getBody());
    }

    // @Test
    void testGetEmployeeById_NotFound() {
        // Arrange
        String nonExistentId = "00000000-0000-0000-0000-000000000000";

        // Act
        ResponseEntity<String> response =
                restTemplate.getForEntity(getBaseUrl() + "/" + nonExistentId, String.class);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // @Test
    void testCreateEmployee_ValidationError() {
        // Arrange - invalid age
        EmployeeInput input = EmployeeInput.builder()
                .name("Invalid Employee")
                .salary(50000)
                .age(10) // Invalid age - below minimum
                .title("Developer")
                .build();

        // Act
        ResponseEntity<String> response = restTemplate.postForEntity(getBaseUrl(), input, String.class);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}

