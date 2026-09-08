package com.eshoppingzone.cart.resource;

import com.eshoppingzone.cart.dto.CartDto;
import com.eshoppingzone.cart.service.CartService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/carts")
public class CartResource {

    private final CartService service;

    public CartResource(CartService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<CartDto>> getAllCarts() {
        return ResponseEntity.ok(service.getallcarts());
    }

    /** Opens an empty cart; the cart id is the customer's profile id. */
    @PostMapping("/{cartId}")
    public ResponseEntity<CartDto> addCart(@PathVariable int cartId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.addCart(new CartDto(cartId, 0, new ArrayList<>())));
    }

    @GetMapping("/{cartId}")
    public ResponseEntity<CartDto> getCart(@PathVariable int cartId) {
        return ResponseEntity.ok(service.getcartById(cartId));
    }

    /** Add or remove products by sending the full item list the cart should hold. */
    @PutMapping
    public ResponseEntity<CartDto> updateCart(@RequestBody CartDto cart) {
        return ResponseEntity.ok(service.updateCart(cart));
    }

    @GetMapping("/{cartId}/total")
    public ResponseEntity<Double> cartTotal(@PathVariable int cartId) {
        return ResponseEntity.ok(service.cartTotal(service.getcartById(cartId)));
    }
}
