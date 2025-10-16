package org.example.controller;

import org.example.model.Cart;
import org.example.service.CartService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public Cart getCart(@RequestHeader("userId") String userId) {
        return cartService.getCart(userId);
    }

    @PostMapping("/add")
    public Cart addToCart(@RequestHeader("userId") String userId,
                          @RequestParam String productId,
                          @RequestParam int qty) throws Exception {
        return cartService.addToCart(userId, productId, qty);
    }

    @DeleteMapping("/remove")
    public Cart removeFromCart(@RequestHeader("userId") String userId,
                               @RequestParam String productId) {
        return cartService.removeFromCart(userId, productId);
    }
}
