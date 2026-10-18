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
import java.util.*;

@Service
public class InventoryService {

    private final InventoryItemRepository itemRepository;
    private final InventoryCategoryRepository categoryRepository;
    private final InventoryLocationRepository locationRepository;
    private final StockMovementRepository stockMovementRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public InventoryService(InventoryItemRepository itemRepository,
                            InventoryCategoryRepository categoryRepository,
                            InventoryLocationRepository locationRepository,
                            StockMovementRepository stockMovementRepository,
                            NotificationRepository notificationRepository,
                            UserRepository userRepository) {
        this.itemRepository = itemRepository;
        this.categoryRepository = categoryRepository;
        this.locationRepository = locationRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    public Map<String, Object> getAllItems(String search, String itemType, String status, String categoryId, int page, int limit) {
        PageRequest pageRequest = PageRequest.of(Math.max(0, page - 1), limit, Sort.by(Sort.Direction.ASC, "itemCode"));
        Page<InventoryItem> itemPage = itemRepository.findFiltered(search, categoryId, itemType, status, pageRequest);

        Map<String, Object> pagination = new HashMap<>();
        pagination.put("total", itemPage.getTotalElements());
        pagination.put("page", page);
        pagination.put("limit", limit);
        pagination.put("totalPages", itemPage.getTotalPages());

        Map<String, Object> response = new HashMap<>();
        response.put("items", itemPage.getContent());
        response.put("pagination", pagination);
        return response;
    }

    public InventoryItem getItemById(String id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new AppException("Inventory item not found.", HttpStatus.NOT_FOUND));
    }

    @Transactional
    public InventoryItem createItem(Map<String, Object> data) {
        String itemCode = (String) data.get("itemCode");
        if (itemCode != null && itemRepository.findByItemCode(itemCode).isPresent()) {
            throw new AppException("Item code already exists.", HttpStatus.CONFLICT);
        }

        InventoryItem item = new InventoryItem();
        item.setItemCode(itemCode);
        item.setName((String) data.get("name"));
        item.setDescription((String) data.get("description"));

        String catId = (String) data.get("categoryId");
        if (catId != null) {
            InventoryCategory cat = categoryRepository.findById(catId)
                    .orElseThrow(() -> new AppException("Category not found.", HttpStatus.BAD_REQUEST));
            item.setCategory(cat);
            item.setCategoryId(catId);
        }

        String locId = (String) data.get("locationId");
        if (locId != null && !locId.isEmpty()) {
            InventoryLocation loc = locationRepository.findById(locId).orElse(null);
            item.setLocation(loc);
            item.setLocationId(locId);
        }

        item.setItemType((String) data.get("itemType"));
        item.setUnitOfMeasure((String) data.getOrDefault("unitOfMeasure", "PIECES"));

        BigDecimal currentQty = data.containsKey("currentQuantity") && data.get("currentQuantity") != null
                ? new BigDecimal(data.get("currentQuantity").toString()) : BigDecimal.ZERO;
        BigDecimal minStock = data.containsKey("minimumStockLevel") && data.get("minimumStockLevel") != null
                ? new BigDecimal(data.get("minimumStockLevel").toString()) : BigDecimal.TEN;
        BigDecimal reorderQty = data.containsKey("reorderQuantity") && data.get("reorderQuantity") != null
                ? new BigDecimal(data.get("reorderQuantity").toString()) : BigDecimal.valueOf(50);
        BigDecimal unitCost = data.containsKey("unitCost") && data.get("unitCost") != null
                ? new BigDecimal(data.get("unitCost").toString()) : BigDecimal.ZERO;
        BigDecimal unitPrice = data.containsKey("unitPrice") && data.get("unitPrice") != null
                ? new BigDecimal(data.get("unitPrice").toString()) : BigDecimal.ZERO;

        item.setCurrentQuantity(currentQty);
        item.setMinimumStockLevel(minStock);
        item.setReorderQuantity(reorderQty);
        item.setUnitCost(unitCost);
        item.setUnitPrice(unitPrice);

        String status = "IN_STOCK";
        if (currentQty.compareTo(BigDecimal.ZERO) <= 0) {
            status = "OUT_OF_STOCK";
        } else if (currentQty.compareTo(minStock) <= 0) {
            status = "LOW_STOCK";
        }
        item.setStatus(status);

        InventoryItem savedItem = itemRepository.save(item);

        if (currentQty.compareTo(BigDecimal.ZERO) > 0) {
            StockMovement movement = new StockMovement();
            movement.setItem(savedItem);
            movement.setItemId(savedItem.getId());
            movement.setMovementType("STOCK_IN");
            movement.setQuantity(currentQty);
            movement.setUnitCost(unitCost);
            movement.setReferenceType("INITIAL_STOCK");
            movement.setReason("Initial stock intake upon item creation");
            stockMovementRepository.save(movement);
        }

        return savedItem;
    }

    @Transactional
    public InventoryItem updateItem(String id, Map<String, Object> data) {
        InventoryItem item = itemRepository.findById(id)
                .orElseThrow(() -> new AppException("Inventory item not found.", HttpStatus.NOT_FOUND));

        if (data.containsKey("name") && data.get("name") != null) item.setName((String) data.get("name"));
        if (data.containsKey("description")) item.setDescription((String) data.get("description"));

        if (data.containsKey("categoryId") && data.get("categoryId") != null) {
            String catId = (String) data.get("categoryId");
            InventoryCategory cat = categoryRepository.findById(catId)
                    .orElseThrow(() -> new AppException("Category not found.", HttpStatus.BAD_REQUEST));
            item.setCategory(cat);
            item.setCategoryId(catId);
        }

        if (data.containsKey("locationId")) {
            String locId = (String) data.get("locationId");
            if (locId != null && !locId.isEmpty()) {
                InventoryLocation loc = locationRepository.findById(locId).orElse(null);
                item.setLocation(loc);
                item.setLocationId(locId);
            }
        }

        if (data.containsKey("itemType") && data.get("itemType") != null) item.setItemType((String) data.get("itemType"));
        if (data.containsKey("unitOfMeasure") && data.get("unitOfMeasure") != null) item.setUnitOfMeasure((String) data.get("unitOfMeasure"));

        if (data.containsKey("minimumStockLevel") && data.get("minimumStockLevel") != null) {
            item.setMinimumStockLevel(new BigDecimal(data.get("minimumStockLevel").toString()));
        }
        if (data.containsKey("reorderQuantity") && data.get("reorderQuantity") != null) {
            item.setReorderQuantity(new BigDecimal(data.get("reorderQuantity").toString()));
        }
        if (data.containsKey("unitCost") && data.get("unitCost") != null) {
            item.setUnitCost(new BigDecimal(data.get("unitCost").toString()));
        }
        if (data.containsKey("unitPrice") && data.get("unitPrice") != null) {
            item.setUnitPrice(new BigDecimal(data.get("unitPrice").toString()));
        }

        BigDecimal currentQty = item.getCurrentQuantity();
        BigDecimal minStock = item.getMinimumStockLevel();
        if (currentQty.compareTo(BigDecimal.ZERO) <= 0) {
            item.setStatus("OUT_OF_STOCK");
        } else if (currentQty.compareTo(minStock) <= 0) {
            item.setStatus("LOW_STOCK");
        } else {
            item.setStatus("IN_STOCK");
        }

        return itemRepository.save(item);
    }

    @Transactional
    public InventoryItem deleteItem(String id) {
        InventoryItem item = itemRepository.findById(id)
                .orElseThrow(() -> new AppException("Inventory item not found.", HttpStatus.NOT_FOUND));
        itemRepository.delete(item);
        return item;
    }

    // ==========================================
    // STOCK MOVEMENTS & ADJUSTMENTS
    // ==========================================

    @Transactional
    public Map<String, Object> createStockMovement(Map<String, Object> movementData, String currentUserId) {
        String itemId = (String) movementData.get("itemId");
        String movementType = (String) movementData.get("movementType");
        BigDecimal quantity = new BigDecimal(movementData.get("quantity").toString());
        String reason = (String) movementData.get("reason");
        String referenceType = (String) movementData.getOrDefault("referenceType", "MANUAL_ADJUSTMENT");
        String referenceId = (String) movementData.get("referenceId");

        InventoryItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new AppException("Inventory item not found.", HttpStatus.NOT_FOUND));

        BigDecimal currentQty = item.getCurrentQuantity();
        BigDecimal newQty = currentQty;

        if ("STOCK_IN".equals(movementType) || "RETURN".equals(movementType)) {
            newQty = currentQty.add(quantity);
        } else if ("STOCK_OUT".equals(movementType)) {
            if (quantity.compareTo(currentQty) > 0) {
                Boolean allowOverride = (Boolean) movementData.getOrDefault("allowOverride", false);
                if (!Boolean.TRUE.equals(allowOverride)) {
                    throw new AppException("Insufficient stock! Requested " + quantity + " " + item.getUnitOfMeasure() + ", but only " + currentQty + " available.", HttpStatus.BAD_REQUEST);
                }
            }
            newQty = currentQty.subtract(quantity).max(BigDecimal.ZERO);
        } else if ("ADJUSTMENT".equals(movementType)) {
            newQty = quantity.max(BigDecimal.ZERO);
        }

        String newStatus = "IN_STOCK";
        if (newQty.compareTo(BigDecimal.ZERO) <= 0) {
            newStatus = "OUT_OF_STOCK";
        } else if (newQty.compareTo(item.getMinimumStockLevel()) <= 0) {
            newStatus = "LOW_STOCK";
        }

        item.setCurrentQuantity(newQty);
        item.setStatus(newStatus);
        InventoryItem updatedItem = itemRepository.save(item);

        StockMovement movement = new StockMovement();
        movement.setItem(updatedItem);
        movement.setItemId(updatedItem.getId());
        movement.setMovementType(movementType);
        movement.setQuantity(quantity);
        movement.setUnitCost(item.getUnitCost());
        movement.setReferenceType(referenceType);
        movement.setReferenceId(referenceId);
        movement.setReason(reason);

        if (currentUserId != null) {
            User user = userRepository.findById(currentUserId).orElse(null);
            movement.setPerformedByUser(user);
            movement.setPerformedByUserId(currentUserId);
        }

        StockMovement savedMovement = stockMovementRepository.save(movement);

        if ("LOW_STOCK".equals(newStatus) || "OUT_OF_STOCK".equals(newStatus)) {
            Notification notification = new Notification();
            notification.setTitle("Low Stock Alert: " + item.getName() + " (" + item.getItemCode() + ")");
            notification.setMessage("Current quantity dropped to " + newQty + " " + item.getUnitOfMeasure() + ". Minimum threshold is " + item.getMinimumStockLevel() + ".");
            notification.setType("LOW_STOCK");
            notification.setRecipientRole("INVENTORY_STAFF");
            notification.setReferenceId(item.getId());
            notificationRepository.save(notification);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("movement", savedMovement);
        result.put("updatedItem", updatedItem);
        return result;
    }

    public Map<String, Object> getStockMovements(String itemId, int page, int limit) {
        PageRequest pageRequest = PageRequest.of(Math.max(0, page - 1), limit);
        Page<StockMovement> movementPage;
        if (itemId != null && !itemId.isEmpty()) {
            List<StockMovement> list = stockMovementRepository.findByItemIdOrderByCreatedAtDesc(itemId);
            Map<String, Object> pagination = new HashMap<>();
            pagination.put("total", list.size());
            pagination.put("page", page);
            pagination.put("limit", limit);
            pagination.put("totalPages", 1);
            Map<String, Object> res = new HashMap<>();
            res.put("movements", list);
            res.put("pagination", pagination);
            return res;
        } else {
            movementPage = stockMovementRepository.findAllByOrderByCreatedAtDesc(pageRequest);
            Map<String, Object> pagination = new HashMap<>();
            pagination.put("total", movementPage.getTotalElements());
            pagination.put("page", page);
            pagination.put("limit", limit);
            pagination.put("totalPages", movementPage.getTotalPages());
            Map<String, Object> res = new HashMap<>();
            res.put("movements", movementPage.getContent());
            res.put("pagination", pagination);
            return res;
        }
    }

    // ==========================================
    // CATEGORIES & LOCATIONS
    // ==========================================

    public List<InventoryCategory> getAllCategories() {
        return categoryRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
    }

    public List<InventoryLocation> getAllLocations() {
        return locationRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
    }
}
