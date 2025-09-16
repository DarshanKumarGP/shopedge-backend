package com.shopedge.backend.controllers;

import com.shopedge.backend.entities.User;
import com.shopedge.backend.repositories.UserRepository;
import com.shopedge.backend.services.CartService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5174", "http://localhost:5173"}, allowCredentials = "true")
@RequestMapping("/api/cart")
public class CartController {
    
    @Autowired
    private CartService cartService;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Add product to cart
     * POST /api/cart/add
     */
    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addToCart(@RequestBody Map<String, Object> request) {
        try {
            // Extract request parameters
            String username = (String) request.get("username");
            Integer productId = (Integer) request.get("productId");
            Integer quantity = request.containsKey("quantity") ? (Integer) request.get("quantity") : 1;
            
            // Fetch user
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("User not found with username: " + username));
            
            // Add to cart
            cartService.addToCart(user.getUserId(), productId, quantity);
            
            // Get updated cart count
            Integer cartCount = cartService.getCartItemCount(user.getUserId());
            
            // Response
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Product added to cart successfully");
            response.put("cartCount", cartCount);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Get cart item count
     * GET /api/cart/items/count
     */
    @GetMapping("/items/count")
    public ResponseEntity<Map<String, Object>> getCartItemCount(@RequestParam String username) {
        try {
            // Fetch user
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("User not found with username: " + username));
            
            // Get cart count
            Integer cartCount = cartService.getCartItemCount(user.getUserId());
            
            // Response
            Map<String, Object> response = new HashMap<>();
            response.put("cartCount", cartCount);
            response.put("username", username);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Get detailed cart items
     * GET /api/cart/items
     */
    @GetMapping("/items")
    public ResponseEntity<Map<String, Object>> getCartItems(HttpServletRequest request) {
        try {
            // Get authenticated user from request attribute (set by authentication filter)
            User user = (User) request.getAttribute("authenticatedUser");
            
            if (user == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "User not authenticated");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }
            
            // Get cart items
            Map<String, Object> cartItems = cartService.getCartItems(user.getUserId());
            
            return ResponseEntity.ok(cartItems);
            
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Update cart item quantity
     * PUT /api/cart/update
     */
    @PutMapping("/update")
    public ResponseEntity<Map<String, Object>> updateCartItemQuantity(@RequestBody Map<String, Object> request) {
        try {
            // Extract request parameters
            String username = (String) request.get("username");
            Integer productId = (Integer) request.get("productId");
            Integer quantity = (Integer) request.get("quantity");
            
            // Fetch user
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("User not found with username: " + username));
            
            // Update cart item quantity
            cartService.updateCartItemQuantity(user.getUserId(), productId, quantity);
            
            // Response
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Cart item quantity updated successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Delete cart item
     * DELETE /api/cart/delete
     */
    @DeleteMapping("/delete")
    public ResponseEntity<Map<String, Object>> deleteCartItem(@RequestBody Map<String, Object> request) {
        try {
            // Extract request parameters
            String username = (String) request.get("username");
            Integer productId = (Integer) request.get("productId");
            
            // Fetch user
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("User not found with username: " + username));
            
            // Delete cart item
            cartService.deleteCartItem(user.getUserId(), productId);
            
            // Response
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Cart item deleted successfully");
            
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
            
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
