package com.shopedge.backend.adminservices;


import com.shopedge.backend.entities.Category;
import com.shopedge.backend.entities.Product;
import com.shopedge.backend.entities.ProductImage;
import com.shopedge.backend.repositories.CategoryRepository;
import com.shopedge.backend.repositories.ProductImageRepository;
import com.shopedge.backend.repositories.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Admin Product Service
 * Contains business logic for admin product management operations
 * Handles product addition and deletion with associated images
 */
@Service
@Transactional
public class AdminProductService {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final CategoryRepository categoryRepository;

    public AdminProductService(
            ProductRepository productRepository,
            ProductImageRepository productImageRepository,
            CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.productImageRepository = productImageRepository;
        this.categoryRepository = categoryRepository;
    }

    /**
     * Add new product with image
     * @param name Product name
     * @param description Product description
     * @param price Product price
     * @param stock Product stock quantity
     * @param categoryId Category ID
     * @param imageUrl Product image URL
     * @return Saved product entity
     */
    public Product addProductWithImage(
            String name,
            String description,
            Double price,
            Integer stock,
            Integer categoryId,
            String imageUrl) {

        // Validate category exists
        Optional<Category> categoryOpt = categoryRepository.findById(categoryId);
        if (categoryOpt.isEmpty()) {
            throw new IllegalArgumentException("Invalid category ID: " + categoryId);
        }

        // Validate required fields
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be empty");
        }

        if (price == null || price <= 0) {
            throw new IllegalArgumentException("Product price must be greater than 0");
        }

        if (stock == null || stock < 0) {
            throw new IllegalArgumentException("Product stock cannot be negative");
        }

        // Create and populate product entity
        Product product = new Product();
        product.setName(name.trim());
        product.setDescription(description != null ? description.trim() : "");
        product.setPrice(BigDecimal.valueOf(price));
        product.setStock(stock);
        product.setCategory(categoryOpt.get());
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());

        // Save product to database
        Product savedProduct = productRepository.save(product);

        // Create and save product image if provided
        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            ProductImage productImage = new ProductImage();
            productImage.setProduct(savedProduct);
            productImage.setImageUrl(imageUrl.trim());
            productImageRepository.save(productImage);
        } else {
            throw new IllegalArgumentException("Product image URL cannot be empty");
        }

        return savedProduct;
    }

    /**
     * Delete product and associated images
     * @param productId Product ID to delete
     */
    public void deleteProduct(Integer productId) {
        // Validate product exists
        if (!productRepository.existsById(productId)) {
            throw new IllegalArgumentException("Product not found with ID: " + productId);
        }

        try {
            // Delete associated product images first (foreign key constraint)
            productImageRepository.deleteByProductId(productId);

            // Delete the product
            productRepository.deleteById(productId);

        } catch (Exception e) {
            throw new RuntimeException("Failed to delete product: " + e.getMessage(), e);
        }
    }
}
