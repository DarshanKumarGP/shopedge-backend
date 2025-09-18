package com.shopedge.backend.services;


import com.shopedge.backend.entities.CartItem;
import com.shopedge.backend.entities.Order;
import com.shopedge.backend.entities.OrderItem;
import com.shopedge.backend.entities.OrderStatus;
import com.shopedge.backend.repositories.CartRepository;
import com.shopedge.backend.repositories.OrderItemRepository;
import com.shopedge.backend.repositories.OrderRepository;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PaymentService {
    
    @Value("${razorpay.key_id}")
    private String razorpayKeyId;
    
    @Value("${razorpay.key_secret}")
    private String razorpayKeySecret;
    
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    
    public PaymentService(OrderRepository orderRepository, 
                         OrderItemRepository orderItemRepository, 
                         CartRepository cartRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.cartRepository = cartRepository;
    }
    
    /**
     * Creates a Razorpay order and saves order details in database
     * @param userId - The authenticated user's ID
     * @param totalAmount - Total amount of the order
     * @param cartItems - List of cart items (not used in current implementation)
     * @return Razorpay order ID
     * @throws RazorpayException if order creation fails
     */
    @Transactional
    public String createOrder(int userId, BigDecimal totalAmount, List<OrderItem> cartItems) throws RazorpayException {
        // Create Razorpay client
        RazorpayClient razorpayClient = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
        
        // Prepare Razorpay order request
        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", totalAmount.multiply(BigDecimal.valueOf(100)).intValue()); // Amount in paise
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", "txn_" + System.currentTimeMillis());
        
        // Create Razorpay order
        com.razorpay.Order razorpayOrder = razorpayClient.orders.create(orderRequest);
        
        // Save order details in the database
        Order order = new Order();
        order.setOrderId(razorpayOrder.get("id"));
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());
        orderRepository.save(order);
        
        return razorpayOrder.get("id");
    }
    
    /**
     * Verifies Razorpay payment signature and processes the order
     * @param razorpayOrderId - Razorpay order ID
     * @param razorpayPaymentId - Razorpay payment ID
     * @param razorpaySignature - Razorpay signature for verification
     * @param userId - User ID for cart operations
     * @return true if payment is verified and processed successfully, false otherwise
     */
    @Transactional
    public boolean verifyPayment(String razorpayOrderId, String razorpayPaymentId, String razorpaySignature, int userId) {
        try {
            // Prepare signature validation attributes
            JSONObject attributes = new JSONObject();
            attributes.put("razorpay_order_id", razorpayOrderId);
            attributes.put("razorpay_payment_id", razorpayPaymentId);
            attributes.put("razorpay_signature", razorpaySignature);
            
            // Verify Razorpay signature
            boolean isSignatureValid = com.razorpay.Utils.verifyPaymentSignature(attributes, razorpayKeySecret);
            
            if (isSignatureValid) {
                // Update order status to SUCCESS
                Order order = orderRepository.findById(razorpayOrderId)
                    .orElseThrow(() -> new RuntimeException("Order not found"));
                order.setStatus(OrderStatus.SUCCESS);
                order.setUpdatedAt(LocalDateTime.now());
                orderRepository.save(order);
                
                // Fetch cart items for the user
                List<CartItem> cartItems = cartRepository.findCartItemsWithProductDetails(userId);
                
                // Save order items
                for (CartItem cartItem : cartItems) {
                    OrderItem orderItem = new OrderItem();
                    orderItem.setOrder(order);
                    orderItem.setProductId(cartItem.getProduct().getProductId());
                    orderItem.setQuantity(cartItem.getQuantity());
                    orderItem.setPricePerUnit(cartItem.getProduct().getPrice());
                    orderItem.setTotalPrice(
                        cartItem.getProduct().getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()))
                    );
                    orderItemRepository.save(orderItem);
                }
                
                // Clear user's cart
                cartRepository.deleteAllCartItemsByUserId(userId);
                
                return true;
            } else {
                // Update order status to FAILED if signature is invalid
                Order order = orderRepository.findById(razorpayOrderId)
                    .orElseThrow(() -> new RuntimeException("Order not found"));
                order.setStatus(OrderStatus.FAILED);
                order.setUpdatedAt(LocalDateTime.now());
                orderRepository.save(order);
                
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            
            // Update order status to FAILED in case of any exception
            try {
                Order order = orderRepository.findById(razorpayOrderId).orElse(null);
                if (order != null) {
                    order.setStatus(OrderStatus.FAILED);
                    order.setUpdatedAt(LocalDateTime.now());
                    orderRepository.save(order);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            
            return false;
        }
    }
    
    /**
     * Helper method to save order items (can be used for additional functionality)
     * @param orderId - Order ID to associate items with
     * @param items - List of order items to save
     */
    @Transactional
    public void saveOrderItems(String orderId, List<OrderItem> items) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found"));
        
        for (OrderItem item : items) {
            item.setOrder(order);
            orderItemRepository.save(item);
        }
    }
}
