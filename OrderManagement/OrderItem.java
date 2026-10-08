package com.texgarment.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "OrderItem")
public class OrderItem {

    @Id
    @Column(name = "id", length = 64)
    private String id;

    @Column(name = "orderId", length = 64, insertable = false, updatable = false)
    private String orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orderId", nullable = false)
    @JsonIgnore
    private Order order;

    @Column(name = "itemId", length = 64, insertable = false, updatable = false)
    private String itemId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "itemId")
    private InventoryItem item;

    @Column(name = "itemName", nullable = false, length = 200)
    private String itemName;

    @Column(name = "itemDescription", columnDefinition = "TEXT")
    private String itemDescription;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unitPrice", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "totalPrice", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalPrice;

    @Column(name = "specifications", columnDefinition = "TEXT")
    private String specifications;

    @CreationTimestamp
    @Column(name = "createdAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updatedAt", nullable = false)
    private LocalDateTime updatedAt;

    public OrderItem() {}

    @PrePersist
    protected void onCreate() {
        if (id == null || id.isEmpty()) {
            id = UUID.randomUUID().toString();
        }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }
    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }
    public InventoryItem getItem() { return item; }
    public void setItem(InventoryItem item) { this.item = item; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public String getItemDescription() { return itemDescription; }
    public void setItemDescription(String itemDescription) { this.itemDescription = itemDescription; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
    public String getSpecifications() { return specifications; }
    public void setSpecifications(String specifications) { this.specifications = specifications; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
