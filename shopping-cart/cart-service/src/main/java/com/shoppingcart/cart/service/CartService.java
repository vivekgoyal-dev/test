package com.shoppingcart.cart.service;

import com.shoppingcart.cart.dto.CartDto;
import com.shoppingcart.cart.dto.CartItemDto;

public interface CartService {

    CartDto getCart(Long userId);

    CartDto addItem(Long userId, CartItemDto dto);

    CartDto updateQuantity(Long userId, Long productId, int quantity);

    CartDto removeItem(Long userId, Long productId);

    void clearCart(Long userId);
}
