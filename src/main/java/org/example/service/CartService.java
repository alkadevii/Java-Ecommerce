package org.example.service;

import org.example.model.Cart;
import org.example.model.Product;
import org.example.repository.CartRepository;
import org.example.repository.ProductRepository;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class CartService {

    private final CartRepository cartRepo;
    private final ProductRepository productRepo;

    public CartService(CartRepository cartRepo, ProductRepository productRepo) {
        this.cartRepo = cartRepo;
        this.productRepo = productRepo;
    }

    public Cart getCart(String userId) {
        return cartRepo.findByUserId(userId).orElseGet(() -> {
            Cart cart = new Cart();
            cart.setUserId(userId);
            return cartRepo.save(cart);
        });
    }

    public Cart addToCart(String userId, String productId, int qty) throws Exception {
        Cart cart = getCart(userId);
        Product p = productRepo.findById(productId)
                .orElseThrow(() -> new Exception("Product not found"));

        if (qty <= 0 || qty > p.getStock()) {
            throw new Exception("Invalid quantity");
        }

        cart.addItem(new Cart.CartItem(productId, p.getName(), p.getPrice(), qty));
        return cartRepo.save(cart);
    }

    public Cart removeFromCart(String userId, String productId) {
        Cart cart = getCart(userId);
        cart.removeItem(productId);
        return cartRepo.save(cart);
    }

    public void clearCart(String userId) {
        Cart cart = getCart(userId);
        cart.clear();
        cartRepo.save(cart);
    }
}
