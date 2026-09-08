package com.eshoppingzone.profile.dto;

import com.eshoppingzone.profile.pojo.Address;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDto {

    private int profileId;
    private String fullName;
    private String image;
    private String emailId;
    private Long mobileNumber;
    private String about;
    private LocalDate dateOfBirth;
    private String gender;
    private String role;

    /** Accepted on the way in, never sent back out. */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    private List<Address> addresses = new ArrayList<>();
}
