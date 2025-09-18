package com.shopedge.backend.services;

import com.shopedge.backend.entities.*;
import com.shopedge.backend.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderService {
    
    @Autowired
    private OrderItemRepository orderItemRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private ProductImageRepository productImageRepository;
    
    /**
     * Fetches all successful orders for a given user and returns the required response format.
     * This method combines data from multiple repositories to create a comprehensive order history.
     * 
     * @param user The authenticated user object
     * @return A map containing the user's role, username, and ordered products
     */
    public Map<String, Object> getOrdersForUser(User user) {
        try {
            // Fetch all successful order items for the user
            List<OrderItem> orderItems = orderItemRepository.findSuccessfulOrderItemsByUserId(user.getUserId());
            
            // Prepare the response structure
            Map<String, Object> response = new HashMap<>();
            response.put("username", user.getUsername());
            response.put("role", user.getRole().toString()); // Convert enum to string
            
            // Transform order items into a list of product details
            List<Map<String, Object>> products = new ArrayList<>();
            
            for (OrderItem item : orderItems) {
                // Fetch product details
                Product product = productRepository.findById(item.getProductId()).orElse(null);
                if (product == null) {
                    // Skip if product doesn't exist (data integrity issue)
                    continue;
                }
                
                // Fetch the first product image (if available)
                List<ProductImage> images = productImageRepository.findByProduct_ProductId(product.getProductId());
                String imageUrl = images.isEmpty() ? null : images.get(0).getImageUrl();
                
                // Create a comprehensive product details map
                Map<String, Object> productDetails = new HashMap<>();
                productDetails.put("order_id", item.getOrder().getOrderId());
                productDetails.put("quantity", item.getQuantity());
                productDetails.put("total_price", item.getTotalPrice());
                productDetails.put("image_url", imageUrl);
                productDetails.put("product_id", product.getProductId());
                productDetails.put("name", product.getName());
                productDetails.put("description", product.getDescription());
                productDetails.put("price_per_unit", item.getPricePerUnit());
                
                products.add(productDetails);
            }
            
            // Wrap products in orders structure for frontend compatibility
            Map<String, Object> ordersData = new HashMap<>();
            ordersData.put("products", products);
            response.put("orders", ordersData);
            
            return response;
            
        } catch (Exception e) {
            // Log the error for debugging
            e.printStackTrace();
            
            // Return empty response structure in case of error
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("username", user.getUsername());
            errorResponse.put("role", user.getRole().toString());
            
            Map<String, Object> emptyOrders = new HashMap<>();
            emptyOrders.put("products", new ArrayList<>());
            errorResponse.put("orders", emptyOrders);
            
            return errorResponse;
        }
    }
    
    /**
     * Get order count for a specific user (useful for analytics)
     * @param userId The user ID
     * @return Number of successful orders
     */
    public long getOrderCountForUser(int userId) {
        try {
            List<OrderItem> items = orderItemRepository.findSuccessfulOrderItemsByUserId(userId);
            return items.stream()
                    .map(item -> item.getOrder().getOrderId())
                    .distinct()
                    .count();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
    
    /**
     * Get total spending for a user (useful for user analytics)
     * @param userId The user ID
     * @return Total amount spent by user
     */
    public Double getTotalSpendingForUser(int userId) {
        try {
            List<OrderItem> items = orderItemRepository.findSuccessfulOrderItemsByUserId(userId);
            return items.stream()
                    .mapToDouble(item -> item.getTotalPrice().doubleValue())
                    .sum();
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0;
        }
    }
}
