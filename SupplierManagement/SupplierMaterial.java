package com.texgarment.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "SupplierMaterial", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"supplierId", "materialName"})
})
public class SupplierMaterial {

    @Id
    @Column(name = "id", length = 64)
    private String id;

    @Column(name = "supplierId", length = 64, insertable = false, updatable = false)
    private String supplierId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "supplierId", nullable = false)
    @JsonIgnoreProperties({"materials", "purchaseOrders", "deliveries"})
    private Supplier supplier;

    @Column(name = "materialName", nullable = false, length = 200)
    private String materialName;

    @Column(name = "materialCode", length = 50)
    private String materialCode;

    @Column(name = "category", length = 100)
    private String category;

    @Column(name = "unitOfMeasure", nullable = false, length = 30)
    private String unitOfMeasure = "METERS";

    @Column(name = "unitPrice", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "leadTimeDays", nullable = false)
    private Integer leadTimeDays = 7;

    @Column(name = "minOrderQuantity", nullable = false)
    private Integer minOrderQuantity = 10;

    @CreationTimestamp
    @Column(name = "createdAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updatedAt", nullable = false)
    private LocalDateTime updatedAt;

    public SupplierMaterial() {}

    @PrePersist
    protected void onCreate() {
        if (id == null || id.isEmpty()) {
            id = UUID.randomUUID().toString();
        }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getSupplierId() { return supplierId; }
    public void setSupplierId(String supplierId) { this.supplierId = supplierId; }
    public Supplier getSupplier() { return supplier; }
    public void setSupplier(Supplier supplier) { this.supplier = supplier; }
    public String getMaterialName() { return materialName; }
    public void setMaterialName(String materialName) { this.materialName = materialName; }
    public String getMaterialCode() { return materialCode; }
    public void setMaterialCode(String materialCode) { this.materialCode = materialCode; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getUnitOfMeasure() { return unitOfMeasure; }
    public void setUnitOfMeasure(String unitOfMeasure) { this.unitOfMeasure = unitOfMeasure; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public Integer getLeadTimeDays() { return leadTimeDays; }
    public void setLeadTimeDays(Integer leadTimeDays) { this.leadTimeDays = leadTimeDays; }
    public Integer getMinOrderQuantity() { return minOrderQuantity; }
    public void setMinOrderQuantity(Integer minOrderQuantity) { this.minOrderQuantity = minOrderQuantity; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
