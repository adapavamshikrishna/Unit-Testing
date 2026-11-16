package com.company.employee.mapper;

import com.company.department.entity.Department;
import com.company.employee.dto.EmployeeRequestDTO;
import com.company.employee.dto.EmployeeResponseDTO;
import com.company.employee.entity.Employee;

public final class EmployeeMapper {
    private EmployeeMapper() {}

    public static Employee toEntity(EmployeeRequestDTO request, Department department) {
        Employee employee = new Employee();
        employee.setName(request.getName());
        employee.setEmail(request.getEmail());
        employee.setDesignation(request.getDesignation());
        employee.setSalary(request.getSalary());
        employee.setDepartment(department);
        return employee;
    }

    public static void updateEntity(Employee employee, EmployeeRequestDTO request, Department department) {
        employee.setName(request.getName());
        employee.setEmail(request.getEmail());
        employee.setDesignation(request.getDesignation());
        employee.setSalary(request.getSalary());
        employee.setDepartment(department);
    }

    public static EmployeeResponseDTO toResponse(Employee employee) {
        Department department = employee.getDepartment();
        return new EmployeeResponseDTO(
                employee.getId(),
                employee.getName(),
                employee.getEmail(),
                employee.getDesignation(),
                employee.getSalary(),
                department == null ? null : department.getId(),
                department == null ? null : department.getName()
        );
    }
}
