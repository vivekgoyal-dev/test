package com.eshoppingzone.product.service;

import com.eshoppingzone.product.dto.ProductDto;
import com.eshoppingzone.product.entity.Product;
import com.eshoppingzone.product.repository.ProductRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public ProductDto addProducts(ProductDto dto) {
        productRepository.findByProductName(dto.getProductName()).ifPresent(p -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "product already exists: " + p.getProductName());
        });
        Product entity = toEntity(dto);
        entity.setProductId(0);
        return toDto(productRepository.save(entity));
    }

    @Override
    public List<ProductDto> getAllProducts() {
        return productRepository.findAll().stream().map(this::toDto).toList();
    }

    @Override
    public ProductDto getProductById(int productId) {
        return toDto(productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "no product " + productId)));
    }

    @Override
    public ProductDto getProductByName(String productName) {
        return toDto(productRepository.findByProductName(productName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "no product " + productName)));
    }

    @Override
    public ProductDto updateProducts(ProductDto dto) {
        if (!productRepository.existsById(dto.getProductId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "no product " + dto.getProductId());
        }
        return toDto(productRepository.save(toEntity(dto)));
    }

    @Override
    public void deleteProductById(int productId) {
        if (!productRepository.existsById(productId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "no product " + productId);
        }
        productRepository.deleteById(productId);
    }

    @Override
    public List<ProductDto> getProductByCategory(String category) {
        return productRepository.findByCategory(category).stream().map(this::toDto).toList();
    }

    @Override
    public List<ProductDto> getProductByType(String productType) {
        return productRepository.findByProductType(productType).stream().map(this::toDto).toList();
    }

    private ProductDto toDto(Product entity) {
        ProductDto dto = new ProductDto();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    private Product toEntity(ProductDto dto) {
        Product entity = new Product();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }
}
