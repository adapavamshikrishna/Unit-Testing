package com.company.department.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import com.company.department.dto.DepartmentRequestDTO;
import com.company.department.dto.DepartmentResponseDTO;
import com.company.department.service.DepartmentService;

@RestController
@Tag(name = "Departments", description = "Create, view, update and delete departments")
@RequestMapping("/api/departments")
public class DepartmentController {
    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @Operation(summary = "Create department", description = "Creates a department using the supplied request DTO.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Department created"),
        @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(schema = @Schema(implementation = com.company.exception.ApiErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "Department name already exists", content = @Content(schema = @Schema(implementation = com.company.exception.ApiErrorResponse.class)))
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DepartmentResponseDTO create(@Valid @RequestBody DepartmentRequestDTO request) {
        return departmentService.create(request);
    }

    @Operation(summary = "Get all departments", description = "Returns all departments as response DTOs.")
    @ApiResponse(responseCode = "200", description = "Departments returned")
    @GetMapping
    public List<DepartmentResponseDTO> getAll() {
        return departmentService.getAll();
    }

    @Operation(summary = "Get department by ID", description = "Returns one department by its database ID.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Department found"),
        @ApiResponse(responseCode = "404", description = "Department not found", content = @Content(schema = @Schema(implementation = com.company.exception.ApiErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public DepartmentResponseDTO getById(@PathVariable Long id) {
        return departmentService.getById(id);
    }

    @Operation(summary = "Update department", description = "Updates department name and description.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Department updated"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "404", description = "Department not found")
    })
    @PutMapping("/{id}")
    public DepartmentResponseDTO update(@PathVariable Long id,
                                         @Valid @RequestBody DepartmentRequestDTO request) {
        return departmentService.update(id, request);
    }

    @Operation(summary = "Delete department", description = "Deletes a department only when it has no employees.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Department deleted"),
        @ApiResponse(responseCode = "400", description = "Department still has employees"),
        @ApiResponse(responseCode = "404", description = "Department not found")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        departmentService.delete(id);
    }
}
