package com.texgarment.repository;

import com.texgarment.model.SupplierDelivery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierDeliveryRepository extends JpaRepository<SupplierDelivery, String> {
    Optional<SupplierDelivery> findByDeliveryNumber(String deliveryNumber);
    List<SupplierDelivery> findByPurchaseOrderId(String purchaseOrderId);
    List<SupplierDelivery> findBySupplierId(String supplierId);

    @Query("SELECT d FROM SupplierDelivery d WHERE " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(d.deliveryNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(d.supplier.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(d.invoiceNumber) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:status IS NULL OR :status = '' OR d.status = :status)")
    Page<SupplierDelivery> findFiltered(@Param("search") String search,
                                        @Param("status") String status,
                                        Pageable pageable);
}
