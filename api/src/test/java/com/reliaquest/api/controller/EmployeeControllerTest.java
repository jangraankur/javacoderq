package com.reliaquest.api.controller;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reliaquest.api.exception.EmployeeNotFoundException;
import com.reliaquest.api.exception.EmployeeServiceException;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.model.EmployeeInput;
import com.reliaquest.api.service.EmployeeService;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Unit tests for {@link EmployeeController}.
 *
 * <p>This test class uses Spring's MockMvc framework to test the controller layer
 * in isolation. The EmployeeService is mocked to focus on testing the controller's
 * HTTP request/response handling, validation, and error scenarios.</p>
 *
 * <p>Test Coverage:</p>
 * <ul>
 *   <li>All REST endpoints (GET, POST, DELETE)</li>
 *   <li>Successful responses with correct HTTP status codes</li>
 *   <li>Error responses (404, 400, 500)</li>
 *   <li>Request body validation</li>
 *   <li>JSON serialization/deserialization</li>
 *   <li>Edge cases (empty lists, invalid input)</li>
 * </ul>
 *
 * @see EmployeeController
 */
@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {

    /**
     * MockMvc for simulating HTTP requests to the controller.
     */
    @Autowired
    private MockMvc mockMvc;

    /**
     * ObjectMapper for JSON serialization in test setup.
     */
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Mocked EmployeeService to isolate controller logic.
     */
    @MockBean
    private EmployeeService employeeService;

    @Test
    void getAllEmployees_Success() throws Exception {
        // Arrange
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

        when(employeeService.getAllEmployees()).thenReturn(employees);

        // Act & Assert
        mockMvc.perform(get("/api/v1/employee"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is("1")))
                .andExpect(jsonPath("$[0].employee_name", is("John Doe")))
                .andExpect(jsonPath("$[1].id", is("2")))
                .andExpect(jsonPath("$[1].employee_name", is("Jane Smith")));

        verify(employeeService, times(1)).getAllEmployees();
    }

    @Test
    void getAllEmployees_EmptyList() throws Exception {
        // Arrange
        when(employeeService.getAllEmployees()).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/v1/employee"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(employeeService, times(1)).getAllEmployees();
    }

    @Test
    void getEmployeesByNameSearch_Success() throws Exception {
        // Arrange
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
                .employeeName("Johnny Smith")
                .employeeSalary(60000)
                .employeeAge(32)
                .employeeTitle("Senior Developer")
                .employeeEmail("johnny@example.com")
                .build();

        List<Employee> allEmployees = Arrays.asList(employee1, employee2);

        when(employeeService.getAllEmployees()).thenReturn(allEmployees);

        // Act & Assert
        mockMvc.perform(get("/api/v1/employee/search/john"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].employee_name", is("John Doe")))
                .andExpect(jsonPath("$[1].employee_name", is("Johnny Smith")));

        verify(employeeService, times(1)).getAllEmployees();
    }

    @Test
    void getEmployeesByNameSearch_NoResults() throws Exception {
        // Arrange
        Employee employee = Employee.builder()
                .id("1")
                .employeeName("John Doe")
                .employeeSalary(50000)
                .employeeAge(30)
                .employeeTitle("Developer")
                .employeeEmail("john@example.com")
                .build();

        List<Employee> employees = Arrays.asList(employee);

        when(employeeService.getAllEmployees()).thenReturn(employees);

        // Act & Assert
        mockMvc.perform(get("/api/v1/employee/search/jane"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(employeeService, times(1)).getAllEmployees();
    }

    @Test
    void getEmployeeById_Success() throws Exception {
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

        when(employeeService.getEmployeeById(employeeId)).thenReturn(employee);

        // Act & Assert
        mockMvc.perform(get("/api/v1/employee/" + employeeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(employeeId)))
                .andExpect(jsonPath("$.employee_name", is("John Doe")))
                .andExpect(jsonPath("$.employee_salary", is(50000)));

        verify(employeeService, times(1)).getEmployeeById(employeeId);
    }

    @Test
    void getEmployeeById_NotFound() throws Exception {
        // Arrange
        String employeeId = "999";

        when(employeeService.getEmployeeById(employeeId))
                .thenThrow(new EmployeeNotFoundException("Employee not found with id: " + employeeId));

        // Act & Assert
        mockMvc.perform(get("/api/v1/employee/" + employeeId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", containsString("Employee not found")));

        verify(employeeService, times(1)).getEmployeeById(employeeId);
    }

    @Test
    void getHighestSalaryOfEmployees_Success() throws Exception {
        // Arrange
        Employee employee1 = Employee.builder()
                .id("1")
                .employeeName("John Doe")
                .employeeSalary(50000)
                .build();

        Employee employee2 = Employee.builder()
                .id("2")
                .employeeName("Jane Smith")
                .employeeSalary(80000)
                .build();

        Employee employee3 = Employee.builder()
                .id("3")
                .employeeName("Bob Johnson")
                .employeeSalary(65000)
                .build();

        List<Employee> employees = Arrays.asList(employee1, employee2, employee3);

        when(employeeService.getAllEmployees()).thenReturn(employees);

        // Act & Assert
        mockMvc.perform(get("/api/v1/employee/highestSalary"))
                .andExpect(status().isOk())
                .andExpect(content().string("80000"));

        verify(employeeService, times(1)).getAllEmployees();
    }

    @Test
    void getHighestSalaryOfEmployees_EmptyList() throws Exception {
        // Arrange
        when(employeeService.getAllEmployees()).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/v1/employee/highestSalary"))
                .andExpect(status().isOk())
                .andExpect(content().string("0"));

        verify(employeeService, times(1)).getAllEmployees();
    }

    @Test
    void getTopTenHighestEarningEmployeeNames_Success() throws Exception {
        // Arrange
        List<Employee> employees = Arrays.asList(
                Employee.builder()
                        .id("1")
                        .employeeName("Employee 1")
                        .employeeSalary(100000)
                        .build(),
                Employee.builder()
                        .id("2")
                        .employeeName("Employee 2")
                        .employeeSalary(95000)
                        .build(),
                Employee.builder()
                        .id("3")
                        .employeeName("Employee 3")
                        .employeeSalary(90000)
                        .build(),
                Employee.builder()
                        .id("4")
                        .employeeName("Employee 4")
                        .employeeSalary(85000)
                        .build(),
                Employee.builder()
                        .id("5")
                        .employeeName("Employee 5")
                        .employeeSalary(80000)
                        .build());

        when(employeeService.getAllEmployees()).thenReturn(employees);

        // Act & Assert
        mockMvc.perform(get("/api/v1/employee/topTenHighestEarningEmployeeNames"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)))
                .andExpect(jsonPath("$[0]", is("Employee 1")))
                .andExpect(jsonPath("$[1]", is("Employee 2")))
                .andExpect(jsonPath("$[2]", is("Employee 3")));

        verify(employeeService, times(1)).getAllEmployees();
    }

    @Test
    void createEmployee_Success() throws Exception {
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

        when(employeeService.createEmployee(any(EmployeeInput.class))).thenReturn(createdEmployee);

        // Act & Assert
        mockMvc.perform(post("/api/v1/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("1")))
                .andExpect(jsonPath("$.employee_name", is("John Doe")))
                .andExpect(jsonPath("$.employee_salary", is(50000)));

        verify(employeeService, times(1)).createEmployee(any(EmployeeInput.class));
    }

    @Test
    void createEmployee_ValidationError() throws Exception {
        // Arrange
        EmployeeInput input = EmployeeInput.builder()
                .name("") // Invalid: blank name
                .salary(-100) // Invalid: negative salary
                .age(10) // Invalid: age below minimum
                .title("") // Invalid: blank title
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest());

        verify(employeeService, never()).createEmployee(any(EmployeeInput.class));
    }

    @Test
    void deleteEmployeeById_Success() throws Exception {
        // Arrange
        String employeeId = "1";
        String employeeName = "John Doe";

        when(employeeService.deleteEmployeeById(employeeId)).thenReturn(employeeName);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/employee/" + employeeId))
                .andExpect(status().isOk())
                .andExpect(content().string(employeeName));

        verify(employeeService, times(1)).deleteEmployeeById(employeeId);
    }

    @Test
    void deleteEmployeeById_NotFound() throws Exception {
        // Arrange
        String employeeId = "999";

        when(employeeService.deleteEmployeeById(employeeId))
                .thenThrow(new EmployeeNotFoundException("Employee not found with id: " + employeeId));

        // Act & Assert
        mockMvc.perform(delete("/api/v1/employee/" + employeeId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", containsString("Employee not found")));

        verify(employeeService, times(1)).deleteEmployeeById(employeeId);
    }

    @Test
    void getAllEmployees_ServiceException() throws Exception {
        // Arrange
        when(employeeService.getAllEmployees())
                .thenThrow(new EmployeeServiceException("Service unavailable"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/employee"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error", containsString("Service unavailable")));

        verify(employeeService, times(1)).getAllEmployees();
    }
}

