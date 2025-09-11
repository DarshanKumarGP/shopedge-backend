package com.shopedge.backend.entities;


import jakarta.persistence.*;

@Entity
@Table(name = "productimages")
public class ProductImage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer imageId;
    
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String imageUrl;
    
    // Default Constructor
    public ProductImage() {
    }
    
    // Constructor without ID
    public ProductImage(Product product, String imageUrl) {
        this.product = product;
        this.imageUrl = imageUrl;
    }
    
    // Constructor with ID
    public ProductImage(Integer imageId, Product product, String imageUrl) {
        this.imageId = imageId;
        this.product = product;
        this.imageUrl = imageUrl;
    }
    
    // Getters and Setters
    public Integer getImageId() {
        return imageId;
    }
    
    public void setImageId(Integer imageId) {
        this.imageId = imageId;
    }
    
    public Product getProduct() {
        return product;
    }
    
    public void setProduct(Product product) {
        this.product = product;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}

