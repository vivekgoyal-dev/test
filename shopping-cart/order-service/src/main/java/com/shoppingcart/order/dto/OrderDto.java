package com.shoppingcart.order.dto;

import com.shoppingcart.order.model.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {

    private Long orderId;
    private Long userId;
    private Long productId;
    private String productName;
    private Long ownerId;
    private int quantity;
    private double amountPaid;
    private String modeOfPayment;
    private OrderStatus orderStatus;
    private LocalDateTime orderDate;
    private AddressDto address;
}
