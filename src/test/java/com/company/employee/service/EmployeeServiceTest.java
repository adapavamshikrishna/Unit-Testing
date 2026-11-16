package com.company.employee.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Optional;

import com.company.department.client.DepartmentApiClient;
import com.company.department.dto.DepartmentResponseDTO;
import com.company.department.entity.Department;
import com.company.department.repository.DepartmentRepository;
import com.company.employee.dto.EmployeeRequestDTO;
import com.company.employee.dto.EmployeeResponseDTO;
import com.company.employee.entity.Employee;
import com.company.employee.repository.EmployeeRepository;
import com.company.exception.DuplicateResourceException;
import com.company.exception.ResourceNotFoundException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private DepartmentApiClient departmentApiClient;

    @InjectMocks
    private EmployeeService employeeService;

    private Department department;
    private EmployeeRequestDTO request;

    @BeforeEach
    void setUp() {
        department = new Department("Engineering", "Software development team");
        department.setId(1L);

        request = new EmployeeRequestDTO();
        request.setName("Arun Kumar");
        request.setEmail("arun@example.com");
        request.setDesignation("Java Developer");
        request.setSalary(new BigDecimal("45000.00"));
        request.setDepartmentId(1L);
    }

    @Test
    void createEmployee_shouldSaveAndReturnResponse_whenRequestIsValid() {
        when(employeeRepository.existsByEmailIgnoreCase("arun@example.com")).thenReturn(false);
        when(departmentApiClient.getDepartmentById(1L))
                .thenReturn(new DepartmentResponseDTO(1L, "Engineering", "Software development team", 0));
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> {
            Employee employee = invocation.getArgument(0);
            employee.setId(100L);
            return employee;
        });

        EmployeeResponseDTO response = employeeService.create(request);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals("Arun Kumar", response.getName());
        assertEquals("arun@example.com", response.getEmail());
        assertEquals("Java Developer", response.getDesignation());
        assertEquals(1L, response.getDepartmentId());
        assertEquals("Engineering", response.getDepartmentName());

        verify(employeeRepository).existsByEmailIgnoreCase("arun@example.com");
        verify(departmentApiClient).getDepartmentById(1L);
        verify(departmentRepository).findById(1L);
        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    void createEmployee_shouldThrowDuplicateResourceException_whenEmailAlreadyExists() {
        when(employeeRepository.existsByEmailIgnoreCase("arun@example.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class,
                () -> employeeService.create(request));

        verify(employeeRepository).existsByEmailIgnoreCase("arun@example.com");
        verifyNoInteractions(departmentApiClient, departmentRepository);
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void createEmployee_shouldThrowResourceNotFound_whenDepartmentDoesNotExistRemotely() {
        when(employeeRepository.existsByEmailIgnoreCase("arun@example.com")).thenReturn(false);
        when(departmentApiClient.getDepartmentById(1L))
                .thenThrow(new ResourceNotFoundException("Department not found: 1"));

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> employeeService.create(request));

        assertEquals("Department not found: 1", exception.getMessage());
        verify(departmentApiClient).getDepartmentById(1L);
        verify(departmentRepository, never()).findById(any());
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void createEmployee_shouldThrowResourceNotFound_whenDepartmentMissingFromLocalStore() {
        when(employeeRepository.existsByEmailIgnoreCase("arun@example.com")).thenReturn(false);
        when(departmentApiClient.getDepartmentById(1L))
                .thenReturn(new DepartmentResponseDTO(1L, "Engineering", "Software development team", 0));
        when(departmentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> employeeService.create(request));

        verify(departmentApiClient).getDepartmentById(1L);
        verify(departmentRepository).findById(1L);
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void findEmployee_shouldReturnEmployee_whenIdExists() {
        Employee employee = new Employee(
                "Arun Kumar", "arun@example.com", "Java Developer",
                new BigDecimal("45000.00"), department);
        employee.setId(100L);
        when(employeeRepository.findById(100L)).thenReturn(Optional.of(employee));

        EmployeeResponseDTO response = employeeService.getById(100L);

        assertEquals(100L, response.getId());
        assertEquals("Arun Kumar", response.getName());
        assertEquals("Engineering", response.getDepartmentName());
        verify(employeeRepository).findById(100L);
    }

    @Test
    void findEmployee_shouldThrowResourceNotFound_whenIdDoesNotExist() {
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> employeeService.getById(999L));

        assertEquals("Employee not found: 999", exception.getMessage());
        verify(employeeRepository).findById(999L);
    }
}
