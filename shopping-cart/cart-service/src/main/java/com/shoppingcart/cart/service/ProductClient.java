package com.shoppingcart.cart.service;

import com.shoppingcart.cart.dto.ProductDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * How this service talks to product-service. Feign writes the HTTP call; the name is resolved
 * through Eureka, so no port appears anywhere.
 */
@FeignClient(name = "product-service")
public interface ProductClient {

    @GetMapping("/products/{productId}")
    ProductDto getProductById(@PathVariable("productId") Long productId);
}
