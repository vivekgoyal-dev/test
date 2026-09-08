package com.eshoppingzone.product.service;

import com.eshoppingzone.product.dto.ProductDto;

import java.util.List;

public interface ProductService {

    ProductDto addProducts(ProductDto product);

    List<ProductDto> getAllProducts();

    ProductDto getProductById(int productId);

    ProductDto getProductByName(String productName);

    ProductDto updateProducts(ProductDto product);

    void deleteProductById(int productId);

    List<ProductDto> getProductByCategory(String category);

    List<ProductDto> getProductByType(String productType);
}
