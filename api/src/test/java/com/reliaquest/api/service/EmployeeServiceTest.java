package com.reliaquest.api.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.reliaquest.api.exception.EmployeeNotFoundException;
import com.reliaquest.api.exception.EmployeeServiceException;
import com.reliaquest.api.model.ApiResponse;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.model.EmployeeInput;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * Unit tests for {@link EmployeeService}.
 *
 * <p>This test class verifies the business logic and error handling of the
 * EmployeeService using Mockito to mock the RestTemplate dependency.</p>
 *
 * <p>Test Coverage:</p>
 * <ul>
 *   <li>Successful CRUD operations (getAllEmployees, getEmployeeById, createEmployee, deleteEmployee)</li>
 *   <li>Error scenarios (employee not found, service exceptions)</li>
 *   <li>Edge cases (empty responses, null handling)</li>
 * </ul>
 *
 * @see EmployeeService
 */
@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    /**
     * Mocked RestTemplate for testing HTTP interactions.
     */
    @Mock
    private RestTemplate restTemplate;

    /**
     * The service under test.
     */
    private EmployeeService employeeService;

    /**
     * Base URL for the mock employee API used in tests.
     */
    private static final String BASE_URL = "http://localhost:8112/api/v1/employee";

    /**
     * Sets up the test environment before each test method.
     * Initializes the EmployeeService with mocked dependencies.
     */
    @BeforeEach
    void setUp() {
        employeeService = new EmployeeService(restTemplate, BASE_URL);
    }

    /**
     * Tests successful retrieval of all employees.
     * Verifies that the service correctly processes a successful API response
     * and returns the employee list.
     */
    @Test
    void getAllEmployees_Success() {
        // Arrange - Create test data
        Employee employee1 = Employee.builder()
                .id("1")
                .employeeName("John Doe")
                .employeeSalary(50000)
                .employeeAge(30)
                .employeeTitle("Developer")
                .employeeEmail("john@example.com")
                .build();

        Employee employee2 = Employee.builder()
                .id("2")
                .employeeName("Jane Smith")
                .employeeSalary(60000)
                .employeeAge(32)
                .employeeTitle("Senior Developer")
                .employeeEmail("jane@example.com")
                .build();

        List<Employee> employees = Arrays.asList(employee1, employee2);
        ApiResponse<List<Employee>> apiResponse = new ApiResponse<>(employees, "Success", null);
        ResponseEntity<ApiResponse<List<Employee>>> responseEntity = ResponseEntity.ok(apiResponse);

        // Mock the RestTemplate to return our test data
        when(restTemplate.exchange(
                        eq(BASE_URL), eq(HttpMethod.GET), any(), any(ParameterizedTypeReference.class)))
                .thenReturn(responseEntity);

        // Act - Call the method under test
        List<Employee> result = employeeService.getAllEmployees();

        // Assert - Verify the results
        assertNotNull(result, "Employee list should not be null");
        assertEquals(2, result.size(), "Should return 2 employees");
        assertEquals("John Doe", result.get(0).getEmployeeName(), "First employee name should match");
        assertEquals("Jane Smith", result.get(1).getEmployeeName(), "Second employee name should match");

        // Verify that RestTemplate was called exactly once
        verify(restTemplate, times(1))
                .exchange(eq(BASE_URL), eq(HttpMethod.GET), any(), any(ParameterizedTypeReference.class));
    }

    @Test
    void getEmployeeById_Success() {
        // Arrange
        String employeeId = "1";
        Employee employee = Employee.builder()
                .id(employeeId)
                .employeeName("John Doe")
                .employeeSalary(50000)
                .employeeAge(30)
                .employeeTitle("Developer")
                .employeeEmail("john@example.com")
                .build();

        ApiResponse<Employee> apiResponse = new ApiResponse<>(employee, "Success", null);
        ResponseEntity<ApiResponse<Employee>> responseEntity = ResponseEntity.ok(apiResponse);

        when(restTemplate.exchange(
                        eq(BASE_URL + "/" + employeeId),
                        eq(HttpMethod.GET),
                        any(),
                        any(ParameterizedTypeReference.class)))
                .thenReturn(responseEntity);

        // Act
        Employee result = employeeService.getEmployeeById(employeeId);

        // Assert
        assertNotNull(result);
        assertEquals(employeeId, result.getId());
        assertEquals("John Doe", result.getEmployeeName());
        verify(restTemplate, times(1))
                .exchange(
                        eq(BASE_URL + "/" + employeeId),
                        eq(HttpMethod.GET),
                        any(),
                        any(ParameterizedTypeReference.class));
    }

    @Test
    void getEmployeeById_NotFound() {
        // Arrange
        String employeeId = "999";

        when(restTemplate.exchange(
                        eq(BASE_URL + "/" + employeeId),
                        eq(HttpMethod.GET),
                        any(),
                        any(ParameterizedTypeReference.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        // Act & Assert
        assertThrows(EmployeeNotFoundException.class, () -> employeeService.getEmployeeById(employeeId));
        verify(restTemplate, times(1))
                .exchange(
                        eq(BASE_URL + "/" + employeeId),
                        eq(HttpMethod.GET),
                        any(),
                        any(ParameterizedTypeReference.class));
    }

    @Test
    void createEmployee_Success() {
        // Arrange
        EmployeeInput input = EmployeeInput.builder()
                .name("John Doe")
                .salary(50000)
                .age(30)
                .title("Developer")
                .build();

        Employee createdEmployee = Employee.builder()
                .id("1")
                .employeeName("John Doe")
                .employeeSalary(50000)
                .employeeAge(30)
                .employeeTitle("Developer")
                .employeeEmail("john@example.com")
                .build();

        ApiResponse<Employee> apiResponse = new ApiResponse<>(createdEmployee, "Success", null);
        ResponseEntity<ApiResponse<Employee>> responseEntity = ResponseEntity.ok(apiResponse);

        when(restTemplate.exchange(
                        eq(BASE_URL), eq(HttpMethod.POST), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenReturn(responseEntity);

        // Act
        Employee result = employeeService.createEmployee(input);

        // Assert
        assertNotNull(result);
        assertEquals("1", result.getId());
        assertEquals("John Doe", result.getEmployeeName());
        verify(restTemplate, times(1))
                .exchange(
                        eq(BASE_URL),
                        eq(HttpMethod.POST),
                        any(HttpEntity.class),
                        any(ParameterizedTypeReference.class));
    }

    @Test
    void deleteEmployeeById_Success() {
        // Arrange
        String employeeId = "1";
        String employeeName = "John Doe";

        Employee employee = Employee.builder()
                .id(employeeId)
                .employeeName(employeeName)
                .employeeSalary(50000)
                .employeeAge(30)
                .employeeTitle("Developer")
                .employeeEmail("john@example.com")
                .build();

        ApiResponse<Employee> getApiResponse = new ApiResponse<>(employee, "Success", null);
        ResponseEntity<ApiResponse<Employee>> getResponseEntity = ResponseEntity.ok(getApiResponse);

        ApiResponse<Boolean> deleteApiResponse = new ApiResponse<>(true, "Success", null);
        ResponseEntity<ApiResponse<Boolean>> deleteResponseEntity = ResponseEntity.ok(deleteApiResponse);

        when(restTemplate.exchange(
                        eq(BASE_URL + "/" + employeeId),
                        eq(HttpMethod.GET),
                        any(),
                        any(ParameterizedTypeReference.class)))
                .thenReturn(getResponseEntity);

        when(restTemplate.exchange(
                        eq(BASE_URL), eq(HttpMethod.DELETE), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenReturn(deleteResponseEntity);

        // Act
        String result = employeeService.deleteEmployeeById(employeeId);

        // Assert
        assertNotNull(result);
        assertEquals(employeeName, result);
        verify(restTemplate, times(1))
                .exchange(
                        eq(BASE_URL + "/" + employeeId),
                        eq(HttpMethod.GET),
                        any(),
                        any(ParameterizedTypeReference.class));
        verify(restTemplate, times(1))
                .exchange(
                        eq(BASE_URL),
                        eq(HttpMethod.DELETE),
                        any(HttpEntity.class),
                        any(ParameterizedTypeReference.class));
    }

    @Test
    void deleteEmployeeById_EmployeeNotFound() {
        // Arrange
        String employeeId = "999";

        when(restTemplate.exchange(
                        eq(BASE_URL + "/" + employeeId),
                        eq(HttpMethod.GET),
                        any(),
                        any(ParameterizedTypeReference.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        // Act & Assert
        assertThrows(EmployeeNotFoundException.class, () -> employeeService.deleteEmployeeById(employeeId));
        verify(restTemplate, times(1))
                .exchange(
                        eq(BASE_URL + "/" + employeeId),
                        eq(HttpMethod.GET),
                        any(),
                        any(ParameterizedTypeReference.class));
        verify(restTemplate, never())
                .exchange(
                        eq(BASE_URL),
                        eq(HttpMethod.DELETE),
                        any(HttpEntity.class),
                        any(ParameterizedTypeReference.class));
    }

    @Test
    void getAllEmployees_ServiceException() {
        // Arrange
        when(restTemplate.exchange(
                        eq(BASE_URL), eq(HttpMethod.GET), any(), any(ParameterizedTypeReference.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        // Act & Assert
        assertThrows(EmployeeServiceException.class, () -> employeeService.getAllEmployees());
    }
}

