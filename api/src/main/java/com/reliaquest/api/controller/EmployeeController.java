package com.reliaquest.api.controller;

import com.reliaquest.api.model.Employee;
import com.reliaquest.api.model.EmployeeInput;
import com.reliaquest.api.service.EmployeeService;
import jakarta.validation.Valid;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing employee operations.
 * This controller implements the IEmployeeController interface and provides
 * endpoints for CRUD operations and employee queries.
 *
 * <p>Base URI: /api/v1/employee</p>
 *
 * <p>All methods include comprehensive logging and delegate business logic
 * to the EmployeeService layer. Exceptions are handled by the GlobalExceptionHandler.</p>
 *
 * @see IEmployeeController
 * @see EmployeeService
 * @see com.reliaquest.api.exception.GlobalExceptionHandler
 */
@RestController
@RequestMapping("/api/v1/employee")
public class EmployeeController implements IEmployeeController<Employee, EmployeeInput> {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeController.class);

    /**
     * Service layer dependency for employee operations.
     * Injected via constructor for better testability.
     */
    private final EmployeeService employeeService;

    /**
     * Constructs a new EmployeeController with the required service dependency.
     *
     * @param employeeService the service layer for employee operations
     */
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    /**
     * Retrieves all employees from the system.
     *
     * <p>Endpoint: GET /api/v1/employee</p>
     *
     * @return ResponseEntity containing a list of all employees and HTTP 200 status
     * @throws com.reliaquest.api.exception.EmployeeServiceException if unable to fetch employees
     */
    @Override
    public ResponseEntity<List<Employee>> getAllEmployees() {
        logger.info("GET request to /api/v1/employee - getAllEmployees");

        // Fetch all employees from the service layer
        List<Employee> employees = employeeService.getAllEmployees();

        return ResponseEntity.ok(employees);
    }

    /**
     * Searches for employees by name using a case-insensitive substring match.
     *
     * <p>Endpoint: GET /api/v1/employee/search/{searchString}</p>
     *
     * <p>The search is performed on the employee name field and matches any
     * employee whose name contains the search string (case-insensitive).</p>
     *
     * @param searchString the string to search for in employee names
     * @return ResponseEntity containing a list of matching employees and HTTP 200 status
     * @throws com.reliaquest.api.exception.EmployeeServiceException if unable to fetch employees
     */
    @Override
    public ResponseEntity<List<Employee>> getEmployeesByNameSearch(String searchString) {
        logger.info("GET request to /api/v1/employee/search/{} - getEmployeesByNameSearch", searchString);

        // Fetch all employees
        List<Employee> employees = employeeService.getAllEmployees();

        // Filter employees whose names contain the search string (case-insensitive)
        List<Employee> filteredEmployees = employees.stream()
                .filter(employee -> employee.getEmployeeName() != null
                        && employee.getEmployeeName().toLowerCase().contains(searchString.toLowerCase()))
                .collect(Collectors.toList());

        logger.info("Found {} employees matching search string: {}", filteredEmployees.size(), searchString);
        return ResponseEntity.ok(filteredEmployees);
    }

    /**
     * Retrieves a single employee by their unique identifier.
     *
     * <p>Endpoint: GET /api/v1/employee/{id}</p>
     *
     * @param id the unique identifier of the employee
     * @return ResponseEntity containing the employee and HTTP 200 status
     * @throws com.reliaquest.api.exception.EmployeeNotFoundException if employee not found
     * @throws com.reliaquest.api.exception.EmployeeServiceException if unable to fetch employee
     */
    @Override
    public ResponseEntity<Employee> getEmployeeById(String id) {
        logger.info("GET request to /api/v1/employee/{} - getEmployeeById", id);

        // Fetch the employee from the service layer
        Employee employee = employeeService.getEmployeeById(id);

        return ResponseEntity.ok(employee);
    }

    /**
     * Retrieves the highest salary among all employees.
     *
     * <p>Endpoint: GET /api/v1/employee/highestSalary</p>
     *
     * <p>If no employees exist or all salaries are null, returns 0.</p>
     *
     * @return ResponseEntity containing the highest salary value and HTTP 200 status
     * @throws com.reliaquest.api.exception.EmployeeServiceException if unable to fetch employees
     */
    @Override
    public ResponseEntity<Integer> getHighestSalaryOfEmployees() {
        logger.info("GET request to /api/v1/employee/highestSalary - getHighestSalaryOfEmployees");

        // Fetch all employees
        List<Employee> employees = employeeService.getAllEmployees();

        // Find the highest salary using streams
        // Filter out null salaries, then get the maximum value
        Integer highestSalary = employees.stream()
                .map(Employee::getEmployeeSalary)
                .filter(salary -> salary != null)
                .max(Integer::compareTo)
                .orElse(0); // Default to 0 if no salaries found

        logger.info("Highest salary found: {}", highestSalary);
        return ResponseEntity.ok(highestSalary);
    }

    /**
     * Retrieves the names of the top 10 highest-earning employees.
     *
     * <p>Endpoint: GET /api/v1/employee/topTenHighestEarningEmployeeNames</p>
     *
     * <p>Employees are sorted by salary in descending order, and the names
     * of the top 10 are returned. If fewer than 10 employees exist, all
     * employee names are returned.</p>
     *
     * @return ResponseEntity containing a list of employee names and HTTP 200 status
     * @throws com.reliaquest.api.exception.EmployeeServiceException if unable to fetch employees
     */
    @Override
    public ResponseEntity<List<String>> getTopTenHighestEarningEmployeeNames() {
        logger.info(
                "GET request to /api/v1/employee/topTenHighestEarningEmployeeNames - getTopTenHighestEarningEmployeeNames");

        // Fetch all employees
        List<Employee> employees = employeeService.getAllEmployees();

        // Use stream pipeline to:
        // 1. Filter out employees with null salaries
        // 2. Sort by salary in descending order
        // 3. Limit to top 10 employees
        // 4. Extract employee names
        List<String> topTenNames = employees.stream()
                .filter(employee -> employee.getEmployeeSalary() != null)
                .sorted(Comparator.comparing(Employee::getEmployeeSalary).reversed())
                .limit(10)
                .map(Employee::getEmployeeName)
                .collect(Collectors.toList());

        logger.info("Found {} employees in top ten highest earners", topTenNames.size());
        return ResponseEntity.ok(topTenNames);
    }

    /**
     * Creates a new employee in the system.
     *
     * <p>Endpoint: POST /api/v1/employee</p>
     *
     * <p>The input is validated using Bean Validation annotations.
     * Validation failures result in HTTP 400 status with error details.</p>
     *
     * @param employeeInput the employee data for creation (validated)
     * @return ResponseEntity containing the created employee and HTTP 200 status
     * @throws org.springframework.web.bind.MethodArgumentNotValidException if validation fails
     * @throws com.reliaquest.api.exception.EmployeeServiceException if unable to create employee
     */
    @Override
    public ResponseEntity<Employee> createEmployee(@Valid EmployeeInput employeeInput) {
        logger.info("POST request to /api/v1/employee - createEmployee with name: {}", employeeInput.getName());

        // Delegate to service layer to create the employee
        Employee createdEmployee = employeeService.createEmployee(employeeInput);

        return ResponseEntity.ok(createdEmployee);
    }

    /**
     * Deletes an employee by their unique identifier.
     *
     * <p>Endpoint: DELETE /api/v1/employee/{id}</p>
     *
     * <p>The method first retrieves the employee to get their name,
     * then deletes the employee. The deleted employee's name is returned.</p>
     *
     * @param id the unique identifier of the employee to delete
     * @return ResponseEntity containing the deleted employee's name and HTTP 200 status
     * @throws com.reliaquest.api.exception.EmployeeNotFoundException if employee not found
     * @throws com.reliaquest.api.exception.EmployeeServiceException if unable to delete employee
     */
    @Override
    public ResponseEntity<String> deleteEmployeeById(String id) {
        logger.info("DELETE request to /api/v1/employee/{} - deleteEmployeeById", id);

        // Delegate to service layer to delete the employee
        String deletedEmployeeName = employeeService.deleteEmployeeById(id);

        logger.info("Successfully deleted employee: {}", deletedEmployeeName);
        return ResponseEntity.ok(deletedEmployeeName);
    }
}

