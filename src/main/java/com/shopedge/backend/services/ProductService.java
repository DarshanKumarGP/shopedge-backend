package com.shopedge.backend.services;


import com.shopedge.backend.entities.Category;
import com.shopedge.backend.entities.Product;
import com.shopedge.backend.entities.ProductImage;
import com.shopedge.backend.repositories.CategoryRepository;
import com.shopedge.backend.repositories.ProductImageRepository;
import com.shopedge.backend.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private ProductImageRepository productImageRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
  
    public List<Product> getProductsByCategory(String categoryName) {
        if (categoryName != null && !categoryName.isEmpty()) {
            Optional<Category> categoryOpt = categoryRepository.findByCategoryName(categoryName);
            if (categoryOpt.isPresent()) {
                Category category = categoryOpt.get();
                return productRepository.findByCategory_CategoryId(category.getCategoryId());
            } else {
                throw new RuntimeException("Category not found: " + categoryName);
            }
        } else {
            return productRepository.findAll();
        }
    }
    
   
    public List<String> getProductImages(Integer productId) {
        List<ProductImage> productImages = productImageRepository.findByProduct_ProductId(productId);
        List<String> imageUrls = new ArrayList<>();
        
        for (ProductImage image : productImages) {
            imageUrls.add(image.getImageUrl());
        }
        
        return imageUrls;
    }
    
    /**
     * Get all categories
     * @return List of all categories
     */
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }
    
    /**
     * Get product by ID
     * @param productId The ID of the product
     * @return Product entity if found
     */
    public Optional<Product> getProductById(Integer productId) {
        return productRepository.findById(productId);
    }
    
    /**
     * Get all products
     * @return List of all products
     */
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }
}