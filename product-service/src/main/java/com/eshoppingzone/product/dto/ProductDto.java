package com.eshoppingzone.product.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {

    private int productId;
    private String productType;
    private String productName;
    private String category;
    private double price;
    private String description;
    private Map<Integer, Double> rating = new HashMap<>();
    private Map<Integer, String> review = new HashMap<>();
    private List<String> image = new ArrayList<>();
    private Map<String, String> specification = new HashMap<>();
}
