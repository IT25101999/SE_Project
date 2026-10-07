package com.texgarment.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "ProductionRun")
public class ProductionRun {

    @Id
    @Column(name = "id", length = 64)
    private String id;

    @Column(name = "runCode", nullable = false, unique = true, length = 50)
    private String runCode;

    @Column(name = "orderId", length = 64, insertable = false, updatable = false)
    private String orderId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "orderId")
    @JsonIgnoreProperties({"orderItems", "productionRuns"})
    private Order order;

    @Column(name = "productId", length = 64, insertable = false, updatable = false)
    private String productId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "productId")
    private InventoryItem product;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "plannedQuantity", nullable = false)
    private Integer plannedQuantity;

    @Column(name = "producedQuantity", nullable = false)
    private Integer producedQuantity = 0;

    @Column(name = "damagedQuantity", nullable = false)
    private Integer damagedQuantity = 0;

    @Column(name = "startDate")
    private LocalDateTime startDate;

    @Column(name = "expectedCompletionDate")
    private LocalDateTime expectedCompletionDate;

    @Column(name = "actualCompletionDate")
    private LocalDateTime actualCompletionDate;

    @Column(name = "priority", nullable = false, length = 30)
    private String priority = "NORMAL";

    @Column(name = "status", nullable = false, length = 30)
    private String status = "PLANNED";

    @Column(name = "delayReason", columnDefinition = "TEXT")
    private String delayReason;

    @Column(name = "materialShortageNotes", columnDefinition = "TEXT")
    private String materialShortageNotes;

    @Column(name = "supervisorEmployeeId", length = 64, insertable = false, updatable = false)
    private String supervisorEmployeeId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "supervisorEmployeeId")
    private Employee supervisorEmployee;

    @OneToMany(mappedBy = "productionRun", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("stageOrder ASC")
    private List<ProductionStage> stages = new ArrayList<>();

    @OneToMany(mappedBy = "productionRun", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<QualityCheck> qualityChecks = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "createdAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updatedAt", nullable = false)
    private LocalDateTime updatedAt;

    public ProductionRun() {}

    @PrePersist
    protected void onCreate() {
        if (id == null || id.isEmpty()) {
            id = UUID.randomUUID().toString();
        }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getRunCode() { return runCode; }
    public void setRunCode(String runCode) { this.runCode = runCode; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public InventoryItem getProduct() { return product; }
    public void setProduct(InventoryItem product) { this.product = product; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getPlannedQuantity() { return plannedQuantity; }
    public void setPlannedQuantity(Integer plannedQuantity) { this.plannedQuantity = plannedQuantity; }
    public Integer getProducedQuantity() { return producedQuantity; }
    public void setProducedQuantity(Integer producedQuantity) { this.producedQuantity = producedQuantity; }
    public Integer getDamagedQuantity() { return damagedQuantity; }
    public void setDamagedQuantity(Integer damagedQuantity) { this.damagedQuantity = damagedQuantity; }
    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }
    public LocalDateTime getExpectedCompletionDate() { return expectedCompletionDate; }
    public void setExpectedCompletionDate(LocalDateTime expectedCompletionDate) { this.expectedCompletionDate = expectedCompletionDate; }
    public LocalDateTime getActualCompletionDate() { return actualCompletionDate; }
    public void setActualCompletionDate(LocalDateTime actualCompletionDate) { this.actualCompletionDate = actualCompletionDate; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelayReason() { return delayReason; }
    public void setDelayReason(String delayReason) { this.delayReason = delayReason; }
    public String getMaterialShortageNotes() { return materialShortageNotes; }
    public void setMaterialShortageNotes(String materialShortageNotes) { this.materialShortageNotes = materialShortageNotes; }
    public String getSupervisorEmployeeId() { return supervisorEmployeeId; }
    public void setSupervisorEmployeeId(String supervisorEmployeeId) { this.supervisorEmployeeId = supervisorEmployeeId; }
    public Employee getSupervisorEmployee() { return supervisorEmployee; }
    public void setSupervisorEmployee(Employee supervisorEmployee) { this.supervisorEmployee = supervisorEmployee; }
    public List<ProductionStage> getStages() { return stages; }
    public void setStages(List<ProductionStage> stages) { this.stages = stages; }
    public List<QualityCheck> getQualityChecks() { return qualityChecks; }
    public void setQualityChecks(List<QualityCheck> qualityChecks) { this.qualityChecks = qualityChecks; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
