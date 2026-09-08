package com.shoppingcart.order.service;

import com.shoppingcart.order.dto.ProductDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/** Only to learn which shopowner owns a product, so /orders/received can filter by it. */
@FeignClient(name = "product-service")
public interface ProductClient {

    @GetMapping("/products/{productId}")
    ProductDto getProductById(@PathVariable("productId") Long productId);
}
