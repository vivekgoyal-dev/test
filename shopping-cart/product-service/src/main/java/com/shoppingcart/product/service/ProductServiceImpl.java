package com.shoppingcart.product.service;

import com.shoppingcart.product.dto.ProductDto;
import com.shoppingcart.product.exception.ApiException;
import com.shoppingcart.product.model.Product;
import com.shoppingcart.product.repository.ProductRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    private static final String ADMIN = "ADMIN";

    private final ProductRepository repository;

    public ProductServiceImpl(ProductRepository repository) {
        this.repository = repository;
    }

    @Override
    public ProductDto add(ProductDto dto, Long ownerId) {
        repository.findByProductName(dto.getProductName()).ifPresent(p -> {
            throw ApiException.conflict("a product named " + p.getProductName() + " already exists");
        });
        Product product = new Product();
        BeanUtils.copyProperties(dto, product);
        product.setProductId(null);
        product.setOwnerId(ownerId);
        return toDto(repository.save(product));
    }

    @Override
    public ProductDto update(Long productId, ProductDto dto, Long callerId) {
        Product product = findOrThrow(productId);
        requireOwner(product, callerId);
        BeanUtils.copyProperties(dto, product, "productId", "ownerId");
        return toDto(repository.save(product));
    }

    @Override
    public void delete(Long productId, Long callerId, String callerRole) {
        Product product = findOrThrow(productId);
        if (!ADMIN.equals(callerRole)) {
            requireOwner(product, callerId);
        }
        repository.delete(product);
    }

    /**
     * The rule that stops one shopowner editing another one's catalog. Without it, any shopowner
     * with a valid token could rewrite the whole store.
     */
    private void requireOwner(Product product, Long callerId) {
        if (!product.getOwnerId().equals(callerId)) {
            throw new ApiException(org.springframework.http.HttpStatus.FORBIDDEN,
                    "this product belongs to another shopowner");
        }
    }

    @Override
    public ProductDto getById(Long productId) {
        return toDto(findOrThrow(productId));
    }

    @Override
    public List<ProductDto> getAll() {
        return repository.findAll().stream().map(this::toDto).toList();
    }

    @Override
    public List<ProductDto> getByCategory(String category) {
        return repository.findByCategory(category).stream().map(this::toDto).toList();
    }

    @Override
    public List<ProductDto> getByOwner(Long ownerId) {
        return repository.findByOwnerId(ownerId).stream().map(this::toDto).toList();
    }

    private Product findOrThrow(Long productId) {
        return repository.findById(productId)
                .orElseThrow(() -> ApiException.notFound("no product " + productId));
    }

    private ProductDto toDto(Product product) {
        ProductDto dto = new ProductDto();
        BeanUtils.copyProperties(product, dto);
        return dto;
    }
}
