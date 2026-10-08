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
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final OrderItemRepository orderItemRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final NotificationRepository notificationRepository;

    public OrderService(OrderRepository orderRepository,
                        CustomerRepository customerRepository,
                        OrderItemRepository orderItemRepository,
                        InventoryItemRepository inventoryItemRepository,
                        NotificationRepository notificationRepository) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.orderItemRepository = orderItemRepository;
        this.inventoryItemRepository = inventoryItemRepository;
        this.notificationRepository = notificationRepository;
    }

    // ==========================================
    // CUSTOMERS
    // ==========================================

    public Map<String, Object> getAllCustomers(String search, int page, int limit) {
        PageRequest pageRequest = PageRequest.of(Math.max(0, page - 1), limit, Sort.by(Sort.Direction.ASC, "name"));
        Page<Customer> customerPage = customerRepository.findFiltered(search, pageRequest);

        Map<String, Object> pagination = new HashMap<>();
        pagination.put("total", customerPage.getTotalElements());
        pagination.put("page", page);
        pagination.put("limit", limit);
        pagination.put("totalPages", customerPage.getTotalPages());

        Map<String, Object> response = new HashMap<>();
        response.put("customers", customerPage.getContent());
        response.put("pagination", pagination);
        return response;
    }

    public Customer getCustomerById(String id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new AppException("Customer not found.", HttpStatus.NOT_FOUND));
    }

    @Transactional
    public Customer createCustomer(Map<String, Object> data) {
        String code = (String) data.get("customerCode");
        if (code == null || code.isEmpty()) {
            code = "CUST-" + (int)(1000 + Math.random() * 9000);
        }

        Customer customer = new Customer();
        customer.setCustomerCode(code);
        customer.setName((String) data.get("name"));
        customer.setCompanyName((String) data.get("companyName"));
        customer.setEmail((String) data.get("email"));
        customer.setPhone((String) data.get("phone"));
        customer.setAddress((String) data.get("address"));
        customer.setCity((String) data.get("city"));
        customer.setCountry((String) data.getOrDefault("country", "Sri Lanka"));

        return customerRepository.save(customer);
    }

    @Transactional
    public Customer updateCustomer(String id, Map<String, Object> data) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new AppException("Customer not found.", HttpStatus.NOT_FOUND));

        if (data.containsKey("name") && data.get("name") != null) customer.setName((String) data.get("name"));
        if (data.containsKey("companyName")) customer.setCompanyName((String) data.get("companyName"));
        if (data.containsKey("email") && data.get("email") != null) customer.setEmail((String) data.get("email"));
        if (data.containsKey("phone")) customer.setPhone((String) data.get("phone"));
        if (data.containsKey("address")) customer.setAddress((String) data.get("address"));
        if (data.containsKey("city")) customer.setCity((String) data.get("city"));

        return customerRepository.save(customer);
    }

    @Transactional
    public Customer deleteCustomer(String id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new AppException("Customer not found.", HttpStatus.NOT_FOUND));
        customerRepository.delete(customer);
        return customer;
    }

    // ==========================================
    // ORDERS
    // ==========================================

    public Map<String, Object> getAllOrders(String search, String status, String paymentStatus, String customerId, int page, int limit) {
        PageRequest pageRequest = PageRequest.of(Math.max(0, page - 1), limit, Sort.by(Sort.Direction.DESC, "orderDate"));
        Page<Order> orderPage = orderRepository.findFiltered(search, status, paymentStatus, customerId, pageRequest);

        Map<String, Object> pagination = new HashMap<>();
        pagination.put("total", orderPage.getTotalElements());
        pagination.put("page", page);
        pagination.put("limit", limit);
        pagination.put("totalPages", orderPage.getTotalPages());

        Map<String, Object> response = new HashMap<>();
        response.put("orders", orderPage.getContent());
        response.put("pagination", pagination);
        return response;
    }

    public Order getOrderById(String id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new AppException("Order not found.", HttpStatus.NOT_FOUND));
    }

    @Transactional
    public Order createOrder(Map<String, Object> data) {
        Customer customer = null;
        String custId = (String) data.get("customerId");
        if (custId != null && !custId.trim().isEmpty()) {
            customer = customerRepository.findById(custId).orElse(null);
        }

        if (customer == null) {
            String customerEmail = (String) data.get("customerEmail");
            String customerName = (String) data.getOrDefault("customerName", data.get("name"));
            String customerPhone = (String) data.get("customerPhone");
            String companyName = (String) data.get("companyName");

            if (customerEmail != null && !customerEmail.trim().isEmpty()) {
                customer = customerRepository.findByEmail(customerEmail.trim()).orElse(null);
            }

            if (customer == null) {
                if (customerName == null || customerName.trim().isEmpty()) {
                    customerName = "Valued Customer";
                }
                if (customerEmail == null || customerEmail.trim().isEmpty()) {
                    customerEmail = "cust_" + System.currentTimeMillis() + "@texgarment.com";
                }
                Customer newCust = new Customer();
                newCust.setCustomerCode("CUST-" + (int)(1000 + Math.random() * 9000));
                newCust.setName(customerName.trim());
                newCust.setEmail(customerEmail.trim());
                newCust.setPhone(customerPhone != null ? customerPhone.trim() : null);
                newCust.setCompanyName(companyName != null ? companyName.trim() : customerName.trim());
                customer = customerRepository.save(newCust);
            }
        }

        String orderNumber = (String) data.get("orderNumber");
        if (orderNumber == null || orderNumber.trim().isEmpty()) {
            orderNumber = "ORD-" + (int)(10000 + Math.random() * 90000);
        }

        Order order = new Order();
        order.setOrderNumber(orderNumber);
        order.setCustomer(customer);
        order.setCustomerId(customer.getId());
        order.setOrderDate(LocalDateTime.now());
        order.setStatus((String) data.getOrDefault("status", "PENDING"));
        order.setPaymentStatus((String) data.getOrDefault("paymentStatus", "PENDING"));
        order.setShippingAddress((String) data.get("shippingAddress"));
        order.setNotes((String) data.get("notes"));

        String delivDateStr = (String) data.getOrDefault("expectedDeliveryDate", data.get("deliveryDate"));
        if (delivDateStr != null && !delivDateStr.trim().isEmpty()) {
            try {
                if (delivDateStr.length() == 10) {
                    order.setExpectedDeliveryDate(java.time.LocalDate.parse(delivDateStr).atStartOfDay());
                } else {
                    order.setExpectedDeliveryDate(LocalDateTime.parse(delivDateStr.replace("Z", "")));
                }
            } catch (Exception ignored) {}
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<Map<String, Object>> itemsData = (List<Map<String, Object>>) data.get("items");
        List<OrderItem> orderItems = new ArrayList<>();

        if (itemsData != null) {
            for (Map<String, Object> itemMap : itemsData) {
                OrderItem item = new OrderItem();
                item.setOrder(order);

                String itemName = (String) itemMap.get("itemName");
                if (itemName == null || itemName.trim().isEmpty()) {
                    itemName = (String) itemMap.get("garmentType");
                }
                if (itemName == null || itemName.trim().isEmpty()) {
                    itemName = "Garment Item";
                }
                item.setItemName(itemName);

                Object desc = itemMap.get("itemDescription");
                if (desc == null && (itemMap.containsKey("size") || itemMap.containsKey("color"))) {
                    desc = "Size: " + itemMap.getOrDefault("size", "Std") + ", Color: " + itemMap.getOrDefault("color", "Std");
                }
                item.setItemDescription(desc != null ? desc.toString() : null);

                int qty = 1;
                if (itemMap.get("quantity") != null) {
                    try {
                        qty = Integer.parseInt(itemMap.get("quantity").toString());
                    } catch (Exception ignored) {}
                }
                BigDecimal unitPrice = BigDecimal.ZERO;
                if (itemMap.get("unitPrice") != null) {
                    try {
                        unitPrice = new BigDecimal(itemMap.get("unitPrice").toString());
                    } catch (Exception ignored) {}
                }
                BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(qty));
                totalAmount = totalAmount.add(lineTotal);

                item.setQuantity(qty);
                item.setUnitPrice(unitPrice);
                item.setTotalPrice(lineTotal);

                String itemId = (String) itemMap.get("itemId");
                if (itemId != null && !itemId.trim().isEmpty()) {
                    InventoryItem invItem = inventoryItemRepository.findById(itemId).orElse(null);
                    item.setItem(invItem);
                    item.setItemId(itemId);
                }

                orderItems.add(item);
            }
        }

        order.setTotalAmount(totalAmount);
        order.setOrderItems(orderItems);

        Order savedOrder = orderRepository.save(order);

        Notification notification = new Notification();
        notification.setTitle("New Order Created: " + savedOrder.getOrderNumber());
        notification.setMessage("Order for " + customer.getName() + " totaling $" + totalAmount + " received.");
        notification.setType("ORDER_STATUS");
        notification.setRecipientRole("SALES_STAFF");
        notification.setReferenceId(savedOrder.getId());
        notificationRepository.save(notification);

        return savedOrder;
    }

    @Transactional
    public Order updateOrderStatus(String id, String status, String notes) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new AppException("Order not found.", HttpStatus.NOT_FOUND));

        order.setStatus(status);
        if (notes != null) order.setNotes(notes);
        if ("DELIVERED".equals(status)) {
            order.setActualDeliveryDate(LocalDateTime.now());
        }

        return orderRepository.save(order);
    }

    @Transactional
    public Order updateOrder(String id, Map<String, Object> data) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new AppException("Order not found.", HttpStatus.NOT_FOUND));

        if (data.containsKey("status") && data.get("status") != null) order.setStatus((String) data.get("status"));
        if (data.containsKey("paymentStatus") && data.get("paymentStatus") != null) order.setPaymentStatus((String) data.get("paymentStatus"));
        if (data.containsKey("shippingAddress")) order.setShippingAddress((String) data.get("shippingAddress"));
        if (data.containsKey("notes")) order.setNotes((String) data.get("notes"));

        return orderRepository.save(order);
    }

    @Transactional
    public Order deleteOrder(String id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new AppException("Order not found.", HttpStatus.NOT_FOUND));
        orderRepository.delete(order);
        return order;
    }
}
