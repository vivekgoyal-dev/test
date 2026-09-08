package com.eshoppingzone.cart.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class Items {

    private int productId;
    private String productName;
    private double price;
    private int quantity;
}
