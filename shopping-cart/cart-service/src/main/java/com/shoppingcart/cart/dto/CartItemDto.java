package com.shoppingcart.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDto {

    @NotNull(message = "productId is required")
    private Long productId;

    /** Filled in from product-service. Anything sent here is ignored. */
    private String productName;
    private double price;

    @Min(value = 1, message = "quantity must be at least 1")
    private int quantity;

    private double lineTotal;
}
