package com.eshoppingzone.order.orders;

import com.eshoppingzone.order.address.Address;
import com.eshoppingzone.order.product.Product;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Orders {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int orderId;

    private LocalDate orderDate;
    private Integer customerId;
    private double ammountPaid;
    private String modeOfPayment;
    private String orderStatus;
    private int quantity;

    @ManyToOne
    @JoinColumn(name = "address_id")
    private Address address;

    @Embedded
    private Product product;
}
