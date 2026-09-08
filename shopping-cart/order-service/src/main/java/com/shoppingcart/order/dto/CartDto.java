package com.shoppingcart.order.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/** This service's copy of the shape cart-service returns. */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CartDto {

    private Long userId;
    private List<CartItemDto> items = new ArrayList<>();
    private double totalPrice;
}
