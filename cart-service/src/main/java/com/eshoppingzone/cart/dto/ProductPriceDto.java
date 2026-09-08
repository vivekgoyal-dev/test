package com.eshoppingzone.cart.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/** Just the bits of product-service's response this service cares about. */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductPriceDto {

    private int productId;
    private String productName;
    private double price;
}
