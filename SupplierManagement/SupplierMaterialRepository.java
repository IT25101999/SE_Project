package com.texgarment.repository;

import com.texgarment.model.SupplierMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierMaterialRepository extends JpaRepository<SupplierMaterial, String> {
    List<SupplierMaterial> findBySupplierId(String supplierId);
    Optional<SupplierMaterial> findBySupplierIdAndMaterialName(String supplierId, String materialName);
}
