package com.shopedge.backend.adminservices;

import com.shopedge.backend.entities.Role;
import com.shopedge.backend.entities.User;
import com.shopedge.backend.repositories.JWTTokenRepository;
import com.shopedge.backend.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Admin User Service
 * Contains business logic for admin user management operations
 * Handles user modification and retrieval with security considerations
 */
@Service
@Transactional
public class AdminUserService {

    private final UserRepository userRepository;
    private final JWTTokenRepository jwtTokenRepository;

    public AdminUserService(UserRepository userRepository, JWTTokenRepository jwtTokenRepository) {
        this.userRepository = userRepository;
        this.jwtTokenRepository = jwtTokenRepository;
    }

    /**
     * Modify user details (username, email, role)
     * @param userId User ID to modify
     * @param username New username (optional)
     * @param email New email (optional)
     * @param role New role (optional)
     * @return Updated user entity
     */
    public User modifyUser(Integer userId, String username, String email, String role) {
        // Validate user exists
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("User not found with ID: " + userId);
        }

        User existingUser = userOptional.get();

        // Update username if provided
        if (username != null && !username.trim().isEmpty()) {
            // Check if username already exists (excluding current user)
            Optional<User> existingByUsername = userRepository.findByUsername(username.trim());
            if (existingByUsername.isPresent() && !existingByUsername.get().getUserId().equals(userId)) {
                throw new IllegalArgumentException("Username already exists: " + username);
            }
            existingUser.setUsername(username.trim());
        }

        // Update email if provided
        if (email != null && !email.trim().isEmpty()) {
            // Check if email already exists (excluding current user)
            Optional<User> existingByEmail = userRepository.findByEmail(email.trim());
            if (existingByEmail.isPresent() && !existingByEmail.get().getUserId().equals(userId)) {
                throw new IllegalArgumentException("Email already exists: " + email);
            }

            // Basic email validation
            if (!email.contains("@")) {
                throw new IllegalArgumentException("Invalid email format");
            }
            
            existingUser.setEmail(email.trim());
        }

        // Update role if provided
        if (role != null && !role.trim().isEmpty()) {
            try {
                Role newRole = Role.valueOf(role.trim().toUpperCase());
                existingUser.setRole(newRole);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid role: " + role + ". Valid roles are: ADMIN, CUSTOMER");
            }
        }

        // Update timestamp
        existingUser.setUpdatedAt(LocalDateTime.now());

        // Delete associated JWT tokens for security (user needs to re-login)
        try {
            jwtTokenRepository.deleteByUserId(userId);
        } catch (Exception e) {
            // Log error but don't fail the operation
            System.err.println("Warning: Failed to delete JWT tokens for user: " + userId);
        }

        // Save and return updated user
        return userRepository.save(existingUser);
    }

    /**
     * Get user details by ID
     * @param userId User ID to retrieve
     * @return User entity
     */
    public User getUserById(Integer userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        return userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
    }
}
