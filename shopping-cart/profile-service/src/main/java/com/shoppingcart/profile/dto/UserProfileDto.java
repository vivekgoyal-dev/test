package com.shoppingcart.profile.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDto {

    /** Set from the token, never from the request body. */
    private Long userId;

    @NotBlank(message = "full name is required")
    private String fullName;

    @Pattern(regexp = "^[0-9]{10}$", message = "phone must be 10 digits")
    private String phone;

    private String gender;

    @Past(message = "date of birth must be in the past")
    private LocalDate dateOfBirth;
}
