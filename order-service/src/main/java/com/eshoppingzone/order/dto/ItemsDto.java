package com.eshoppingzone.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** One line of the cart posted at checkout. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemsDto {

    private int productId;
    private String productName;
    private double price;
    private int quantity;
}
