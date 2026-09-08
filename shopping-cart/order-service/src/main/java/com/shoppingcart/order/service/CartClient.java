package com.shoppingcart.order.service;

import com.shoppingcart.order.dto.CartDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * Checkout reads the cart from its owner rather than trusting a cart posted by the client. The
 * X-User-Id header is passed on, because cart-service identifies the cart by user.
 */
@FeignClient(name = "cart-service")
public interface CartClient {

    @GetMapping("/cart")
    CartDto getCart(@RequestHeader("X-User-Id") Long userId);

    @DeleteMapping("/cart")
    void clearCart(@RequestHeader("X-User-Id") Long userId);
}
