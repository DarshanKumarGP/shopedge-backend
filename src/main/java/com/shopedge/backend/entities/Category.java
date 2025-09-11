package com.shopedge.backend.entities;


import jakarta.persistence.*;

@Entity
@Table(name = "categories")
public class Category {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer categoryId;
    
    @Column(nullable = false, unique = true)
    private String categoryName;
    
    // Default Constructor
    public Category() {
    }
    
    // Constructor without ID
    public Category(String categoryName) {
        this.categoryName = categoryName;
    }
    
    // Constructor with ID
    public Category(Integer categoryId, String categoryName) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
    }
    
    // Getters and Setters
    public Integer getCategoryId() {
        return categoryId;
    }
    
    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }
    
    public String getCategoryName() {
        return categoryName;
    }
    
    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
}

