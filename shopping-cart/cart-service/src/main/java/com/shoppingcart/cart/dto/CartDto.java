package com.shoppingcart.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartDto {

    private Long userId;
    private List<CartItemDto> items = new ArrayList<>();
    private double totalPrice;
}
