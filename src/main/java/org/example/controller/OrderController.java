package org.example.controller;

import org.example.model.Order;
import org.example.service.OrderService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // User confirms an order
    @PostMapping("/confirm")
    public Order confirmOrder(@RequestHeader("userId") String userId) throws Exception {
        return orderService.confirmOrder(userId);
    }

    // Get orders for a specific user
    @GetMapping
    public List<Order> getUserOrders(@RequestHeader("userId") String userId) {
        return orderService.getUserOrders(userId);
    }

    // **Admin endpoint: Get all orders**
    @GetMapping("/all")
    public List<Order> getAllOrders() {
        return orderService.getAllOrders(); // fetches all orders in DB
    }
}
