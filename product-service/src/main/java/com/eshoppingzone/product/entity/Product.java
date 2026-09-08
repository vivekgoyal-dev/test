package com.eshoppingzone.product.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int productId;

    private String productType;

    @Column(unique = true)
    private String productName;

    private String category;
    private double price;

    @Column(length = 2000)
    private String description;

    /** profileId -> stars given. */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "product_rating", joinColumns = @JoinColumn(name = "product_id"))
    @MapKeyColumn(name = "user_id")
    @Column(name = "stars")
    private Map<Integer, Double> rating = new HashMap<>();

    /** profileId -> review text. */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "product_review", joinColumns = @JoinColumn(name = "product_id"))
    @MapKeyColumn(name = "user_id")
    @Column(name = "review", length = 2000)
    private Map<Integer, String> review = new HashMap<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "product_image", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "image_url")
    private List<String> image = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "product_specification", joinColumns = @JoinColumn(name = "product_id"))
    @MapKeyColumn(name = "spec_name")
    @Column(name = "spec_value")
    private Map<String, String> specification = new HashMap<>();
}
