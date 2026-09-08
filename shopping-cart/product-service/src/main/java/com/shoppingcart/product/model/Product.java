package com.shoppingcart.product.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "product")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;

    @Column(nullable = false, unique = true)
    private String productName;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private double price;

    @Column(length = 2000)
    private String description;

    /** The shopowner who listed it, taken from the token. Ownership is checked against this. */
    @Column(nullable = false)
    private Long ownerId;
}
