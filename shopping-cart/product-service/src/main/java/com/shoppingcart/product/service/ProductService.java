package com.shoppingcart.product.service;

import com.shoppingcart.product.dto.ProductDto;

import java.util.List;

public interface ProductService {

    ProductDto add(ProductDto dto, Long ownerId);

    ProductDto update(Long productId, ProductDto dto, Long callerId);

    void delete(Long productId, Long callerId, String callerRole);

    ProductDto getById(Long productId);

    List<ProductDto> getAll();

    List<ProductDto> getByCategory(String category);

    List<ProductDto> getByOwner(Long ownerId);
}
