package com.shoppingcart.cart.service;

import com.shoppingcart.cart.dto.CartDto;
import com.shoppingcart.cart.dto.CartItemDto;
import com.shoppingcart.cart.dto.ProductDto;
import com.shoppingcart.cart.exception.ApiException;
import com.shoppingcart.cart.model.CartItem;
import com.shoppingcart.cart.repository.CartItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    private final CartItemRepository repository;
    private final ProductClient productClient;

    public CartServiceImpl(CartItemRepository repository, ProductClient productClient) {
        this.repository = repository;
        this.productClient = productClient;
    }

    @Override
    public CartDto getCart(Long userId) {
        return toCart(userId, repository.findByUserId(userId));
    }

    /**
     * The name and the price are read from product-service, never from the request. A client that
     * posts its own price is ignored, which is the difference between a cart and a discount coupon
     * generator.
     */
    @Override
    @Transactional
    public CartDto addItem(Long userId, CartItemDto dto) {
        ProductDto product = lookUp(dto.getProductId());

        CartItem item = repository.findByUserIdAndProductId(userId, dto.getProductId())
                .orElseGet(CartItem::new);
        item.setUserId(userId);
        item.setProductId(product.getProductId());
        item.setProductName(product.getProductName());
        item.setPrice(product.getPrice());
        item.setQuantity(item.getId() == null ? dto.getQuantity() : item.getQuantity() + dto.getQuantity());
        repository.save(item);

        return getCart(userId);
    }

    @Override
    @Transactional
    public CartDto updateQuantity(Long userId, Long productId, int quantity) {
        if (quantity < 1) {
            throw ApiException.badRequest("quantity must be at least 1");
        }
        CartItem item = repository.findByUserIdAndProductId(userId, productId)
                .orElseThrow(() -> ApiException.notFound("product " + productId + " is not in your cart"));
        item.setQuantity(quantity);
        repository.save(item);
        return getCart(userId);
    }

    @Override
    @Transactional
    public CartDto removeItem(Long userId, Long productId) {
        repository.findByUserIdAndProductId(userId, productId)
                .orElseThrow(() -> ApiException.notFound("product " + productId + " is not in your cart"));
        repository.deleteByUserIdAndProductId(userId, productId);
        return getCart(userId);
    }

    @Override
    @Transactional
    public void clearCart(Long userId) {
        repository.deleteByUserId(userId);
    }

    private ProductDto lookUp(Long productId) {
        try {
            return productClient.getProductById(productId);
        } catch (Exception e) {
            throw ApiException.badRequest("no such product: " + productId);
        }
    }

    private CartDto toCart(Long userId, List<CartItem> items) {
        CartDto cart = new CartDto();
        cart.setUserId(userId);
        cart.setItems(items.stream().map(i -> new CartItemDto(
                i.getProductId(), i.getProductName(), i.getPrice(), i.getQuantity(),
                i.getPrice() * i.getQuantity())).toList());
        cart.setTotalPrice(cart.getItems().stream().mapToDouble(CartItemDto::getLineTotal).sum());
        return cart;
    }
}
