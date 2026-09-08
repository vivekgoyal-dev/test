package com.eshoppingzone.profile.pojo;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class Address {

    private int houseNumber;
    private String streetName;
    private String colonyName;
    private String city;
    private String state;
    private int pincode;
}
