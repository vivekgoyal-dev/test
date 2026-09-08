package com.eshoppingzone.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartDto {

    private int cartId;
    private double totalPrice;
    private List<ItemsDto> items = new ArrayList<>();
}
