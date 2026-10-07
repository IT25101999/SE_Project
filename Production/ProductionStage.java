package com.texgarment.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ProductionStage", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"productionRunId", "stageName"})
})
public class ProductionStage {

    @Id
    @Column(name = "id", length = 64)
    private String id;

    @Column(name = "productionRunId", length = 64, insertable = false, updatable = false)
    private String productionRunId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "productionRunId", nullable = false)
    @JsonIgnore
    private ProductionRun productionRun;

    @Column(name = "stageName", nullable = false, length = 50)
    private String stageName;

    @Column(name = "stageOrder", nullable = false)
    private Integer stageOrder = 1;

    @Column(name = "status", nullable = false, length = 30)
    private String status = "PENDING";

    @Column(name = "startedAt")
    private LocalDateTime startedAt;

    @Column(name = "completedAt")
    private LocalDateTime completedAt;

    @Column(name = "assignedTeam", length = 100)
    private String assignedTeam;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "createdAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updatedAt", nullable = false)
    private LocalDateTime updatedAt;

    public ProductionStage() {}

    @PrePersist
    protected void onCreate() {
        if (id == null || id.isEmpty()) {
            id = UUID.randomUUID().toString();
        }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getProductionRunId() { return productionRunId; }
    public void setProductionRunId(String productionRunId) { this.productionRunId = productionRunId; }
    public ProductionRun getProductionRun() { return productionRun; }
    public void setProductionRun(ProductionRun productionRun) { this.productionRun = productionRun; }
    public String getStageName() { return stageName; }
    public void setStageName(String stageName) { this.stageName = stageName; }
    public Integer getStageOrder() { return stageOrder; }
    public void setStageOrder(Integer stageOrder) { this.stageOrder = stageOrder; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    public String getAssignedTeam() { return assignedTeam; }
    public void setAssignedTeam(String assignedTeam) { this.assignedTeam = assignedTeam; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
