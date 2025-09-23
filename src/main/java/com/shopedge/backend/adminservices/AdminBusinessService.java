package com.shopedge.backend.adminservices;

import com.shopedge.backend.entities.Order;
import com.shopedge.backend.entities.OrderItem;
import com.shopedge.backend.entities.Product;
import com.shopedge.backend.repositories.OrderItemRepository;
import com.shopedge.backend.repositories.OrderRepository;
import com.shopedge.backend.repositories.ProductRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Admin Business Service
 * Contains business logic for calculating business analytics and metrics
 * Handles daily, monthly, yearly, and overall business performance calculations
 */
@Service
public class AdminBusinessService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;

    public AdminBusinessService(
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository;
    }

    /**
     * Calculate monthly business metrics
     */
    public Map<String, Object> calculateMonthlyBusiness(int month, int year) {
        List<Order> successfulOrders = orderRepository.findSuccessfulOrdersByMonthAndYear(month, year);
        
        Map<String, Object> metrics = calculateBusinessMetrics(successfulOrders);
        metrics.put("period", "Monthly");
        metrics.put("month", month);
        metrics.put("year", year);
        metrics.put("totalOrders", successfulOrders.size());
        
        return metrics;
    }

    /**
     * Calculate daily business metrics
     */
    public Map<String, Object> calculateDailyBusiness(LocalDate date) {
        List<Order> successfulOrders = orderRepository.findSuccessfulOrdersByDate(date);
        
        Map<String, Object> metrics = calculateBusinessMetrics(successfulOrders);
        metrics.put("period", "Daily");
        metrics.put("date", date.toString());
        metrics.put("totalOrders", successfulOrders.size());
        
        return metrics;
    }

    /**
     * Calculate yearly business metrics
     */
    public Map<String, Object> calculateYearlyBusiness(int year) {
        List<Order> successfulOrders = orderRepository.findSuccessfulOrdersByYear(year);
        
        Map<String, Object> metrics = calculateBusinessMetrics(successfulOrders);
        metrics.put("period", "Yearly");
        metrics.put("year", year);
        metrics.put("totalOrders", successfulOrders.size());
        
        return metrics;
    }

    /**
     * Calculate overall business metrics (all-time)
     */
    public Map<String, Object> calculateOverallBusiness() {
        List<Order> successfulOrders = orderRepository.findAllSuccessfulOrders();
        BigDecimal totalBusiness = orderRepository.calculateOverallBusiness();
        
        Map<String, Object> response = calculateBusinessMetrics(successfulOrders);
        response.put("period", "Overall");
        response.put("totalBusiness", totalBusiness.doubleValue());
        response.put("totalOrders", successfulOrders.size());
        response.put("averageOrderValue", successfulOrders.isEmpty() ? 0.0 : 
            totalBusiness.doubleValue() / successfulOrders.size());
        
        return response;
    }

    /**
     * Calculate comprehensive business metrics from orders
     */
    private Map<String, Object> calculateBusinessMetrics(List<Order> orders) {
        double totalRevenue = 0.0;
        Map<String, Integer> categorySales = new HashMap<>();
        Map<String, Double> categoryRevenue = new HashMap<>();
        int totalItemsSold = 0;

        for (Order order : orders) {
            totalRevenue += order.getTotalAmount().doubleValue();
            
            // Get order items to analyze category-wise performance
            List<OrderItem> items = orderItemRepository.findByOrderId(order.getOrderId());
            
            for (OrderItem item : items) {
                try {
                    // Get product details to fetch price and category
                    Product product = productRepository.findById(item.getProductId()).orElse(null);
                    
                    if (product != null && product.getCategory() != null) {
                        String categoryName = product.getCategory().getCategoryName();
                        
                        // Update category sales quantity
                        categorySales.put(categoryName, 
                            categorySales.getOrDefault(categoryName, 0) + item.getQuantity());
                        
                        // Calculate item revenue using product price * quantity
                        double itemRevenue = product.getPrice().doubleValue() * item.getQuantity();
                        categoryRevenue.put(categoryName, 
                            categoryRevenue.getOrDefault(categoryName, 0.0) + itemRevenue);
                        
                        totalItemsSold += item.getQuantity();
                    }
                } catch (Exception e) {
                    // Log error but continue processing other items
                    System.err.println("Error processing order item: " + item.getProductId() + " - " + e.getMessage());
                }
            }
        }

        // Build comprehensive metrics response
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalRevenue", Math.round(totalRevenue * 100.0) / 100.0);
        metrics.put("categorySales", categorySales);
        metrics.put("categoryRevenue", categoryRevenue);
        metrics.put("totalItemsSold", totalItemsSold);
        metrics.put("uniqueCategories", categorySales.size());
        
        // Calculate top performing category by quantity
        String topCategoryByQuantity = categorySales.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("N/A");
        metrics.put("topPerformingCategory", topCategoryByQuantity);
        
        // Calculate top performing category by revenue
        String topCategoryByRevenue = categoryRevenue.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("N/A");
        metrics.put("topRevenueCategory", topCategoryByRevenue);
        
        return metrics;
    }
}
