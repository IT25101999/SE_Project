package com.texgarment.repository;

import com.texgarment.model.ProductionStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductionStageRepository extends JpaRepository<ProductionStage, String> {
    List<ProductionStage> findByProductionRunIdOrderByStageOrderAsc(String productionRunId);
    Optional<ProductionStage> findByProductionRunIdAndStageName(String productionRunId, String stageName);
}
