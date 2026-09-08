package com.eshoppingzone.cart.service;

import com.eshoppingzone.cart.dto.CartDto;

import java.util.List;

public interface CartService {

    List<CartDto> getallcarts();

    CartDto getcartById(int cartId);

    CartDto addCart(CartDto cart);

    CartDto updateCart(CartDto cart);

    double cartTotal(CartDto cart);
}
