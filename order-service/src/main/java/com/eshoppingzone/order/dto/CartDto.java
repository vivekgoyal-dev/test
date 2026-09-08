package com.eshoppingzone.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/** The cart posted at checkout; cartId is the customer's profile id. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartDto {

    private int cartId;
    private double totalPrice;
    private List<ItemsDto> items = new ArrayList<>();
}
