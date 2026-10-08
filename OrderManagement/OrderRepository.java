package com.texgarment.repository;

import com.texgarment.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    Optional<Order> findByOrderNumber(String orderNumber);

    @Query("SELECT o FROM Order o WHERE " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(o.orderNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(o.customer.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(o.customer.companyName) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:status IS NULL OR :status = '' OR o.status = :status) AND " +
           "(:paymentStatus IS NULL OR :paymentStatus = '' OR o.paymentStatus = :paymentStatus) AND " +
           "(:customerId IS NULL OR :customerId = '' OR o.customerId = :customerId)")
    Page<Order> findFiltered(@Param("search") String search,
                             @Param("status") String status,
                             @Param("paymentStatus") String paymentStatus,
                             @Param("customerId") String customerId,
                             Pageable pageable);

    List<Order> findByStatus(String status);
    List<Order> findByCustomerId(String customerId);
}
