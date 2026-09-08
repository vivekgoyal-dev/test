package com.eshoppingzone.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemsDto {

    /** Filled in from product-service. */
    private int productId;

    private String productName;

    /** Filled in from product-service; whatever the caller sends is ignored. */
    private double price;

    private int quantity;
}
