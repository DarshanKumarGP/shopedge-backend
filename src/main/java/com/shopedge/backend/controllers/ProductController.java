package com.shopedge.backend.controllers;


import com.shopedge.backend.entities.Category;
import com.shopedge.backend.entities.Product;
import com.shopedge.backend.entities.User;
import com.shopedge.backend.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5174" }, allowCredentials = "true")
@RequestMapping("/api/products")
public class ProductController {
    
    @Autowired
    private ProductService productService;
    
    /**
     * Get products with user authentication and structured response
     * @param category Optional category filter parameter
     * @param request HttpServletRequest to get authenticated user
     * @return Structured response with user info and products
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getProducts(
            @RequestParam(required = false) String category,
            HttpServletRequest request) {
        try {
            // Retrieve authenticated user from the request attribute set by the filter
            User authenticatedUser = (User) request.getAttribute("authenticatedUser");
            if (authenticatedUser == null) {
                return ResponseEntity.status(401)
                        .body(Map.of("error", "Unauthorized access"));
            }
            
            // Fetch products based on the category filter
            List<Product> products = productService.getProductsByCategory(category);
            
            // Build the response
            Map<String, Object> response = new HashMap<>();
            
            // Add user info
            Map<String, String> userInfo = new HashMap<>();
            userInfo.put("name", authenticatedUser.getUsername());
            userInfo.put("role", authenticatedUser.getRole().name());
            response.put("user", userInfo);
            
            // Add product details
            List<Map<String, Object>> productList = new ArrayList<>();
            for (Product product : products) {
                Map<String, Object> productDetails = new HashMap<>();
                productDetails.put("product_id", product.getProductId());
                productDetails.put("name", product.getName());
                productDetails.put("description", product.getDescription());
                productDetails.put("price", product.getPrice());
                productDetails.put("stock", product.getStock());
                
                // Fetch product images
                List<String> images = productService.getProductImages(product.getProductId());
                productDetails.put("images", images);
                
                productList.add(productDetails);
            }
            
            response.put("products", productList);
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Get single product by ID with authentication
     * @param productId The ID of the product
     * @param request HttpServletRequest to get authenticated user
     * @return Product details with user info
     */
    @GetMapping("/{productId}")
    public ResponseEntity<Map<String, Object>> getProductById(
            @PathVariable Integer productId,
            HttpServletRequest request) {
        try {
            // Check authentication
            User authenticatedUser = (User) request.getAttribute("authenticatedUser");
            if (authenticatedUser == null) {
                return ResponseEntity.status(401)
                        .body(Map.of("error", "Unauthorized access"));
            }
            
            return productService.getProductById(productId)
                    .map(product -> {
                        Map<String, Object> response = new HashMap<>();
                        
                        // Add user info
                        Map<String, String> userInfo = new HashMap<>();
                        userInfo.put("name", authenticatedUser.getUsername());
                        userInfo.put("role", authenticatedUser.getRole().name());
                        response.put("user", userInfo);
                        
                        // Add product details
                        Map<String, Object> productDetails = new HashMap<>();
                        productDetails.put("product_id", product.getProductId());
                        productDetails.put("name", product.getName());
                        productDetails.put("description", product.getDescription());
                        productDetails.put("price", product.getPrice());
                        productDetails.put("stock", product.getStock());
                        productDetails.put("images", productService.getProductImages(productId));
                        
                        response.put("product", productDetails);
                        return ResponseEntity.ok(response);
                    })
                    .orElse(ResponseEntity.notFound().build());
                    
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/categories")
    public ResponseEntity<List<Category>> getCategories(HttpServletRequest request) {
        User authenticatedUser = (User) request.getAttribute("authenticatedUser");
        if (authenticatedUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Example: return category names, or you can return Category objects
        List<Category> categories = productService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

}