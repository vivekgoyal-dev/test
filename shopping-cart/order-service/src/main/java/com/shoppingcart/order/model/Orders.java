package com.shoppingcart.order.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "orders")
public class Orders {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private String productName;

    /** The shopowner who listed the product, copied at checkout so /orders/received can filter. */
    @Column(nullable = false)
    private Long ownerId;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private double amountPaid;

    @Column(nullable = false)
    private String modeOfPayment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus orderStatus;

    @Column(nullable = false)
    private LocalDateTime orderDate;

    /**
     * The address is copied onto the order, not linked. An order from last month must still show
     * where it was actually delivered after the customer edits their address.
     */
    @ManyToOne
    @JoinColumn(name = "address_id", nullable = false)
    private Address address;
}
