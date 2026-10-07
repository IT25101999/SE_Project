package com.texgarment.repository;

import com.texgarment.model.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, String> {
    Optional<Employee> findByEmployeeNumber(String employeeNumber);
    Optional<Employee> findByEmail(String email);
    Optional<Employee> findByUserId(String userId);

    @Query("SELECT e FROM Employee e WHERE " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(e.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(e.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(e.employeeNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(e.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(e.designation) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:departmentId IS NULL OR :departmentId = '' OR e.departmentId = :departmentId) AND " +
           "(:status IS NULL OR :status = '' OR e.status = :status)")
    Page<Employee> findFiltered(@Param("search") String search,
                                @Param("departmentId") String departmentId,
                                @Param("status") String status,
                                Pageable pageable);
}
