package com.texgarment.controller;

import com.texgarment.model.Customer;
import com.texgarment.model.Order;
import com.texgarment.service.OrderService;
import com.texgarment.util.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // ==========================================
    // CUSTOMERS (Must be before /{id})
    // ==========================================
    @GetMapping("/customers")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllCustomers(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int limit) {
        Map<String, Object> result = orderService.getAllCustomers(search, page, limit);
        return ResponseEntity.ok(ApiResponse.success(result, "Customers retrieved"));
    }

    @GetMapping("/customers/{id}")
    public ResponseEntity<ApiResponse<Customer>> getCustomerById(@PathVariable String id) {
        Customer customer = orderService.getCustomerById(id);
        return ResponseEntity.ok(ApiResponse.success(customer, "Customer retrieved"));
    }

    @PostMapping("/customers")
    public ResponseEntity<ApiResponse<Customer>> createCustomer(@RequestBody Map<String, Object> data) {
        Customer customer = orderService.createCustomer(data);
        return ResponseEntity.ok(ApiResponse.success(customer, "Customer created successfully"));
    }

    @PutMapping("/customers/{id}")
    public ResponseEntity<ApiResponse<Customer>> updateCustomer(@PathVariable String id, @RequestBody Map<String, Object> data) {
        Customer customer = orderService.updateCustomer(id, data);
        return ResponseEntity.ok(ApiResponse.success(customer, "Customer updated successfully"));
    }

    @DeleteMapping("/customers/{id}")
    public ResponseEntity<ApiResponse<Customer>> deleteCustomer(@PathVariable String id) {
        Customer customer = orderService.deleteCustomer(id);
        return ResponseEntity.ok(ApiResponse.success(customer, "Customer deleted successfully"));
    }

    // ==========================================
    // ORDERS CRUD
    // ==========================================
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllOrders(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String paymentStatus,
            @RequestParam(required = false) String customerId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {
        Map<String, Object> result = orderService.getAllOrders(search, status, paymentStatus, customerId, page, limit);
        return ResponseEntity.ok(ApiResponse.success(result, "Orders retrieved successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Order>> getOrderById(@PathVariable String id) {
        Order order = orderService.getOrderById(id);
        return ResponseEntity.ok(ApiResponse.success(order, "Order details retrieved"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Order>> createOrder(@RequestBody Map<String, Object> data) {
        Order order = orderService.createOrder(data);
        return ResponseEntity.ok(ApiResponse.success(order, "Order created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Order>> updateOrder(
            @PathVariable String id,
            @RequestBody Map<String, Object> data) {
        Order order = orderService.updateOrder(id, data);
        return ResponseEntity.ok(ApiResponse.success(order, "Order updated successfully"));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Order>> updateOrderStatus(
            @PathVariable String id,
            @RequestBody Map<String, String> data) {
        String status = data.get("status");
        String notes = data.get("notes");
        Order order = orderService.updateOrderStatus(id, status, notes);
        return ResponseEntity.ok(ApiResponse.success(order, "Order status updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Order>> deleteOrder(@PathVariable String id) {
        Order order = orderService.deleteOrder(id);
        return ResponseEntity.ok(ApiResponse.success(order, "Order deleted successfully"));
    }
}
