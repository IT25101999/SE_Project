package com.texgarment.controller;

import com.texgarment.model.PurchaseOrder;
import com.texgarment.model.Supplier;
import com.texgarment.service.SupplierService;
import com.texgarment.util.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/suppliers")
public class SupplierController {

    private final SupplierService supplierService;

    @Autowired
    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    // ==========================================
    // PURCHASE ORDERS (Must be before /{id})
    // ==========================================
    @GetMapping("/purchase-orders")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllPurchaseOrders(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String supplierId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {
        Map<String, Object> result = supplierService.getAllPurchaseOrders(search, status, supplierId, page, limit);
        return ResponseEntity.ok(ApiResponse.success(result, "Purchase orders retrieved"));
    }

    @PostMapping("/purchase-orders")
    public ResponseEntity<ApiResponse<PurchaseOrder>> createPurchaseOrder(
            @RequestBody Map<String, Object> data,
            Authentication authentication) {
        String currentUserId = authentication != null ? (String) authentication.getPrincipal() : null;
        PurchaseOrder po = supplierService.createPurchaseOrder(data, currentUserId);
        return ResponseEntity.ok(ApiResponse.success(po, "Purchase order created successfully"));
    }

    @PutMapping("/purchase-orders/{id}/status")
    public ResponseEntity<ApiResponse<PurchaseOrder>> updatePurchaseOrderStatus(
            @PathVariable String id,
            @RequestBody Map<String, String> data) {
        String status = data.get("status");
        PurchaseOrder po = supplierService.updatePurchaseOrderStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success(po, "Purchase order status updated"));
    }

    @DeleteMapping("/purchase-orders/{id}")
    public ResponseEntity<ApiResponse<PurchaseOrder>> deletePurchaseOrder(@PathVariable String id) {
        PurchaseOrder po = supplierService.deletePurchaseOrder(id);
        return ResponseEntity.ok(ApiResponse.success(po, "Purchase order deleted"));
    }

    // ==========================================
    // SUPPLIERS CRUD
    // ==========================================
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllSuppliers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {
        Map<String, Object> result = supplierService.getAllSuppliers(search, status, page, limit);
        return ResponseEntity.ok(ApiResponse.success(result, "Suppliers retrieved successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Supplier>> getSupplierById(@PathVariable String id) {
        Supplier supplier = supplierService.getSupplierById(id);
        return ResponseEntity.ok(ApiResponse.success(supplier, "Supplier details retrieved"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Supplier>> createSupplier(@RequestBody Map<String, Object> data) {
        Supplier supplier = supplierService.createSupplier(data);
        return ResponseEntity.ok(ApiResponse.success(supplier, "Supplier registered successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Supplier>> updateSupplier(
            @PathVariable String id,
            @RequestBody Map<String, Object> data) {
        Supplier supplier = supplierService.updateSupplier(id, data);
        return ResponseEntity.ok(ApiResponse.success(supplier, "Supplier updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Supplier>> deleteSupplier(@PathVariable String id) {
        Supplier supplier = supplierService.deleteSupplier(id);
        return ResponseEntity.ok(ApiResponse.success(supplier, "Supplier deleted successfully"));
    }
}
