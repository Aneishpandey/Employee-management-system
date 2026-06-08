package com.ems.service;

import com.ems.dto.EmployeeDTO;
import com.ems.exception.ResourceNotFoundException;
import com.ems.model.Department;
import com.ems.model.Employee;
import com.ems.repository.DepartmentRepository;
import com.ems.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// @ExtendWith: tells JUnit to use Mockito to create mocks
@ExtendWith(MockitoExtension.class)
@DisplayName("EmployeeService Unit Tests")
class EmployeeServiceTest {

    // @Mock: creates a fake EmployeeRepository
    // No real DB — Mockito controls what it returns
    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    // @InjectMocks: creates real EmployeeService
    // and injects the mocks above into it
    @InjectMocks
    private EmployeeService employeeService;

    // Test data we reuse across tests
    private Department testDepartment;
    private Employee testEmployee;
    private EmployeeDTO testEmployeeDTO;

    // @BeforeEach: runs before EVERY test method
    // Sets up fresh test data each time
    @BeforeEach
    void setUp() {
        testDepartment = new Department();
        testDepartment.setId(1L);
        testDepartment.setName("Engineering");
        testDepartment.setDescription("Software Engineering");

        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setName("Anish Pandey");
        testEmployee.setEmail("anish@ems.com");
        testEmployee.setSalary(new BigDecimal("75000"));
        testEmployee.setDepartment(testDepartment);

        testEmployeeDTO = new EmployeeDTO();
        testEmployeeDTO.setName("Anish Pandey");
        testEmployeeDTO.setEmail("anish@ems.com");
        testEmployeeDTO.setSalary(new BigDecimal("75000"));
        testEmployeeDTO.setDepartmentId(1L);
    }

    // ================================================================
    // GET ALL EMPLOYEES TESTS
    // ================================================================

    @Test
    @DisplayName("getAllEmployees - should return list of all employees")
    void getAllEmployees_ShouldReturnAllEmployees() {
        // ARRANGE: tell mock what to return when findAll() is called
        when(employeeRepository.findAll()).thenReturn(List.of(testEmployee));

        // ACT: call the real method we're testing
        List<EmployeeDTO> result = employeeService.getAllEmployees();

        // ASSERT: verify the result is what we expect
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Anish Pandey");
        assertThat(result.get(0).getEmail()).isEqualTo("anish@ems.com");
        assertThat(result.get(0).getSalary()).isEqualByComparingTo("75000");

        // VERIFY: confirm findAll() was called exactly once
        verify(employeeRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getAllEmployees - should return empty list when no employees")
    void getAllEmployees_ShouldReturnEmptyList_WhenNoEmployees() {
        when(employeeRepository.findAll()).thenReturn(List.of());

        List<EmployeeDTO> result = employeeService.getAllEmployees();

        assertThat(result).isEmpty();
        verify(employeeRepository, times(1)).findAll();
    }

    // ================================================================
    // GET EMPLOYEE BY ID TESTS
    // ================================================================

    @Test
    @DisplayName("getEmployeeById - should return employee when found")
    void getEmployeeById_ShouldReturnEmployee_WhenFound() {
        // Mock: when findById(1) is called, return our test employee
        when(employeeRepository.findById(1L))
            .thenReturn(Optional.of(testEmployee));

        EmployeeDTO result = employeeService.getEmployeeById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Anish Pandey");
        assertThat(result.getDepartmentName()).isEqualTo("Engineering");
    }

    @Test
    @DisplayName("getEmployeeById - should throw exception when not found")
    void getEmployeeById_ShouldThrowException_WhenNotFound() {
        // Mock: return empty Optional (employee doesn't exist)
        when(employeeRepository.findById(999L))
            .thenReturn(Optional.empty());

        // assertThatThrownBy: verify the method throws the right exception
        assertThatThrownBy(() -> employeeService.getEmployeeById(999L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("999");
    }

    // ================================================================
    // CREATE EMPLOYEE TESTS
    // ================================================================

    @Test
    @DisplayName("createEmployee - should create and return new employee")
    void createEmployee_ShouldCreateEmployee_WhenValidData() {
        // Mock setup
        when(employeeRepository.existsByEmail("anish@ems.com"))
            .thenReturn(false);
        when(departmentRepository.findById(1L))
            .thenReturn(Optional.of(testDepartment));
        when(employeeRepository.save(any(Employee.class)))
            .thenReturn(testEmployee);

        EmployeeDTO result = employeeService.createEmployee(testEmployeeDTO);

        assertThat(result.getName()).isEqualTo("Anish Pandey");
        assertThat(result.getEmail()).isEqualTo("anish@ems.com");

        // Verify save was actually called
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("createEmployee - should throw exception when email already exists")
    void createEmployee_ShouldThrowException_WhenEmailExists() {
        // Mock: email already taken
        when(employeeRepository.existsByEmail("anish@ems.com"))
            .thenReturn(true);

        assertThatThrownBy(() ->
            employeeService.createEmployee(testEmployeeDTO))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("anish@ems.com");

        // Verify save was NEVER called (we stopped before saving)
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    @DisplayName("createEmployee - should throw exception when department not found")
    void createEmployee_ShouldThrowException_WhenDepartmentNotFound() {
        when(employeeRepository.existsByEmail(any())).thenReturn(false);
        when(departmentRepository.findById(99L))
            .thenReturn(Optional.empty());

        testEmployeeDTO.setDepartmentId(99L);

        assertThatThrownBy(() ->
            employeeService.createEmployee(testEmployeeDTO))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Department not found");
    }

    // ================================================================
    // UPDATE EMPLOYEE TESTS
    // ================================================================

    @Test
    @DisplayName("updateEmployee - should update and return employee")
    void updateEmployee_ShouldUpdateEmployee_WhenExists() {
        EmployeeDTO updateDTO = new EmployeeDTO();
        updateDTO.setName("Anish Updated");
        updateDTO.setEmail("anish@ems.com");
        updateDTO.setSalary(new BigDecimal("90000"));
        updateDTO.setDepartmentId(1L);

        Employee updatedEmployee = new Employee();
        updatedEmployee.setId(1L);
        updatedEmployee.setName("Anish Updated");
        updatedEmployee.setEmail("anish@ems.com");
        updatedEmployee.setSalary(new BigDecimal("90000"));
        updatedEmployee.setDepartment(testDepartment);

        when(employeeRepository.findById(1L))
            .thenReturn(Optional.of(testEmployee));
        when(departmentRepository.findById(1L))
            .thenReturn(Optional.of(testDepartment));
        when(employeeRepository.save(any(Employee.class)))
            .thenReturn(updatedEmployee);

        EmployeeDTO result = employeeService.updateEmployee(1L, updateDTO);

        assertThat(result.getName()).isEqualTo("Anish Updated");
        assertThat(result.getSalary()).isEqualByComparingTo("90000");
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("updateEmployee - should throw exception when employee not found")
    void updateEmployee_ShouldThrowException_WhenNotFound() {
        when(employeeRepository.findById(999L))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
            employeeService.updateEmployee(999L, testEmployeeDTO))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("999");

        verify(employeeRepository, never()).save(any(Employee.class));
    }

    // ================================================================
    // DELETE EMPLOYEE TESTS
    // ================================================================

    @Test
    @DisplayName("deleteEmployee - should delete when employee exists")
    void deleteEmployee_ShouldDelete_WhenExists() {
        when(employeeRepository.existsById(1L)).thenReturn(true);
        // void method — no return to mock

        // Should not throw any exception
        assertThatNoException().isThrownBy(() ->
            employeeService.deleteEmployee(1L));

        // Verify deleteById was called with correct ID
        verify(employeeRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("deleteEmployee - should throw exception when not found")
    void deleteEmployee_ShouldThrowException_WhenNotFound() {
        when(employeeRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() ->
            employeeService.deleteEmployee(999L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("999");

        // Verify deleteById was NEVER called
        verify(employeeRepository, never()).deleteById(any());
    }
}
