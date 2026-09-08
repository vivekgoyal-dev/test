package com.shoppingcart.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressDto {

    private Long addressId;
    private Long userId;

    @NotBlank(message = "full name is required")
    private String fullName;

    @Pattern(regexp = "^[0-9]{10}$", message = "mobile number must be 10 digits")
    private String mobileNumber;

    private String flatNumber;

    @NotBlank(message = "city is required")
    private String city;

    private String state;

    @Pattern(regexp = "^[0-9]{6}$", message = "pincode must be 6 digits")
    private String pincode;
}
