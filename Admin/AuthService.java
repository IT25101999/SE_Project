package com.texgarment.service;

import com.texgarment.config.JwtUtil;
import com.texgarment.exception.AppException;
import com.texgarment.model.User;
import com.texgarment.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    public Map<String, Object> register(Map<String, Object> userData) {
        String email = (String) userData.get("email");
        String username = (String) userData.get("username");
        String password = (String) userData.get("password");
        String firstName = (String) userData.get("firstName");
        String lastName = (String) userData.get("lastName");
        String phone = (String) userData.get("phone");
        String role = (String) userData.getOrDefault("role", "EMPLOYEE");

        if (userRepository.existsByEmail(email)) {
            throw new AppException("Email is already registered.", HttpStatus.CONFLICT);
        }
        if (userRepository.existsByUsername(username)) {
            throw new AppException("Username is already registered.", HttpStatus.CONFLICT);
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPhone(phone);
        user.setRole(role != null ? role : "EMPLOYEE");
        user.setIsActive(true);

        User savedUser = userRepository.save(user);
        String token = jwtUtil.generateToken(savedUser.getId(), savedUser.getEmail(), savedUser.getRole(), savedUser.getUsername());

        Map<String, Object> safeUser = getSafeUserMap(savedUser);
        Map<String, Object> response = new HashMap<>();
        response.put("user", safeUser);
        response.put("token", token);
        return response;
    }

    public Map<String, Object> login(Map<String, String> credentials) {
        String identifier = credentials.get("username");
        if (identifier == null || identifier.trim().isEmpty()) {
            identifier = credentials.get("email");
        }
        String password = credentials.get("password");

        if (identifier == null || identifier.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            throw new AppException("Username/email and password are required.", HttpStatus.BAD_REQUEST);
        }

        identifier = identifier.trim();
        Optional<User> userOpt = userRepository.findByEmailOrUsername(identifier, identifier);
        if (userOpt.isEmpty()) {
            throw new AppException("Invalid email or password.", HttpStatus.UNAUTHORIZED);
        }

        User user = userOpt.get();
        if (Boolean.FALSE.equals(user.getIsActive())) {
            throw new AppException("Your account has been deactivated. Please contact an administrator.", HttpStatus.FORBIDDEN);
        }

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new AppException("Invalid email or password.", HttpStatus.UNAUTHORIZED);
        }

        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole(), user.getUsername());
        Map<String, Object> safeUser = getSafeUserMap(user);

        Map<String, Object> response = new HashMap<>();
        response.put("user", safeUser);
        response.put("token", token);
        return response;
    }

    public Map<String, Object> getProfile(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException("User not found.", HttpStatus.NOT_FOUND));
        return getSafeUserMap(user);
    }

    @Transactional
    public Map<String, Object> updateProfile(String userId, Map<String, Object> updateData) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException("User not found.", HttpStatus.NOT_FOUND));

        if (updateData.containsKey("firstName") && updateData.get("firstName") != null) {
            user.setFirstName((String) updateData.get("firstName"));
        }
        if (updateData.containsKey("lastName") && updateData.get("lastName") != null) {
            user.setLastName((String) updateData.get("lastName"));
        }
        if (updateData.containsKey("phone")) {
            user.setPhone((String) updateData.get("phone"));
        }

        User saved = userRepository.save(user);
        return getSafeUserMap(saved);
    }

    @Transactional
    public Map<String, Object> forgotPassword(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        Map<String, Object> res = new HashMap<>();
        if (userOpt.isEmpty()) {
            res.put("message", "If an account exists with this email, a reset code has been sent.");
            return res;
        }

        User user = userOpt.get();
        String code = String.valueOf((int) (100000 + Math.random() * 900000));
        user.setResetPasswordCode(code);
        user.setResetPasswordExpires(LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);

        res.put("message", "Verification code sent successfully. Valid for 15 minutes.");
        res.put("demoVerificationCode", code);
        return res;
    }

    @Transactional
    public Map<String, Object> resetPassword(Map<String, String> data) {
        String email = data.get("email");
        String code = data.get("code");
        String newPassword = data.get("newPassword");

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException("Invalid or expired password reset request.", HttpStatus.BAD_REQUEST));

        if (user.getResetPasswordCode() == null || user.getResetPasswordExpires() == null) {
            throw new AppException("Invalid or expired password reset request.", HttpStatus.BAD_REQUEST);
        }

        if (LocalDateTime.now().isAfter(user.getResetPasswordExpires())) {
            throw new AppException("Password reset code has expired. Please request a new one.", HttpStatus.BAD_REQUEST);
        }

        if (!user.getResetPasswordCode().equals(code != null ? code.trim() : "")) {
            throw new AppException("Invalid verification code.", HttpStatus.BAD_REQUEST);
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setResetPasswordCode(null);
        user.setResetPasswordExpires(null);
        userRepository.save(user);

        Map<String, Object> res = new HashMap<>();
        res.put("message", "Password has been reset successfully. You may now log in.");
        return res;
    }

    public Map<String, Object> getAllUsers(String search, String role, Boolean status, int page, int limit) {
        PageRequest pageRequest = PageRequest.of(Math.max(0, page - 1), limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<User> userPage = userRepository.findFiltered(search, role, status, pageRequest);

        List<Map<String, Object>> users = userPage.getContent().stream().map(this::getSafeUserMap).toList();

        Map<String, Object> pagination = new HashMap<>();
        pagination.put("total", userPage.getTotalElements());
        pagination.put("page", page);
        pagination.put("limit", limit);
        pagination.put("totalPages", userPage.getTotalPages());

        Map<String, Object> response = new HashMap<>();
        response.put("users", users);
        response.put("pagination", pagination);
        return response;
    }

    @Transactional
    public Map<String, Object> createUser(Map<String, Object> data) {
        String username = (String) data.get("username");
        String email = (String) data.get("email");
        String password = (String) data.get("password");
        String firstName = (String) data.get("firstName");
        String lastName = (String) data.get("lastName");
        String fullName = (String) data.get("fullName");
        String phone = (String) data.get("phone");
        String role = (String) data.getOrDefault("role", "EMPLOYEE");

        if (fullName != null && !fullName.trim().isEmpty() && (firstName == null || firstName.isEmpty())) {
            String[] parts = fullName.trim().split("\\s+", 2);
            firstName = parts[0];
            if (parts.length > 1) lastName = parts[1];
        }

        if (username == null || username.trim().isEmpty()) {
            throw new AppException("Username is required.", HttpStatus.BAD_REQUEST);
        }
        if (email == null || email.trim().isEmpty()) {
            throw new AppException("Email is required.", HttpStatus.BAD_REQUEST);
        }
        if (password == null || password.trim().isEmpty()) {
            throw new AppException("Password is required.", HttpStatus.BAD_REQUEST);
        }

        if (userRepository.existsByEmail(email.trim())) {
            throw new AppException("Email is already registered.", HttpStatus.CONFLICT);
        }
        if (userRepository.existsByUsername(username.trim())) {
            throw new AppException("Username is already registered.", HttpStatus.CONFLICT);
        }

        User user = new User();
        user.setUsername(username.trim());
        user.setEmail(email.trim());
        user.setPasswordHash(passwordEncoder.encode(password.trim()));
        user.setFirstName(firstName != null ? firstName.trim() : "User");
        user.setLastName(lastName != null ? lastName.trim() : "");
        user.setPhone(phone != null ? phone.trim() : null);
        user.setRole(role != null ? role.trim() : "EMPLOYEE");
        user.setIsActive(true);

        User savedUser = userRepository.save(user);
        Map<String, Object> safeUser = getSafeUserMap(savedUser);
        Map<String, Object> response = new HashMap<>();
        response.put("user", safeUser);
        return response;
    }

    @Transactional
    public Map<String, Object> updateUser(String id, Map<String, Object> data) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException("User not found.", HttpStatus.NOT_FOUND));

        if (data.containsKey("username") && data.get("username") != null) user.setUsername((String) data.get("username"));
        if (data.containsKey("email") && data.get("email") != null) user.setEmail((String) data.get("email"));
        if (data.containsKey("firstName") && data.get("firstName") != null) user.setFirstName((String) data.get("firstName"));
        if (data.containsKey("lastName") && data.get("lastName") != null) user.setLastName((String) data.get("lastName"));
        
        if (data.containsKey("fullName") && data.get("fullName") != null) {
            String fn = (String) data.get("fullName");
            String[] parts = fn.trim().split("\\s+", 2);
            user.setFirstName(parts[0]);
            if (parts.length > 1) user.setLastName(parts[1]);
        }

        if (data.containsKey("phone")) user.setPhone((String) data.get("phone"));
        if (data.containsKey("role") && data.get("role") != null) user.setRole((String) data.get("role"));
        if (data.containsKey("isActive") && data.get("isActive") != null) user.setIsActive((Boolean) data.get("isActive"));

        if (data.containsKey("password") && data.get("password") != null && !((String) data.get("password")).trim().isEmpty()) {
            user.setPasswordHash(passwordEncoder.encode(((String) data.get("password")).trim()));
        }

        User saved = userRepository.save(user);
        Map<String, Object> safeUser = getSafeUserMap(saved);
        Map<String, Object> response = new HashMap<>();
        response.put("user", safeUser);
        return response;
    }

    @Transactional
    public Map<String, Object> deleteUser(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException("User not found.", HttpStatus.NOT_FOUND));
        userRepository.delete(user);
        Map<String, Object> res = new HashMap<>();
        res.put("id", id);
        res.put("username", user.getUsername());
        res.put("email", user.getEmail());
        return res;
    }

    private Map<String, Object> getSafeUserMap(User user) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", user.getId());
        map.put("username", user.getUsername());
        map.put("email", user.getEmail());
        map.put("firstName", user.getFirstName());
        map.put("lastName", user.getLastName());
        String fn = ((user.getFirstName() != null ? user.getFirstName() : "") + " " + (user.getLastName() != null ? user.getLastName() : "")).trim();
        map.put("fullName", fn.isEmpty() ? user.getUsername() : fn);
        map.put("phone", user.getPhone());
        map.put("role", user.getRole());
        map.put("isActive", user.getIsActive());
        map.put("createdAt", user.getCreatedAt());
        return map;
    }
}
