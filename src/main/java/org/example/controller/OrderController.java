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

    @PostMapping("/confirm")
    public Order confirmOrder(@RequestHeader("userId") String userId) throws Exception {
        return orderService.confirmOrder(userId);
    }

    @GetMapping
    public List<Order> getOrders(@RequestHeader("userId") String userId) {
        return orderService.getUserOrders(userId);
    }
}
