package com.company.employee.service;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import com.company.department.entity.Department;
import com.company.department.client.DepartmentApiClient;
import com.company.department.repository.DepartmentRepository;
import com.company.employee.dto.EmployeeRequestDTO;
import com.company.employee.dto.EmployeeResponseDTO;
import com.company.employee.entity.Employee;
import com.company.employee.mapper.EmployeeMapper;
import com.company.employee.repository.EmployeeRepository;
import com.company.exception.ResourceNotFoundException;
import com.company.exception.DuplicateResourceException;

@Service
@Transactional
public class EmployeeService {
    private static final Logger log = LoggerFactory.getLogger(EmployeeService.class);
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final DepartmentApiClient departmentApiClient;

    public EmployeeService(EmployeeRepository employeeRepository,
                           DepartmentRepository departmentRepository,
                           DepartmentApiClient departmentApiClient) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.departmentApiClient = departmentApiClient;
    }

    public EmployeeResponseDTO create(EmployeeRequestDTO request) {
        log.info("Creating employee request");
        if (employeeRepository.existsByEmailIgnoreCase(request.getEmail())) {
            log.warn("Employee creation rejected because the email is already registered");
            throw new DuplicateResourceException("Employee email already exists");
        }
        Department department = validateRemoteDepartmentAndLoadLocally(request.getDepartmentId());
        Employee saved = employeeRepository.save(EmployeeMapper.toEntity(request, department));
        log.info("Employee created successfully: employeeId={}", saved.getId());
        return EmployeeMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<EmployeeResponseDTO> getAll() {
        log.debug("Fetching all employees");
        List<EmployeeResponseDTO> result = employeeRepository.findAll().stream()
                .map(EmployeeMapper::toResponse).toList();
        log.info("Fetched employees: count={}", result.size());
        return result;
    }

    @Transactional(readOnly = true)
    public Page<EmployeeResponseDTO> getAll(Pageable pageable) {
        log.debug("Fetching employees page: page={}, size={}",
                pageable.getPageNumber(), pageable.getPageSize());
        Page<EmployeeResponseDTO> result = employeeRepository.findAll(pageable)
                .map(EmployeeMapper::toResponse);
        log.info("Fetched employee page: page={}, returned={}, totalElements={}",
                result.getNumber(), result.getNumberOfElements(), result.getTotalElements());
        return result;
    }

    @Transactional(readOnly = true)
    public List<EmployeeResponseDTO> searchByName(String name) {
        log.info("Searching employees by name");
        List<EmployeeResponseDTO> result = employeeRepository.findByNameContainingIgnoreCase(name).stream()
                .map(EmployeeMapper::toResponse).toList();
        log.info("Employee name search completed: resultCount={}", result.size());
        return result;
    }

    @Transactional(readOnly = true)
    public List<EmployeeResponseDTO> searchByDesignation(String designation) {
        log.info("Searching employees by designation");
        List<EmployeeResponseDTO> result = employeeRepository.findByDesignationContainingIgnoreCase(designation).stream()
                .map(EmployeeMapper::toResponse).toList();
        log.info("Employee designation search completed: resultCount={}", result.size());
        return result;
    }

    @Transactional(readOnly = true)
    public List<EmployeeResponseDTO> searchByDepartment(Long departmentId) {
        log.info("Filtering employees by departmentId={}", departmentId);
        departmentApiClient.getDepartmentById(departmentId);
        List<EmployeeResponseDTO> result = employeeRepository.findByDepartment_Id(departmentId).stream()
                .map(EmployeeMapper::toResponse).toList();
        log.info("Department employee filter completed: departmentId={}, resultCount={}",
                departmentId, result.size());
        return result;
    }

    @Transactional(readOnly = true)
    public Page<EmployeeResponseDTO> searchEmployees(
            String name, String designation, Long departmentId, Pageable pageable) {
        if (departmentId != null) {
            departmentApiClient.getDepartmentById(departmentId);
        }
        return employeeRepository.searchEmployees(name, designation, departmentId, pageable)
                .map(EmployeeMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public EmployeeResponseDTO getById(Long id) {
        log.debug("Fetching employee: employeeId={}", id);
        return EmployeeMapper.toResponse(findEmployee(id));
    }

    public EmployeeResponseDTO update(Long id, EmployeeRequestDTO request) {
        log.info("Updating employee: employeeId={}", id);
        Employee employee = findEmployee(id);
        if (!employee.getEmail().equalsIgnoreCase(request.getEmail())
                && employeeRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new DuplicateResourceException("Employee email already exists");
        }
        Department department = validateRemoteDepartmentAndLoadLocally(request.getDepartmentId());
        EmployeeMapper.updateEntity(employee, request, department);
        Employee updated = employeeRepository.save(employee);
        log.info("Employee updated successfully: employeeId={}", updated.getId());
        return EmployeeMapper.toResponse(updated);
    }

    public void delete(Long id) {
        log.info("Deleting employee: employeeId={}", id);
        employeeRepository.delete(findEmployee(id));
        log.info("Employee deleted successfully: employeeId={}", id);
    }

    private Employee findEmployee(Long id) {
        return employeeRepository.findById(id).orElseThrow(() -> {
            log.warn("Employee not found: employeeId={}", id);
            return new ResourceNotFoundException("Employee not found: " + id);
        });
    }

    private Department validateRemoteDepartmentAndLoadLocally(Long id) {
        // First call the Department REST API. If it is down, the global handler
        // returns a clear 503/504/502 response instead of an unclear database error.
        departmentApiClient.getDepartmentById(id);

        // This single-application sample still stores the department relation locally
        // to preserve the existing JPA foreign-key mapping.
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + id));
    }
}
