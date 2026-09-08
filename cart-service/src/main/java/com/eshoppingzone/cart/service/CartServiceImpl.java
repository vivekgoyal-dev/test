package com.eshoppingzone.cart.service;

import com.eshoppingzone.cart.dto.CartDto;
import com.eshoppingzone.cart.dto.ItemsDto;
import com.eshoppingzone.cart.dto.ProductPriceDto;
import com.eshoppingzone.cart.entity.Cart;
import com.eshoppingzone.cart.entity.Items;
import com.eshoppingzone.cart.repository.CartRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final RestTemplate restTemplate;

    public CartServiceImpl(CartRepository cartRepository, RestTemplate restTemplate) {
        this.cartRepository = cartRepository;
        this.restTemplate = restTemplate;
    }

    @Override
    public List<CartDto> getallcarts() {
        return cartRepository.findAll().stream().map(this::toDto).toList();
    }

    @Override
    public CartDto getcartById(int cartId) {
        Cart cart = cartRepository.findByCartId(cartId);
        if (cart == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "no cart " + cartId);
        }
        return toDto(cart);
    }

    @Override
    public CartDto addCart(CartDto cart) {
        if (cartRepository.existsById(cart.getCartId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "cart " + cart.getCartId() + " already exists");
        }
        return save(cart);
    }

    @Override
    public CartDto updateCart(CartDto cart) {
        if (!cartRepository.existsById(cart.getCartId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "no cart " + cart.getCartId());
        }
        return save(cart);
    }

    @Override
    public double cartTotal(CartDto cart) {
        return cart.getItems().stream().mapToDouble(i -> i.getPrice() * i.getQuantity()).sum();
    }

    /** Prices always come from product-service, never from the caller. */
    private CartDto save(CartDto dto) {
        Cart cart = new Cart();
        cart.setCartId(dto.getCartId());
        for (ItemsDto item : dto.getItems()) {
            if (item.getQuantity() < 1) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "quantity must be at least 1 for " + item.getProductName());
            }
            ProductPriceDto product = lookUp(item.getProductName());
            item.setProductId(product.getProductId());
            item.setPrice(product.getPrice());
            cart.getItems().add(new Items(product.getProductId(), product.getProductName(), product.getPrice(),
                    item.getQuantity()));
        }
        cart.setTotalPrice(cartTotal(dto));
        return toDto(cartRepository.save(cart));
    }

    private ProductPriceDto lookUp(String productName) {
        ProductPriceDto product;
        try {
            product = restTemplate.getForObject("http://product-service/products/name/{name}",
                    ProductPriceDto.class, productName);
        } catch (RestClientException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "no such product: " + productName, e);
        }
        if (product == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "no such product: " + productName);
        }
        return product;
    }

    private CartDto toDto(Cart cart) {
        CartDto dto = new CartDto();
        dto.setCartId(cart.getCartId());
        dto.setTotalPrice(cart.getTotalPrice());
        dto.setItems(cart.getItems().stream()
                .map(i -> new ItemsDto(i.getProductId(), i.getProductName(), i.getPrice(), i.getQuantity()))
                .toList());
        return dto;
    }
}
