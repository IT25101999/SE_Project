package com.texgarment.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "SupplierDelivery")
public class SupplierDelivery {

    @Id
    @Column(name = "id", length = 64)
    private String id;

    @Column(name = "deliveryNumber", nullable = false, unique = true, length = 50)
    private String deliveryNumber;

    @Column(name = "purchaseOrderId", length = 64, insertable = false, updatable = false)
    private String purchaseOrderId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "purchaseOrderId", nullable = false)
    @JsonIgnoreProperties({"items", "deliveries"})
    private PurchaseOrder purchaseOrder;

    @Column(name = "supplierId", length = 64, insertable = false, updatable = false)
    private String supplierId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "supplierId", nullable = false)
    @JsonIgnoreProperties({"materials", "purchaseOrders", "deliveries"})
    private Supplier supplier;

    @CreationTimestamp
    @Column(name = "deliveryDate", nullable = false)
    private LocalDateTime deliveryDate;

    @Column(name = "receivedByUserId", length = 64, insertable = false, updatable = false)
    private String receivedByUserId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "receivedByUserId")
    private User receivedByUser;

    @Column(name = "invoiceNumber", length = 50)
    private String invoiceNumber;

    @Column(name = "trackingNumber", length = 100)
    private String trackingNumber;

    @Column(name = "status", nullable = false, length = 30)
    private String status = "RECEIVED";

    @Column(name = "qualityNotes", columnDefinition = "TEXT")
    private String qualityNotes;

    @CreationTimestamp
    @Column(name = "createdAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updatedAt", nullable = false)
    private LocalDateTime updatedAt;

    public SupplierDelivery() {}

    @PrePersist
    protected void onCreate() {
        if (id == null || id.isEmpty()) {
            id = UUID.randomUUID().toString();
        }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getDeliveryNumber() { return deliveryNumber; }
    public void setDeliveryNumber(String deliveryNumber) { this.deliveryNumber = deliveryNumber; }
    public String getPurchaseOrderId() { return purchaseOrderId; }
    public void setPurchaseOrderId(String purchaseOrderId) { this.purchaseOrderId = purchaseOrderId; }
    public PurchaseOrder getPurchaseOrder() { return purchaseOrder; }
    public void setPurchaseOrder(PurchaseOrder purchaseOrder) { this.purchaseOrder = purchaseOrder; }
    public String getSupplierId() { return supplierId; }
    public void setSupplierId(String supplierId) { this.supplierId = supplierId; }
    public Supplier getSupplier() { return supplier; }
    public void setSupplier(Supplier supplier) { this.supplier = supplier; }
    public LocalDateTime getDeliveryDate() { return deliveryDate; }
    public void setDeliveryDate(LocalDateTime deliveryDate) { this.deliveryDate = deliveryDate; }
    public String getReceivedByUserId() { return receivedByUserId; }
    public void setReceivedByUserId(String receivedByUserId) { this.receivedByUserId = receivedByUserId; }
    public User getReceivedByUser() { return receivedByUser; }
    public void setReceivedByUser(User receivedByUser) { this.receivedByUser = receivedByUser; }
    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }
    public String getTrackingNumber() { return trackingNumber; }
    public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getQualityNotes() { return qualityNotes; }
    public void setQualityNotes(String qualityNotes) { this.qualityNotes = qualityNotes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
