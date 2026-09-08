package com.eshoppingzone.product.resource;

import com.eshoppingzone.product.dto.ProductDto;
import com.eshoppingzone.product.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductResource {

    private final ProductService productService;

    public ProductResource(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<ProductDto> addProducts(@RequestBody ProductDto product) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.addProducts(product));
    }

    @GetMapping
    public ResponseEntity<List<ProductDto>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductDto> getProductById(@PathVariable int productId) {
        return ResponseEntity.ok(productService.getProductById(productId));
    }

    @GetMapping("/name/{productName}")
    public ResponseEntity<ProductDto> getProductByName(@PathVariable String productName) {
        return ResponseEntity.ok(productService.getProductByName(productName));
    }

    @GetMapping("/type/{productType}")
    public ResponseEntity<List<ProductDto>> getProductByType(@PathVariable String productType) {
        return ResponseEntity.ok(productService.getProductByType(productType));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<ProductDto>> getProductsByCategory(@PathVariable String category) {
        return ResponseEntity.ok(productService.getProductByCategory(category));
    }

    @PutMapping
    public ResponseEntity<ProductDto> updateProduct(@RequestBody ProductDto product) {
        return ResponseEntity.ok(productService.updateProducts(product));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(@PathVariable int productId) {
        productService.deleteProductById(productId);
        return ResponseEntity.noContent().build();
    }
}
