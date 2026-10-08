package com.texgarment.controller;

import com.texgarment.model.InventoryCategory;
import com.texgarment.model.InventoryItem;
import com.texgarment.model.InventoryLocation;
import com.texgarment.service.InventoryService;
import com.texgarment.util.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    // ==========================================
    // CATEGORIES (Must be before /{id})
    // ==========================================
    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<InventoryCategory>>> getAllCategories() {
        List<InventoryCategory> categories = inventoryService.getAllCategories();
        return ResponseEntity.ok(ApiResponse.success(categories, "Categories retrieved"));
    }

    // ==========================================
    // LOCATIONS (Must be before /{id})
    // ==========================================
    @GetMapping("/locations")
    public ResponseEntity<ApiResponse<List<InventoryLocation>>> getAllLocations() {
        List<InventoryLocation> locations = inventoryService.getAllLocations();
        return ResponseEntity.ok(ApiResponse.success(locations, "Locations retrieved"));
    }

    // ==========================================
    // MOVEMENTS (Must be before /{id})
    // ==========================================
    @GetMapping("/movements")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStockMovements(
            @RequestParam(required = false) String itemId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        Map<String, Object> result = inventoryService.getStockMovements(itemId, page, limit);
        return ResponseEntity.ok(ApiResponse.success(result, "Stock movements retrieved"));
    }

    @PostMapping("/movements")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createStockMovement(
            @RequestBody Map<String, Object> movementData,
            Authentication authentication) {
        String currentUserId = authentication != null ? (String) authentication.getPrincipal() : null;
        Map<String, Object> result = inventoryService.createStockMovement(movementData, currentUserId);
        return ResponseEntity.ok(ApiResponse.success(result, "Stock movement recorded successfully"));
    }

    // ==========================================
    // INVENTORY ITEMS CRUD
    // ==========================================
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllItems(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String itemType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String categoryId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {
        Map<String, Object> result = inventoryService.getAllItems(search, itemType, status, categoryId, page, limit);
        return ResponseEntity.ok(ApiResponse.success(result, "Inventory items retrieved"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InventoryItem>> getItemById(@PathVariable String id) {
        InventoryItem item = inventoryService.getItemById(id);
        return ResponseEntity.ok(ApiResponse.success(item, "Item details retrieved"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<InventoryItem>> createItem(@RequestBody Map<String, Object> data) {
        InventoryItem item = inventoryService.createItem(data);
        return ResponseEntity.ok(ApiResponse.success(item, "Inventory item created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<InventoryItem>> updateItem(
            @PathVariable String id,
            @RequestBody Map<String, Object> data) {
        InventoryItem item = inventoryService.updateItem(id, data);
        return ResponseEntity.ok(ApiResponse.success(item, "Inventory item updated successfully"));
    }

    @PostMapping("/{id}/adjust")
    public ResponseEntity<ApiResponse<Map<String, Object>>> adjustStock(
            @PathVariable String id,
            @RequestBody Map<String, Object> data,
            Authentication authentication) {
        String currentUserId = authentication != null ? (String) authentication.getPrincipal() : null;
        data.put("itemId", id);
        if (!data.containsKey("movementType") && data.containsKey("adjustmentType")) {
            data.put("movementType", data.get("adjustmentType"));
        }
        if (!data.containsKey("movementType")) {
            data.put("movementType", "ADJUSTMENT");
        }
        Map<String, Object> result = inventoryService.createStockMovement(data, currentUserId);
        return ResponseEntity.ok(ApiResponse.success(result, "Stock adjusted successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<InventoryItem>> deleteItem(@PathVariable String id) {
        InventoryItem item = inventoryService.deleteItem(id);
        return ResponseEntity.ok(ApiResponse.success(item, "Inventory item deleted successfully"));
    }
}
