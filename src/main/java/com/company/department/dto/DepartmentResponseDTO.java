package com.company.department.dto;

public class DepartmentResponseDTO {
    private Long id;
    private String name;
    private String description;
    private int employeeCount;

    public DepartmentResponseDTO() {}

    public DepartmentResponseDTO(Long id, String name, String description, int employeeCount) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.employeeCount = employeeCount;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getEmployeeCount() { return employeeCount; }
    public void setEmployeeCount(int employeeCount) { this.employeeCount = employeeCount; }
}
