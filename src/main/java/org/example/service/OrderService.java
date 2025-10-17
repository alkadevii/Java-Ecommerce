package org.example.service;

import org.example.model.Cart;
import org.example.model.Order;
import org.example.model.Product;
import org.example.repository.CartRepository;
import org.example.repository.OrderRepository;
import org.example.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepo;
    private final CartService cartService;
    private final ProductRepository productRepo;

    public OrderService(OrderRepository orderRepo, CartService cartService, ProductRepository productRepo) {
        this.orderRepo = orderRepo;
        this.cartService = cartService;
        this.productRepo = productRepo;
    }

    @Transactional
    public Order confirmOrder(String userId) throws Exception {
        Cart cart = cartService.getCart(userId);
        List<Cart.CartItem> items = cart.getItems();

        if (items.isEmpty()) {
            throw new Exception("Cart is empty");
        }

        // Reduce stock
        for (Cart.CartItem item : items) {
            Product p = productRepo.findById(item.getProductId())
                    .orElseThrow(() -> new Exception("Product not found"));
            if (item.getQuantity() > p.getStock()) {
                throw new Exception("Insufficient stock for " + p.getName());
            }
            p.setStock(p.getStock() - item.getQuantity());
            productRepo.save(p);
        }

        // Create Order
        Order order = new Order();
        order.setUserId(userId);
        order.setItems(items);
        double total = items.stream().mapToDouble(i -> i.getPrice() * i.getQuantity()).sum();
        order.setTotalAmount(total);

        orderRepo.save(order);

        // Clear Cart
        cartService.clearCart(userId);

        return order;
    }

    public List<Order> getUserOrders(String userId) {
        return orderRepo.findByUserId(userId);
    }

    public List<Order> getAllOrders() {
        return orderRepo.findAll();
    }
}
