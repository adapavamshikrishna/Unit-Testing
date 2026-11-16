package com.company.department.mapper;

import com.company.department.dto.DepartmentRequestDTO;
import com.company.department.dto.DepartmentResponseDTO;
import com.company.department.entity.Department;

public final class DepartmentMapper {
    private DepartmentMapper() {}

    public static Department toEntity(DepartmentRequestDTO request) {
        Department department = new Department();
        department.setName(request.getName());
        department.setDescription(request.getDescription());
        return department;
    }

    public static void updateEntity(Department department, DepartmentRequestDTO request) {
        department.setName(request.getName());
        department.setDescription(request.getDescription());
    }

    public static DepartmentResponseDTO toResponse(Department department) {
        int employeeCount = department.getEmployees() == null ? 0 : department.getEmployees().size();
        return new DepartmentResponseDTO(
                department.getId(),
                department.getName(),
                department.getDescription(),
                employeeCount
        );
    }
}
