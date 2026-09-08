package com.shoppingcart.order.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductDto {

    private Long productId;
    private String productName;
    private double price;
    private Long ownerId;
}
