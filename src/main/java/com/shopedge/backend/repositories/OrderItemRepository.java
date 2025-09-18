package com.shopedge.backend.repositories;


import com.shopedge.backend.entities.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {
    
    // Find order items by order ID
    @Query("SELECT oi FROM OrderItem oi WHERE oi.order.orderId = :orderId")
    List<OrderItem> findByOrderId(@Param("orderId") String orderId);
    
    // Find order items by product ID
    List<OrderItem> findByProductId(int productId);
    
    // Custom query to get detailed order items with product info
    @Query("SELECT oi FROM OrderItem oi JOIN FETCH oi.order WHERE oi.order.orderId = :orderId")
    List<OrderItem> findOrderItemsWithOrderDetails(@Param("orderId") String orderId);
}
