package com.texgarment.controller;

import com.texgarment.service.AuthService;
import com.texgarment.util.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(@RequestBody Map<String, String> credentials) {
        Map<String, Object> result = authService.login(credentials);
        return ResponseEntity.ok(ApiResponse.success(result, "Logged in successfully"));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Map<String, Object>>> register(@RequestBody Map<String, Object> userData) {
        Map<String, Object> result = authService.register(userData);
        return ResponseEntity.ok(ApiResponse.success(result, "User registered successfully"));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Map<String, Object>>> forgotPassword(@RequestBody Map<String, String> data) {
        Map<String, Object> result = authService.forgotPassword(data.get("email"));
        return ResponseEntity.ok(ApiResponse.success(result, (String) result.get("message")));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Map<String, Object>>> resetPassword(@RequestBody Map<String, String> data) {
        Map<String, Object> result = authService.resetPassword(data);
        return ResponseEntity.ok(ApiResponse.success(result, (String) result.get("message")));
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProfile(Authentication authentication) {
        String userId = (String) authentication.getPrincipal();
        Map<String, Object> profile = authService.getProfile(userId);
        return ResponseEntity.ok(ApiResponse.success(profile, "Profile retrieved successfully"));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateProfile(
            Authentication authentication,
            @RequestBody Map<String, Object> updateData) {
        String userId = (String) authentication.getPrincipal();
        Map<String, Object> profile = authService.updateProfile(userId, updateData);
        return ResponseEntity.ok(ApiResponse.success(profile, "Profile updated successfully"));
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Boolean status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {
        Map<String, Object> result = authService.getAllUsers(search, role, status, page, limit);
        return ResponseEntity.ok(ApiResponse.success(result, "Users retrieved successfully"));
    }

    @PostMapping("/users")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createUser(@RequestBody Map<String, Object> data) {
        Map<String, Object> result = authService.createUser(data);
        return ResponseEntity.ok(ApiResponse.success(result, "User created successfully"));
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateUser(
            @PathVariable String id,
            @RequestBody Map<String, Object> data) {
        Map<String, Object> result = authService.updateUser(id, data);
        return ResponseEntity.ok(ApiResponse.success(result, "User updated successfully"));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteUser(@PathVariable String id) {
        Map<String, Object> result = authService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success(result, "User deleted successfully"));
    }
}
