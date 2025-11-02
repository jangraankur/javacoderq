package com.reliaquest.api.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) for creating a new employee.
 * This class encapsulates the required fields for employee creation
 * and includes validation constraints to ensure data integrity.
 *
 * <p>Validation Rules:</p>
 * <ul>
 *   <li>Name: Cannot be blank or null</li>
 *   <li>Salary: Must be a positive integer (greater than zero)</li>
 *   <li>Age: Must be between 16 and 75 (inclusive)</li>
 *   <li>Title: Cannot be blank or null</li>
 * </ul>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeInput {

    /**
     * The full name of the employee to be created.
     * This field is required and cannot be blank.
     */
    @NotBlank(message = "Name is required")
    private String name;

    /**
     * The annual salary for the employee.
     * Must be a positive integer greater than zero.
     */
    @Positive(message = "Salary must be greater than zero")
    private Integer salary;

    /**
     * The age of the employee in years.
     * Must be between 16 (minimum working age) and 75 (maximum) inclusive.
     */
    @Min(value = 16, message = "Age must be at least 16")
    @Max(value = 75, message = "Age must be at most 75")
    private Integer age;

    /**
     * The job title or position for the employee.
     * This field is required and cannot be blank.
     */
    @NotBlank(message = "Title is required")
    private String title;
}

