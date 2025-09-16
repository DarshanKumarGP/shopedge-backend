package com.shopedge.backend.services;

import com.shopedge.backend.entities.CartItem;
import com.shopedge.backend.entities.Product;
import com.shopedge.backend.entities.ProductImage;
import com.shopedge.backend.entities.User;
import com.shopedge.backend.repositories.CartRepository;
import com.shopedge.backend.repositories.ProductImageRepository;
import com.shopedge.backend.repositories.ProductRepository;
import com.shopedge.backend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CartService {
    
    @Autowired
    private CartRepository cartRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private ProductImageRepository productImageRepository;
    
    /**
     * Add product to cart or increment quantity if already exists
     */
    public void addToCart(Integer userId, Integer productId, Integer quantity) {
        // Validate user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        
        // Validate product exists
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + productId));
        
        // Check if item already exists in cart
        Optional<CartItem> existingItem = cartRepository.findByUserAndProduct(userId, productId);
        
        if (existingItem.isPresent()) {
            // Item exists, increment quantity
            CartItem cartItem = existingItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
            cartRepository.save(cartItem);
        } else {
            // Item doesn't exist, create new cart item
            CartItem newItem = new CartItem(user, product, quantity);
            cartRepository.save(newItem);
        }
    }
    
    /**
     * Get total count of items in user's cart
     */
    public Integer getCartItemCount(Integer userId) {
        return cartRepository.countTotalItems(userId);
    }
    
    /**
     * Get detailed cart items for a user with product information and calculations
     */
    public Map<String, Object> getCartItems(Integer userId) {
        // Fetch cart items with product details
        List<CartItem> cartItems = cartRepository.findCartItemsWithProductDetails(userId);
        
        // Get user details
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        // Create response structure
        Map<String, Object> response = new HashMap<>();
        response.put("username", user.getUsername());
        response.put("role", user.getRole().toString());
        
        // Process cart items
        List<Map<String, Object>> products = new ArrayList<>();
        double overallTotalPrice = 0;
        
        for (CartItem cartItem : cartItems) {
            Map<String, Object> productDetails = new HashMap<>();
            Product product = cartItem.getProduct();
            
            // Fetch product images
            List<ProductImage> productImages = productImageRepository.findByProduct_ProductId(product.getProductId());
            String imageUrl = (productImages != null && !productImages.isEmpty())
                    ? productImages.get(0).getImageUrl()
                    : "https://via.placeholder.com/400?text=No+Image";
            
            // Calculate total price for this item
            double itemTotalPrice = cartItem.getQuantity() * product.getPrice().doubleValue();
            
            // Populate product details
            productDetails.put("product_id", product.getProductId());
            productDetails.put("image_url", imageUrl);
            productDetails.put("name", product.getName());
            productDetails.put("description", product.getDescription());
            productDetails.put("price_per_unit", product.getPrice());
            productDetails.put("quantity", cartItem.getQuantity());
            productDetails.put("total_price", itemTotalPrice);
            
            products.add(productDetails);
            overallTotalPrice += itemTotalPrice;
        }
        
        // Create cart object
        Map<String, Object> cart = new HashMap<>();
        cart.put("products", products);
        cart.put("overall_total_price", overallTotalPrice);
        
        response.put("cart", cart);
        return response;
    }
    
    /**
     * Update cart item quantity
     */
    public void updateCartItemQuantity(Integer userId, Integer productId, Integer quantity) {
        // Validate user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        // Validate product exists
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        
        // Find existing cart item
        Optional<CartItem> existingItem = cartRepository.findByUserAndProduct(userId, productId);
        
        if (existingItem.isPresent()) {
            CartItem cartItem = existingItem.get();
            
            if (quantity == 0) {
                // Delete item if quantity is 0
                deleteCartItem(userId, productId);
            } else {
                // Update quantity
                cartItem.setQuantity(quantity);
                cartRepository.save(cartItem);
            }
        } else {
            throw new IllegalArgumentException("Cart item not found for user and product");
        }
    }
    
    /**
     * Delete cart item
     */
    public void deleteCartItem(Integer userId, Integer productId) {
        // Validate user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        // Validate product exists
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        
        // Delete cart item
        cartRepository.deleteCartItem(userId, productId);
    }
}
