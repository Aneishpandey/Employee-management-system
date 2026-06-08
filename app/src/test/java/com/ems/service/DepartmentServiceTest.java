package com.ems.service;

import com.ems.dto.DepartmentDTO;
import com.ems.exception.ResourceNotFoundException;
import com.ems.model.Department;
import com.ems.repository.DepartmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DepartmentService Unit Tests")
class DepartmentServiceTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private DepartmentService departmentService;

    private Department testDepartment;
    private DepartmentDTO testDepartmentDTO;

    @BeforeEach
    void setUp() {
        testDepartment = new Department();
        testDepartment.setId(1L);
        testDepartment.setName("Engineering");
        testDepartment.setDescription("Software Engineering");
        testDepartment.setEmployees(List.of()); // empty list

        testDepartmentDTO = new DepartmentDTO();
        testDepartmentDTO.setName("Engineering");
        testDepartmentDTO.setDescription("Software Engineering");
    }

    @Test
    @DisplayName("getAllDepartments - should return all departments")
    void getAllDepartments_ShouldReturnAll() {
        when(departmentRepository.findAll())
            .thenReturn(List.of(testDepartment));

        List<DepartmentDTO> result = departmentService.getAllDepartments();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Engineering");
        assertThat(result.get(0).getEmployeeCount()).isZero();
        verify(departmentRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("createDepartment - should create when name is unique")
    void createDepartment_ShouldCreate_WhenNameUnique() {
        when(departmentRepository.existsByName("Engineering"))
            .thenReturn(false);
        when(departmentRepository.save(any(Department.class)))
            .thenReturn(testDepartment);

        DepartmentDTO result =
            departmentService.createDepartment(testDepartmentDTO);

        assertThat(result.getName()).isEqualTo("Engineering");
        verify(departmentRepository, times(1)).save(any(Department.class));
    }

    @Test
    @DisplayName("createDepartment - should throw when name already exists")
    void createDepartment_ShouldThrow_WhenNameExists() {
        when(departmentRepository.existsByName("Engineering"))
            .thenReturn(true);

        assertThatThrownBy(() ->
            departmentService.createDepartment(testDepartmentDTO))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Engineering");

        verify(departmentRepository, never()).save(any(Department.class));
    }

    @Test
    @DisplayName("updateDepartment - should update when department exists")
    void updateDepartment_ShouldUpdate_WhenExists() {
        DepartmentDTO updateDTO = new DepartmentDTO();
        updateDTO.setName("Engineering Updated");
        updateDTO.setDescription("Updated Description");

        Department updatedDept = new Department();
        updatedDept.setId(1L);
        updatedDept.setName("Engineering Updated");
        updatedDept.setDescription("Updated Description");
        updatedDept.setEmployees(List.of());

        when(departmentRepository.findById(1L))
            .thenReturn(Optional.of(testDepartment));
        when(departmentRepository.save(any(Department.class)))
            .thenReturn(updatedDept);

        DepartmentDTO result =
            departmentService.updateDepartment(1L, updateDTO);

        assertThat(result.getName()).isEqualTo("Engineering Updated");
        verify(departmentRepository, times(1)).save(any(Department.class));
    }

    @Test
    @DisplayName("updateDepartment - should throw when not found")
    void updateDepartment_ShouldThrow_WhenNotFound() {
        when(departmentRepository.findById(999L))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
            departmentService.updateDepartment(999L, testDepartmentDTO))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("999");
    }

    @Test
    @DisplayName("deleteDepartment - should delete when exists")
    void deleteDepartment_ShouldDelete_WhenExists() {
        when(departmentRepository.existsById(1L)).thenReturn(true);

        assertThatNoException().isThrownBy(() ->
            departmentService.deleteDepartment(1L));

        verify(departmentRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("deleteDepartment - should throw when not found")
    void deleteDepartment_ShouldThrow_WhenNotFound() {
        when(departmentRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() ->
            departmentService.deleteDepartment(999L))
            .isInstanceOf(ResourceNotFoundException.class);

        verify(departmentRepository, never()).deleteById(any());
    }
}
