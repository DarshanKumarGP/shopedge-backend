package com.shopedge.backend.repositories;

import com.shopedge.backend.entities.Order;
import com.shopedge.backend.entities.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Order Repository
 * Handles database operations for Order entity
 * Includes methods for customer services and admin business analytics
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    
    // ===================================
    // EXISTING CUSTOMER SERVICE METHODS
    // ===================================
    
    /**
     * Find orders by user ID
     * @param userId User ID
     * @return List of orders for the user
     */
    List<Order> findByUserId(int userId);
    
    /**
     * Find orders by status
     * @param status Order status enum
     * @return List of orders with the specified status
     */
    List<Order> findByStatus(OrderStatus status);
    
    /**
     * Find orders by user ID and status
     * @param userId User ID
     * @param status Order status enum
     * @return List of orders for the user with specified status
     */
    List<Order> findByUserIdAndStatus(int userId, OrderStatus status);
    
    /**
     * Custom query to find orders with their items (eager loading)
     * @param orderId Order ID
     * @return Optional Order with loaded order items
     */
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderItems WHERE o.orderId = :orderId")
    Optional<Order> findOrderWithItems(@Param("orderId") String orderId);
    
    /**
     * Find recent orders for a user (sorted by creation date)
     * @param userId User ID
     * @return List of recent orders for the user
     */
    @Query("SELECT o FROM Order o WHERE o.userId = :userId ORDER BY o.createdAt DESC")
    List<Order> findRecentOrdersByUserId(@Param("userId") int userId);
    
    // =========================================
    // NEW ADMIN BUSINESS ANALYTICS METHODS
    // =========================================
    
    /**
     * Find successful orders by month and year
     * @param month Month (1-12)
     * @param year Year (e.g., 2024)
     * @return List of successful orders for the specified month and year
     */
    @Query("SELECT o FROM Order o WHERE MONTH(o.createdAt) = :month AND YEAR(o.createdAt) = :year AND o.status = com.shopedge.backend.entities.OrderStatus.SUCCESS")
    List<Order> findSuccessfulOrdersByMonthAndYear(@Param("month") int month, @Param("year") int year);

    /**
     * Find successful orders by specific date
     * @param date Specific date for filtering
     * @return List of successful orders for the given date
     */
    @Query("SELECT o FROM Order o WHERE DATE(o.createdAt) = :date AND o.status = com.shopedge.backend.entities.OrderStatus.SUCCESS")
    List<Order> findSuccessfulOrdersByDate(@Param("date") LocalDate date);

    /**
     * Find successful orders by year
     * @param year Year for filtering
     * @return List of successful orders for the given year
     */
    @Query("SELECT o FROM Order o WHERE YEAR(o.createdAt) = :year AND o.status = com.shopedge.backend.entities.OrderStatus.SUCCESS")
    List<Order> findSuccessfulOrdersByYear(@Param("year") int year);

    /**
     * Calculate total business revenue from all successful orders
     * @return Total revenue from all successful orders
     */
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status = com.shopedge.backend.entities.OrderStatus.SUCCESS")
    BigDecimal calculateOverallBusiness();

    /**
     * Find all successful orders (for overall business analysis)
     * @return List of all successful orders
     */
    @Query("SELECT o FROM Order o WHERE o.status = com.shopedge.backend.entities.OrderStatus.SUCCESS")
    List<Order> findAllSuccessfulOrders();

    /**
     * Find all orders by status (string version - kept for backward compatibility)
     * @param status Order status as string
     * @return List of orders with the specified status
     */
    @Query("SELECT o FROM Order o WHERE o.status = :status")
    List<Order> findAllByStatus(@Param("status") String status);

    /**
     * Count total successful orders
     * @return Total number of successful orders
     */
    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = com.shopedge.backend.entities.OrderStatus.SUCCESS")
    Long countSuccessfulOrders();
    
    // ========================================
    // ADDITIONAL USEFUL ADMIN ANALYTICS METHODS
    // ========================================
    
    /**
     * Find orders within a date range with specific status
     * @param startDate Start date
     * @param endDate End date
     * @param status Order status
     * @return List of orders within date range with specified status
     */
    @Query("SELECT o FROM Order o WHERE DATE(o.createdAt) BETWEEN :startDate AND :endDate AND o.status = :status")
    List<Order> findOrdersByDateRangeAndStatus(
        @Param("startDate") LocalDate startDate, 
        @Param("endDate") LocalDate endDate, 
        @Param("status") OrderStatus status
    );
    
    /**
     * Get monthly order count for a specific year
     * @param year Year for analysis
     * @return List of monthly order counts
     */
    @Query("SELECT MONTH(o.createdAt) as month, COUNT(o) as orderCount " +
           "FROM Order o WHERE YEAR(o.createdAt) = :year AND o.status = com.shopedge.backend.entities.OrderStatus.SUCCESS " +
           "GROUP BY MONTH(o.createdAt) ORDER BY MONTH(o.createdAt)")
    List<Object[]> getMonthlyOrderCounts(@Param("year") int year);
    
    /**
     * Get daily order count for a specific month
     * @param month Month (1-12)
     * @param year Year
     * @return List of daily order counts
     */
    @Query("SELECT DAY(o.createdAt) as day, COUNT(o) as orderCount " +
           "FROM Order o WHERE MONTH(o.createdAt) = :month AND YEAR(o.createdAt) = :year " +
           "AND o.status = com.shopedge.backend.entities.OrderStatus.SUCCESS " +
           "GROUP BY DAY(o.createdAt) ORDER BY DAY(o.createdAt)")
    List<Object[]> getDailyOrderCounts(@Param("month") int month, @Param("year") int year);
    
    /**
     * Calculate total revenue by status
     * @param status Order status
     * @return Total revenue for orders with specified status
     */
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status = :status")
    BigDecimal calculateRevenueByStatus(@Param("status") OrderStatus status);
    
    /**
     * Find top customers by order count
     * @param limit Number of top customers to return
     * @return List of user IDs with their order counts
     */
    @Query("SELECT o.userId, COUNT(o) as orderCount " +
           "FROM Order o WHERE o.status = com.shopedge.backend.entities.OrderStatus.SUCCESS " +
           "GROUP BY o.userId ORDER BY COUNT(o) DESC")
    List<Object[]> findTopCustomersByOrderCount(@Param("limit") int limit);
    
    /**
     * Find orders by user with pagination support
     * @param userId User ID
     * @return List of orders for the user (can be used with Pageable)
     */
    @Query("SELECT o FROM Order o WHERE o.userId = :userId ORDER BY o.createdAt DESC")
    List<Order> findOrdersByUserIdOrderedByDate(@Param("userId") int userId);
}
