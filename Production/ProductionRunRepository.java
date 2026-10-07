package com.texgarment.repository;

import com.texgarment.model.ProductionRun;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductionRunRepository extends JpaRepository<ProductionRun, String> {
    Optional<ProductionRun> findByRunCode(String runCode);

    @Query("SELECT r FROM ProductionRun r WHERE " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(r.runCode) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(r.title) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:status IS NULL OR :status = '' OR r.status = :status) AND " +
           "(:priority IS NULL OR :priority = '' OR r.priority = :priority)")
    Page<ProductionRun> findFiltered(@Param("search") String search,
                                     @Param("status") String status,
                                     @Param("priority") String priority,
                                     Pageable pageable);

    List<ProductionRun> findByStatus(String status);
    List<ProductionRun> findByOrderId(String orderId);
}
