package com.ems.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class EmployeeDTO {
    private Long id;

    @NotBlank(message = "Name is required")
    private String name;

    @Email(message = "Valid email required")
    private String email;

    @DecimalMin(value = "0.0", message = "Salary must be positive")
    private BigDecimal salary;

    private Long departmentId;
    private String departmentName;
}
