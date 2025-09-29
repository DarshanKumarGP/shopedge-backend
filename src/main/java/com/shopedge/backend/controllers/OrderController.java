package com.shopedge.backend.controllers;

import com.shopedge.backend.entities.User;
import com.shopedge.backend.services.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@CrossOrigin(
		  origins = {"http://localhost:3000", "http://localhost:5174", "http://localhost:5173","http://localhost", "http://localhost:80","http://127.0.0.1:3000", "http://127.0.0.1:5174", "http://127.0.0.1:5173","http://127.0.0.1", "http://127.0.0.1:80"},
		  allowCredentials = "true",
		  allowedHeaders = {"Content-Type", "Authorization", "X-Timestamp", "X-Requested-With"},
		  exposedHeaders = {"Authorization", "X-Timestamp"}
		)
@RequestMapping("/api/orders")
public class OrderController {
    
    @Autowired
    private OrderService orderService;
    
    /**
     * Fetches all successful orders for the authenticated user.
     * This endpoint provides comprehensive order history with product details.
     * 
     * @param request HttpServletRequest containing the authenticated user details
     * @return ResponseEntity containing the user's role, username, and their orders
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getOrdersForUser(HttpServletRequest request) {
        try {
            // Retrieve the authenticated user from the request attribute
            // This is set by the AuthenticationFilter
            User authenticatedUser = (User) request.getAttribute("authenticatedUser");
            
            // Handle unauthenticated requests
            if (authenticatedUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "User not authenticated"));
            }
            
            // Fetch orders for the user via the service layer
            Map<String, Object> response = orderService.getOrdersForUser(authenticatedUser);
            
            // Return the response with HTTP 200 OK
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            // Handle cases where user details are invalid or missing
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            // Handle unexpected exceptions
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "An unexpected error occurred while fetching orders"));
        }
    }
    
    /**
     * Get order statistics for the authenticated user
     * @param request HttpServletRequest containing user details
     * @return ResponseEntity with order statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getOrderStats(HttpServletRequest request) {
        try {
            User authenticatedUser = (User) request.getAttribute("authenticatedUser");
            
            if (authenticatedUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "User not authenticated"));
            }
            
            long orderCount = orderService.getOrderCountForUser(authenticatedUser.getUserId());
            double totalSpending = orderService.getTotalSpendingForUser(authenticatedUser.getUserId());
            
            Map<String, Object> stats = Map.of(
                "username", authenticatedUser.getUsername(),
                "total_orders", orderCount,
                "total_spending", totalSpending
            );
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to fetch order statistics"));
        }
    }
}
