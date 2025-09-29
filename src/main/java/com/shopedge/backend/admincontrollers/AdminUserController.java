package com.shopedge.backend.admincontrollers;

import com.shopedge.backend.entities.User;
import com.shopedge.backend.adminservices.AdminUserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Admin User Controller
 * Handles HTTP requests for admin user management operations
 * Provides endpoints for modifying and retrieving user data
 */
@RestController
@CrossOrigin(
		  origins = {"http://localhost:3000", "http://localhost:5174", "http://localhost:5173","http://localhost", "http://localhost:80","http://127.0.0.1:3000", "http://127.0.0.1:5174", "http://127.0.0.1:5173","http://127.0.0.1", "http://127.0.0.1:80"},
		  allowCredentials = "true",
		  allowedHeaders = {"Content-Type", "Authorization", "X-Timestamp", "X-Requested-With"},
		  exposedHeaders = {"Authorization", "X-Timestamp"}
		)
@RequestMapping("/admin/user")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    /**
     * Modify user details (username, email, role)
     * PUT /admin/user/modify
     */
    @PutMapping("/modify")
    public ResponseEntity<?> modifyUser(@RequestBody Map<String, Object> userRequest) {
        try {
            // Extract user modification details from request
            Integer userId = (Integer) userRequest.get("userId");
            String username = (String) userRequest.get("username");
            String email = (String) userRequest.get("email");
            String role = (String) userRequest.get("role");

            // Validate required userId
            if (userId == null) {
                return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "User ID is required"));
            }

            // Validate at least one field to update
            if ((username == null || username.trim().isEmpty()) && 
                (email == null || email.trim().isEmpty()) && 
                (role == null || role.trim().isEmpty())) {
                return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "At least one field (username, email, role) must be provided"));
            }

            // Modify user through service
            User updatedUser = adminUserService.modifyUser(userId, username, email, role);

            // Build success response
            Map<String, Object> response = new HashMap<>();
            response.put("message", "User modified successfully");
            response.put("userId", updatedUser.getUserId());
            response.put("username", updatedUser.getUsername());
            response.put("email", updatedUser.getEmail());
            response.put("role", updatedUser.getRole().name());
            response.put("createdAt", updatedUser.getCreatedAt());
            response.put("updatedAt", updatedUser.getUpdatedAt());

            return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));

        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Something went wrong while modifying the user"));
        }
    }

    /**
     * Get user details by ID
     * GET /admin/user/getbyid
     */
    @PostMapping("/getbyid")
    public ResponseEntity<?> getUserById(@RequestBody Map<String, Integer> userRequest) {
        try {
            Integer userId = userRequest.get("userId");

            if (userId == null) {
                return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "User ID is required"));
            }

            // Get user through service
            User user = adminUserService.getUserById(userId);

            // Build success response
            Map<String, Object> response = new HashMap<>();
            response.put("userId", user.getUserId());
            response.put("username", user.getUsername());
            response.put("email", user.getEmail());
            response.put("role", user.getRole().name());
            response.put("createdAt", user.getCreatedAt());
            response.put("updatedAt", user.getUpdatedAt());

            return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));

        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Something went wrong while retrieving the user"));
        }
    }
    
}
