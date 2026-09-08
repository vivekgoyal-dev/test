package com.shoppingcart.order.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CartItemDto {

    private Long productId;
    private String productName;
    private double price;
    private int quantity;
}
