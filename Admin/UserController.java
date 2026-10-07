package com.texgarment.controller;

import com.texgarment.service.AuthService;
import com.texgarment.util.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final AuthService authService;

    public UserController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Boolean status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {
        Map<String, Object> result = authService.getAllUsers(search, role, status, page, limit);
        return ResponseEntity.ok(ApiResponse.success(result, "Users retrieved successfully"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> createUser(@RequestBody Map<String, Object> data) {
        Map<String, Object> result = authService.createUser(data);
        return ResponseEntity.ok(ApiResponse.success(result, "User created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateUser(
            @PathVariable String id,
            @RequestBody Map<String, Object> data) {
        Map<String, Object> result = authService.updateUser(id, data);
        return ResponseEntity.ok(ApiResponse.success(result, "User updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteUser(@PathVariable String id) {
        Map<String, Object> result = authService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success(result, "User deleted successfully"));
    }
}
