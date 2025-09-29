package com.shopedge.backend.admincontrollers;


import com.shopedge.backend.entities.Product;
import com.shopedge.backend.adminservices.AdminProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Admin Product Controller
 * Handles HTTP requests for admin product management operations
 * Provides endpoints for adding and deleting products
 */
@RestController
@CrossOrigin(
		  origins = {"http://localhost:3000", "http://localhost:5174", "http://localhost:5173","http://localhost", "http://localhost:80","http://127.0.0.1:3000", "http://127.0.0.1:5174", "http://127.0.0.1:5173","http://127.0.0.1", "http://127.0.0.1:80"},
		  allowCredentials = "true",
		  allowedHeaders = {"Content-Type", "Authorization", "X-Timestamp", "X-Requested-With"},
		  exposedHeaders = {"Authorization", "X-Timestamp"}
		)
@RequestMapping("/admin/products")
public class AdminProductController {

    private final AdminProductService adminProductService;

    public AdminProductController(AdminProductService adminProductService) {
        this.adminProductService = adminProductService;
    }

    /**
     * Add new product with image
     * POST /admin/products/add
     */
    @PostMapping("/add")
    public ResponseEntity<?> addProduct(@RequestBody Map<String, Object> productRequest) {
        try {
            // Extract product details from request
            String name = (String) productRequest.get("name");
            String description = (String) productRequest.get("description");
            Double price = Double.valueOf(String.valueOf(productRequest.get("price")));
            Integer stock = (Integer) productRequest.get("stock");
            Integer categoryId = (Integer) productRequest.get("categoryId");
            String imageUrl = (String) productRequest.get("imageUrl");

            // Validate required fields
            if (name == null || name.trim().isEmpty()) {
                return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Product name cannot be empty");
            }

            if (price == null || price <= 0) {
                return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Product price must be greater than 0");
            }

            if (categoryId == null) {
                return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Category ID is required");
            }

            // Add product through service
            Product addedProduct = adminProductService.addProductWithImage(
                name, description, price, stock, categoryId, imageUrl
            );

            // Success response
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Product added successfully");
            response.put("product", addedProduct);

            return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));

        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Something went wrong while adding the product"));
        }
    }

    /**
     * Delete existing product
     * DELETE /admin/products/delete
     */
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteProduct(@RequestBody Map<String, Integer> requestBody) {
        try {
            Integer productId = requestBody.get("productId");

            if (productId == null) {
                return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Product ID is required"));
            }

            // Delete product through service
            adminProductService.deleteProduct(productId);

            // Success response
            return ResponseEntity
                .status(HttpStatus.OK)
                .body(Map.of("message", "Product deleted successfully"));

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));

        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Something went wrong while deleting the product"));
        }
    }
}
