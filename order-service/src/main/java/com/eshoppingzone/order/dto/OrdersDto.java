package com.eshoppingzone.order.dto;

import com.eshoppingzone.order.address.Address;
import com.eshoppingzone.order.product.Product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrdersDto {

    private int orderId;
    private LocalDate orderDate;
    private Integer customerId;
    private double ammountPaid;
    private String modeOfPayment;
    private String orderStatus;
    private int quantity;
    private Address address;
    private Product product;
}
