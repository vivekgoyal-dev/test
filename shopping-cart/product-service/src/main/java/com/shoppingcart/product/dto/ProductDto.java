package com.shoppingcart.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {

    private Long productId;

    @NotBlank(message = "product name is required")
    private String productName;

    @NotBlank(message = "category is required")
    private String category;

    @Positive(message = "price must be greater than 0")
    private double price;

    @Size(max = 2000, message = "description must be at most 2000 characters")
    private String description;

    /** Set from the token on create, never accepted from the caller. */
    private Long ownerId;
}
