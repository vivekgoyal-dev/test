package com.shoppingcart.order.controller;

import com.shoppingcart.order.dto.AddressDto;
import com.shoppingcart.order.dto.CheckoutRequest;
import com.shoppingcart.order.dto.OrderDto;
import com.shoppingcart.order.model.OrderStatus;
import com.shoppingcart.order.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @PostMapping("/address")
    public ResponseEntity<AddressDto> storeAddress(@RequestHeader("X-User-Id") Long userId,
                                                   @Valid @RequestBody AddressDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.storeAddress(userId, dto));
    }

    @GetMapping("/address")
    public ResponseEntity<List<AddressDto>> getMyAddresses(@RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(service.getMyAddresses(userId));
    }

    @PostMapping("/checkout")
    public ResponseEntity<List<OrderDto>> checkout(@RequestHeader("X-User-Id") Long userId,
                                                   @Valid @RequestBody CheckoutRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.checkout(userId, request.getAddressId()));
    }

    @GetMapping("/my")
    public ResponseEntity<List<OrderDto>> getMyOrders(@RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(service.getMyOrders(userId));
    }

    @GetMapping("/received")
    public ResponseEntity<List<OrderDto>> getReceivedOrders(@RequestHeader("X-User-Id") Long ownerId) {
        return ResponseEntity.ok(service.getReceivedOrders(ownerId));
    }

    @GetMapping
    public ResponseEntity<List<OrderDto>> getAllOrders() {
        return ResponseEntity.ok(service.getAllOrders());
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<OrderDto> changeStatus(@PathVariable Long orderId,
                                                 @RequestHeader("X-User-Id") Long ownerId,
                                                 @RequestParam OrderStatus status) {
        return ResponseEntity.ok(service.changeStatus(orderId, ownerId, status));
    }

    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<OrderDto> cancel(@PathVariable Long orderId,
                                           @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(service.cancel(orderId, userId));
    }
}
