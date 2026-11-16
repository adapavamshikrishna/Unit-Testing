package com.company.department.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import com.company.department.dto.DepartmentRequestDTO;
import com.company.department.dto.DepartmentResponseDTO;
import com.company.department.entity.Department;
import com.company.department.mapper.DepartmentMapper;
import com.company.department.repository.DepartmentRepository;
import com.company.exception.ResourceNotFoundException;
import com.company.exception.DuplicateResourceException;
import com.company.exception.BadRequestException;

@Service
@Transactional
public class DepartmentService {
    private static final Logger log = LoggerFactory.getLogger(DepartmentService.class);
    private final DepartmentRepository departmentRepository;

    public DepartmentService(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    public DepartmentResponseDTO create(DepartmentRequestDTO request) {
        log.info("Creating department request");
        if (departmentRepository.existsByNameIgnoreCase(request.getName())) {
            log.warn("Department creation rejected because the department name already exists");
            throw new DuplicateResourceException("Department name already exists");
        }
        Department saved = departmentRepository.save(DepartmentMapper.toEntity(request));
        log.info("Department created successfully: departmentId={}", saved.getId());
        return DepartmentMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<DepartmentResponseDTO> getAll() {
        log.debug("Fetching all departments");
        List<DepartmentResponseDTO> result = departmentRepository.findAll().stream()
                .map(DepartmentMapper::toResponse).toList();
        log.info("Fetched departments: count={}", result.size());
        return result;
    }

    @Transactional(readOnly = true)
    public DepartmentResponseDTO getById(Long id) {
        log.debug("Fetching department: departmentId={}", id);
        return DepartmentMapper.toResponse(findDepartment(id));
    }

    public DepartmentResponseDTO update(Long id, DepartmentRequestDTO request) {
        log.info("Updating department: departmentId={}", id);
        Department department = findDepartment(id);
        if (!department.getName().equalsIgnoreCase(request.getName())
                && departmentRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateResourceException("Department name already exists");
        }
        DepartmentMapper.updateEntity(department, request);
        Department updated = departmentRepository.save(department);
        log.info("Department updated successfully: departmentId={}", updated.getId());
        return DepartmentMapper.toResponse(updated);
    }

    public void delete(Long id) {
        log.info("Deleting department: departmentId={}", id);
        Department department = findDepartment(id);
        if (!department.getEmployees().isEmpty()) {
            throw new BadRequestException("Cannot delete a department that has employees");
        }
        departmentRepository.delete(department);
        log.info("Department deleted successfully: departmentId={}", id);
    }

    private Department findDepartment(Long id) {
        return departmentRepository.findById(id).orElseThrow(() -> {
            log.warn("Department not found: departmentId={}", id);
            return new ResourceNotFoundException("Department not found: " + id);
        });
    }
}
