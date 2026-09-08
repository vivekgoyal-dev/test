package com.eshoppingzone.order.address;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer addressId;

    private Integer customerId;
    private String fullName;
    private String mobileNumber;
    private Integer flatNumber;
    private String city;
    private Integer pincode;
    private String state;
}
