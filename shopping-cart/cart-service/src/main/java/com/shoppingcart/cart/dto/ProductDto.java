package com.shoppingcart.cart.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * Only the fields of product-service's response this service needs. ignoreUnknown means
 * product-service can add fields later without breaking this one.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductDto {

    private Long productId;
    private String productName;
    private double price;
}
