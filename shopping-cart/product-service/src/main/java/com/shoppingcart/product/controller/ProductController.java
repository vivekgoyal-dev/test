package com.shoppingcart.product.controller;

import com.shoppingcart.product.dto.ProductDto;
import com.shoppingcart.product.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<ProductDto>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/my")
    public ResponseEntity<List<ProductDto>> getMyProducts(@RequestHeader("X-User-Id") Long ownerId) {
        return ResponseEntity.ok(service.getByOwner(ownerId));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<ProductDto>> getByCategory(@PathVariable String category) {
        return ResponseEntity.ok(service.getByCategory(category));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductDto> getById(@PathVariable Long productId) {
        return ResponseEntity.ok(service.getById(productId));
    }

    @PostMapping
    public ResponseEntity<ProductDto> add(@RequestHeader("X-User-Id") Long ownerId,
                                          @Valid @RequestBody ProductDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.add(dto, ownerId));
    }

    @PutMapping("/{productId}")
    public ResponseEntity<ProductDto> update(@PathVariable Long productId,
                                             @RequestHeader("X-User-Id") Long callerId,
                                             @Valid @RequestBody ProductDto dto) {
        return ResponseEntity.ok(service.update(productId, dto, callerId));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> delete(@PathVariable Long productId,
                                       @RequestHeader("X-User-Id") Long callerId,
                                       @RequestHeader("X-User-Role") String callerRole) {
        service.delete(productId, callerId, callerRole);
        return ResponseEntity.noContent().build();
    }
}
