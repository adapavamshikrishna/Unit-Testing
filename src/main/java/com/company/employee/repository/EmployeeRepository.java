package com.company.employee.repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.company.employee.entity.Employee;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    boolean existsByEmailIgnoreCase(String email);

    List<Employee> findByNameContainingIgnoreCase(String name);

    List<Employee> findByDesignationContainingIgnoreCase(String designation);

    List<Employee> findByDepartment_Id(Long departmentId);

    @Query("""
            SELECT e FROM Employee e
            WHERE (:name IS NULL OR LOWER(e.name) LIKE LOWER(CONCAT('%', :name, '%')))
              AND (:designation IS NULL OR LOWER(e.designation) LIKE LOWER(CONCAT('%', :designation, '%')))
              AND (:departmentId IS NULL OR e.department.id = :departmentId)
            """)
    Page<Employee> searchEmployees(
            @Param("name") String name,
            @Param("designation") String designation,
            @Param("departmentId") Long departmentId,
            Pageable pageable);
}
