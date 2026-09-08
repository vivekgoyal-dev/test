package com.eshoppingzone.order.resource;

import com.eshoppingzone.order.address.Address;
import com.eshoppingzone.order.dto.CartDto;
import com.eshoppingzone.order.dto.OrdersDto;
import com.eshoppingzone.order.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderResource {

    private final OrderService orderService;

    public OrderResource(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<List<OrdersDto>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    /** Cash on delivery checkout. */
    @PostMapping("/place")
    public ResponseEntity<List<OrdersDto>> placeOrder(@RequestBody CartDto cart) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.placeOrder(cart));
    }

    /** Wallet checkout: debits wallet-service, rolls the orders back if the debit fails. */
    @PostMapping("/onlinePayment")
    public ResponseEntity<List<OrdersDto>> onlinePayment(@RequestBody CartDto cart) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.onlinePayment(cart));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrdersDto> getOrderById(@PathVariable int orderId) {
        return ResponseEntity.ok(orderService.getOrderById(orderId));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<OrdersDto>> getOrderByCustomerId(@PathVariable int customerId) {
        return ResponseEntity.ok(orderService.getOrderByCustomerId(customerId));
    }

    @GetMapping("/latest")
    public ResponseEntity<OrdersDto> findMAXByOrderId() {
        return ResponseEntity.ok(orderService.findMAXByOrderId());
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<OrdersDto> changeOrderStatus(@PathVariable int orderId, @RequestParam String status) {
        return ResponseEntity.ok(orderService.changeStatus(status, orderId));
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<String> deleteOrder(@PathVariable int orderId) {
        orderService.deleteOrder(orderId);
        return ResponseEntity.ok("order " + orderId + " deleted");
    }

    @PostMapping("/address")
    public ResponseEntity<Address> storeAddress(@RequestBody Address address) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.storeAddress(address));
    }

    @GetMapping("/address")
    public ResponseEntity<List<Address>> getAllAddress() {
        return ResponseEntity.ok(orderService.getAllAddress());
    }

    @GetMapping("/address/{customerId}")
    public ResponseEntity<List<Address>> getAddressByCustomerId(@PathVariable int customerId) {
        return ResponseEntity.ok(orderService.getAddressByCustomerId(customerId));
    }
}
