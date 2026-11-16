package com.company.employee.controller;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import com.company.employee.dto.EmployeeRequestDTO;
import com.company.employee.dto.EmployeeResponseDTO;
import com.company.employee.service.EmployeeService;

@RestController
@Tag(name = "Employees", description = "Employee CRUD, pagination, sorting, search and filtering")
@RequestMapping("/api/employees")
public class EmployeeController {
    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @Operation(summary = "Create employee", description = "Creates an employee. Supply an existing departmentId.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Employee created"),
        @ApiResponse(responseCode = "400", description = "Validation failed", content = @Content(schema = @Schema(implementation = com.company.exception.ApiErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Department not found"),
        @ApiResponse(responseCode = "409", description = "Email already exists")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmployeeResponseDTO create(@Valid @RequestBody EmployeeRequestDTO request) {
        return employeeService.create(request);
    }

    // Example: GET /api/employees?page=0&size=5&sort=name,asc
    @Operation(summary = "List employees", description = "Returns employees as a paginated response. Page index starts at zero. Page size must be 1–100.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Page of employees returned"),
        @ApiResponse(responseCode = "400", description = "Invalid page or size")
    })
    @GetMapping
    public Page<EmployeeResponseDTO> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id,asc") String[] sort) {

        if (page < 0) {
            throw new com.company.exception.BadRequestException("Page index must be zero or greater");
        }
        if (size < 1 || size > 100) {
            throw new com.company.exception.BadRequestException("Page size must be between 1 and 100");
        }

        Sort sortOrder = Sort.by(
                "desc".equalsIgnoreCase(sort.length > 1 ? sort[1] : "asc")
                        ? Sort.Direction.DESC : Sort.Direction.ASC,
                sort[0]
        );
        Pageable pageable = PageRequest.of(page, size, sortOrder);
        return employeeService.getAll(pageable);
    }

    @Operation(summary = "Search employees by name", description = "Case-insensitive partial match on employee name.")
    @ApiResponse(responseCode = "200", description = "Matching employees returned")
    // Search by partial employee name: GET /api/employees/search/by-name?name=arun
    @GetMapping("/search/by-name")
    public List<EmployeeResponseDTO> searchByName(@RequestParam String name) {
        if (name.isBlank()) {
            throw new com.company.exception.BadRequestException("Name search value must not be blank");
        }
        return employeeService.searchByName(name.trim());
    }

    @Operation(summary = "Search employees by designation", description = "Case-insensitive partial match on designation.")
    @ApiResponse(responseCode = "200", description = "Matching employees returned")
    // Search by designation: GET /api/employees/search/by-designation?designation=developer
    @GetMapping("/search/by-designation")
    public List<EmployeeResponseDTO> searchByDesignation(@RequestParam String designation) {
        if (designation.isBlank()) {
            throw new com.company.exception.BadRequestException("Designation search value must not be blank");
        }
        return employeeService.searchByDesignation(designation.trim());
    }

    @Operation(summary = "Filter employees by department", description = "Returns employees belonging to the specified department ID.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Matching employees returned"),
        @ApiResponse(responseCode = "404", description = "Department not found")
    })
    // Search by department: GET /api/employees/search/by-department/1
    @GetMapping("/search/by-department/{departmentId}")
    public List<EmployeeResponseDTO> searchByDepartment(@PathVariable Long departmentId) {
        if (departmentId < 1) {
            throw new com.company.exception.BadRequestException("Department ID must be greater than zero");
        }
        return employeeService.searchByDepartment(departmentId);
    }

    @Operation(summary = "Search and filter employees", description = "Combines optional name, designation and department filters with pagination and sorting.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Filtered employee page returned"),
        @ApiResponse(responseCode = "400", description = "Invalid filter or pagination parameter"),
        @ApiResponse(responseCode = "404", description = "Department not found")
    })
    // Combined optional filters with pagination and sorting.
    // Example: GET /api/employees/search?name=arun&designation=developer&departmentId=1&page=0&size=5
    @GetMapping("/search")
    public Page<EmployeeResponseDTO> searchEmployees(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String designation,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id,asc") String[] sort) {

        if (name != null && name.isBlank()) {
            throw new com.company.exception.BadRequestException("Name filter must not be blank");
        }
        if (designation != null && designation.isBlank()) {
            throw new com.company.exception.BadRequestException("Designation filter must not be blank");
        }
        if (departmentId != null && departmentId < 1) {
            throw new com.company.exception.BadRequestException("Department ID must be greater than zero");
        }
        if (page < 0) {
            throw new com.company.exception.BadRequestException("Page index must be zero or greater");
        }
        if (size < 1 || size > 100) {
            throw new com.company.exception.BadRequestException("Page size must be between 1 and 100");
        }

        String sortField = sort.length > 0 && !sort[0].isBlank() ? sort[0] : "id";
        String direction = sort.length > 1 ? sort[1] : "asc";
        if (!direction.equalsIgnoreCase("asc") && !direction.equalsIgnoreCase("desc")) {
            throw new com.company.exception.BadRequestException("Sort direction must be asc or desc");
        }
        Sort sortOrder = Sort.by(
                "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC,
                sortField
        );
        Pageable pageable = PageRequest.of(page, size, sortOrder);
        return employeeService.searchEmployees(
                name == null ? null : name.trim(),
                designation == null ? null : designation.trim(),
                departmentId,
                pageable);
    }

    @Operation(summary = "Get employee by ID", description = "Returns an employee response DTO by ID.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Employee found"),
        @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    @GetMapping("/{id}")
    public EmployeeResponseDTO getById(@PathVariable Long id) {
        return employeeService.getById(id);
    }

    @Operation(summary = "Update employee", description = "Updates employee fields and department assignment.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Employee updated"),
        @ApiResponse(responseCode = "400", description = "Validation failed"),
        @ApiResponse(responseCode = "404", description = "Employee or department not found"),
        @ApiResponse(responseCode = "409", description = "Email already exists")
    })
    @PutMapping("/{id}")
    public EmployeeResponseDTO update(@PathVariable Long id,
                                      @Valid @RequestBody EmployeeRequestDTO request) {
        return employeeService.update(id, request);
    }

    @Operation(summary = "Delete employee", description = "Deletes an employee by ID.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Employee deleted"),
        @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        employeeService.delete(id);
    }
}
