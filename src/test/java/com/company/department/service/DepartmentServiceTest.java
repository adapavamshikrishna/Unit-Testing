package com.company.department.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

import com.company.department.dto.DepartmentRequestDTO;
import com.company.department.dto.DepartmentResponseDTO;
import com.company.department.entity.Department;
import com.company.department.repository.DepartmentRepository;
import com.company.exception.BadRequestException;
import com.company.exception.DuplicateResourceException;
import com.company.exception.ResourceNotFoundException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private DepartmentService departmentService;

    private DepartmentRequestDTO request;

    @BeforeEach
    void setUp() {
        request = new DepartmentRequestDTO();
        request.setName("Engineering");
        request.setDescription("Software development team");
    }

    @Test
    void createDepartment_shouldSaveAndReturnResponse_whenNameIsUnique() {
        when(departmentRepository.existsByNameIgnoreCase("Engineering")).thenReturn(false);
        when(departmentRepository.save(any(Department.class))).thenAnswer(invocation -> {
            Department department = invocation.getArgument(0);
            department.setId(10L);
            return department;
        });

        DepartmentResponseDTO response = departmentService.create(request);

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals("Engineering", response.getName());
        assertEquals("Software development team", response.getDescription());
        verify(departmentRepository).save(any(Department.class));
    }

    @Test
    void createDepartment_shouldThrowDuplicateResourceException_whenNameAlreadyExists() {
        when(departmentRepository.existsByNameIgnoreCase("Engineering")).thenReturn(true);

        assertThrows(DuplicateResourceException.class,
                () -> departmentService.create(request));

        verify(departmentRepository, never()).save(any(Department.class));
    }

    @Test
    void findDepartment_shouldReturnDepartment_whenIdExists() {
        Department department = new Department("Engineering", "Software development team");
        department.setId(10L);
        when(departmentRepository.findById(10L)).thenReturn(Optional.of(department));

        DepartmentResponseDTO response = departmentService.getById(10L);

        assertEquals(10L, response.getId());
        assertEquals("Engineering", response.getName());
        verify(departmentRepository).findById(10L);
    }

    @Test
    void findDepartment_shouldThrowResourceNotFound_whenIdDoesNotExist() {
        when(departmentRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> departmentService.getById(999L));

        assertEquals("Department not found: 999", exception.getMessage());
        verify(departmentRepository).findById(999L);
    }

    @Test
    void deleteDepartment_shouldThrowBadRequest_whenDepartmentHasEmployees() {
        Department department = new Department("Engineering", "Software development team");
        department.setId(10L);
        department.getEmployees().add(new com.company.employee.entity.Employee());
        when(departmentRepository.findById(10L)).thenReturn(Optional.of(department));

        assertThrows(BadRequestException.class,
                () -> departmentService.delete(10L));

        verify(departmentRepository, never()).delete(any(Department.class));
    }
}
