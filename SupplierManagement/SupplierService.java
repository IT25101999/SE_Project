package com.texgarment.service;

import com.texgarment.exception.AppException;
import com.texgarment.model.*;
import com.texgarment.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final SupplierMaterialRepository supplierMaterialRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderItemRepository purchaseOrderItemRepository;
    private final SupplierDeliveryRepository supplierDeliveryRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final StockMovementRepository stockMovementRepository;
    private final NotificationRepository notificationRepository;

    public SupplierService(SupplierRepository supplierRepository,
                           SupplierMaterialRepository supplierMaterialRepository,
                           PurchaseOrderRepository purchaseOrderRepository,
                           PurchaseOrderItemRepository purchaseOrderItemRepository,
                           SupplierDeliveryRepository supplierDeliveryRepository,
                           InventoryItemRepository inventoryItemRepository,
                           StockMovementRepository stockMovementRepository,
                           NotificationRepository notificationRepository) {
        this.supplierRepository = supplierRepository;
        this.supplierMaterialRepository = supplierMaterialRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.purchaseOrderItemRepository = purchaseOrderItemRepository;
        this.supplierDeliveryRepository = supplierDeliveryRepository;
        this.inventoryItemRepository = inventoryItemRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.notificationRepository = notificationRepository;
    }

    // ==========================================
    // SUPPLIERS
    // ==========================================

    public Map<String, Object> getAllSuppliers(String search, String status, int page, int limit) {
        PageRequest pageRequest = PageRequest.of(Math.max(0, page - 1), limit, Sort.by(Sort.Direction.ASC, "name"));
        Page<Supplier> supplierPage = supplierRepository.findFiltered(search, status, pageRequest);

        Map<String, Object> pagination = new HashMap<>();
        pagination.put("total", supplierPage.getTotalElements());
        pagination.put("page", page);
        pagination.put("limit", limit);
        pagination.put("totalPages", supplierPage.getTotalPages());

        Map<String, Object> response = new HashMap<>();
        response.put("suppliers", supplierPage.getContent());
        response.put("pagination", pagination);
        return response;
    }

    public Supplier getSupplierById(String id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new AppException("Supplier not found.", HttpStatus.NOT_FOUND));
    }

    @Transactional
    public Supplier createSupplier(Map<String, Object> data) {
        String code = (String) data.get("supplierCode");
        if (code == null || code.isEmpty()) {
            code = "SUP-" + (int)(1000 + Math.random() * 9000);
        }

        Supplier supplier = new Supplier();
        supplier.setSupplierCode(code);
        supplier.setName((String) data.get("name"));
        supplier.setContactPerson((String) data.get("contactPerson"));
        supplier.setEmail((String) data.get("email"));
        supplier.setPhone((String) data.get("phone"));
        supplier.setAddress((String) data.get("address"));
        supplier.setCity((String) data.get("city"));
        supplier.setCountry((String) data.getOrDefault("country", "Sri Lanka"));
        supplier.setTaxNumber((String) data.get("taxNumber"));
        supplier.setStatus((String) data.getOrDefault("status", "ACTIVE"));
        supplier.setPaymentTerms((String) data.getOrDefault("paymentTerms", "Net 30"));

        return supplierRepository.save(supplier);
    }

    @Transactional
    public Supplier updateSupplier(String id, Map<String, Object> data) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new AppException("Supplier not found.", HttpStatus.NOT_FOUND));

        if (data.containsKey("name") && data.get("name") != null) supplier.setName((String) data.get("name"));
        if (data.containsKey("contactPerson")) supplier.setContactPerson((String) data.get("contactPerson"));
        if (data.containsKey("email") && data.get("email") != null) supplier.setEmail((String) data.get("email"));
        if (data.containsKey("phone")) supplier.setPhone((String) data.get("phone"));
        if (data.containsKey("address")) supplier.setAddress((String) data.get("address"));
        if (data.containsKey("city")) supplier.setCity((String) data.get("city"));
        if (data.containsKey("status") && data.get("status") != null) supplier.setStatus((String) data.get("status"));
        if (data.containsKey("paymentTerms")) supplier.setPaymentTerms((String) data.get("paymentTerms"));

        return supplierRepository.save(supplier);
    }

    @Transactional
    public Supplier deleteSupplier(String id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new AppException("Supplier not found.", HttpStatus.NOT_FOUND));
        supplierRepository.delete(supplier);
        return supplier;
    }

    // ==========================================
    // PURCHASE ORDERS
    // ==========================================

    public Map<String, Object> getAllPurchaseOrders(String search, String status, String supplierId, int page, int limit) {
        PageRequest pageRequest = PageRequest.of(Math.max(0, page - 1), limit, Sort.by(Sort.Direction.DESC, "orderDate"));
        Page<PurchaseOrder> poPage = purchaseOrderRepository.findFiltered(search, status, supplierId, pageRequest);

        Map<String, Object> pagination = new HashMap<>();
        pagination.put("total", poPage.getTotalElements());
        pagination.put("page", page);
        pagination.put("limit", limit);
        pagination.put("totalPages", poPage.getTotalPages());

        Map<String, Object> response = new HashMap<>();
        response.put("purchaseOrders", poPage.getContent());
        response.put("pagination", pagination);
        return response;
    }

    public PurchaseOrder getPurchaseOrderById(String id) {
        return purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new AppException("Purchase order not found.", HttpStatus.NOT_FOUND));
    }

    @Transactional
    public PurchaseOrder createPurchaseOrder(Map<String, Object> data, String currentUserId) {
        String supId = (String) data.get("supplierId");
        Supplier supplier = supplierRepository.findById(supId)
                .orElseThrow(() -> new AppException("Supplier not found.", HttpStatus.BAD_REQUEST));

        String poNumber = (String) data.get("poNumber");
        if (poNumber == null || poNumber.isEmpty()) {
            poNumber = "PO-" + (int)(10000 + Math.random() * 90000);
        }

        PurchaseOrder po = new PurchaseOrder();
        po.setPoNumber(poNumber);
        po.setSupplier(supplier);
        po.setSupplierId(supId);
        po.setOrderDate(LocalDateTime.now());
        po.setStatus((String) data.getOrDefault("status", "ORDERED"));
        po.setNotes((String) data.get("notes"));
        po.setCreatedByUserId(currentUserId);

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<Map<String, Object>> itemsData = (List<Map<String, Object>>) data.get("items");
        List<PurchaseOrderItem> poItems = new ArrayList<>();

        if (itemsData != null) {
            for (Map<String, Object> itemMap : itemsData) {
                PurchaseOrderItem item = new PurchaseOrderItem();
                item.setPurchaseOrder(po);
                item.setMaterialName((String) itemMap.get("materialName"));

                BigDecimal qty = new BigDecimal(itemMap.get("quantity").toString());
                BigDecimal unitPrice = new BigDecimal(itemMap.get("unitPrice").toString());
                BigDecimal lineTotal = unitPrice.multiply(qty);
                totalAmount = totalAmount.add(lineTotal);

                item.setQuantity(qty);
                item.setUnitPrice(unitPrice);
                item.setTotalAmount(lineTotal);
                item.setReceivedQuantity(BigDecimal.ZERO);

                String invId = (String) itemMap.get("inventoryItemId");
                if (invId != null && !invId.isEmpty()) {
                    InventoryItem inv = inventoryItemRepository.findById(invId).orElse(null);
                    item.setInventoryItem(inv);
                    item.setInventoryItemId(invId);
                }

                poItems.add(item);
            }
        }

        po.setTotalAmount(totalAmount);
        po.setItems(poItems);

        return purchaseOrderRepository.save(po);
    }

    @Transactional
    public PurchaseOrder updatePurchaseOrderStatus(String id, String status) {
        PurchaseOrder po = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new AppException("Purchase order not found.", HttpStatus.NOT_FOUND));
        po.setStatus(status);
        return purchaseOrderRepository.save(po);
    }

    @Transactional
    public PurchaseOrder deletePurchaseOrder(String id) {
        PurchaseOrder po = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new AppException("Purchase order not found.", HttpStatus.NOT_FOUND));
        purchaseOrderRepository.delete(po);
        return po;
    }
}
