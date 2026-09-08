package com.shoppingcart.cart.controller;

import com.shoppingcart.cart.dto.CartDto;
import com.shoppingcart.cart.dto.CartItemDto;
import com.shoppingcart.cart.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
public class CartController {

    private final CartService service;

    public CartController(CartService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<CartDto> getCart(@RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(service.getCart(userId));
    }

    @PostMapping("/items")
    public ResponseEntity<CartDto> addItem(@RequestHeader("X-User-Id") Long userId,
                                           @Valid @RequestBody CartItemDto dto) {
        return ResponseEntity.ok(service.addItem(userId, dto));
    }

    @PutMapping("/items/{productId}")
    public ResponseEntity<CartDto> updateQuantity(@RequestHeader("X-User-Id") Long userId,
                                                  @PathVariable Long productId,
                                                  @RequestParam int quantity) {
        return ResponseEntity.ok(service.updateQuantity(userId, productId, quantity));
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<CartDto> removeItem(@RequestHeader("X-User-Id") Long userId,
                                              @PathVariable Long productId) {
        return ResponseEntity.ok(service.removeItem(userId, productId));
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart(@RequestHeader("X-User-Id") Long userId) {
        service.clearCart(userId);
        return ResponseEntity.noContent().build();
    }
}
