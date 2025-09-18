package com.shopedge.backend.repositories;


import com.shopedge.backend.entities.Order;
import com.shopedge.backend.entities.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    
    // Find orders by user ID
    List<Order> findByUserId(int userId);
    
    // Find orders by status
    List<Order> findByStatus(OrderStatus status);
    
    // Find orders by user ID and status
    List<Order> findByUserIdAndStatus(int userId, OrderStatus status);
    
    // Custom query to find orders with their items
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderItems WHERE o.orderId = :orderId")
    Optional<Order> findOrderWithItems(@Param("orderId") String orderId);
    
    // Find recent orders for a user
    @Query("SELECT o FROM Order o WHERE o.userId = :userId ORDER BY o.createdAt DESC")
    List<Order> findRecentOrdersByUserId(@Param("userId") int userId);
}
