package com.shoppingcart.product.repository;

import com.shoppingcart.product.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByCategory(String category);

    List<Product> findByOwnerId(Long ownerId);

    Optional<Product> findByProductName(String productName);
}
