package com.eshoppingzone.profile.pojo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int profileId;

    private String fullName;
    private String image;
    private String emailId;

    @Column(unique = true)
    private Long mobileNumber;

    private String about;
    private LocalDate dateOfBirth;
    private String gender;
    private String role;
    private String password;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_address", joinColumns = @JoinColumn(name = "profile_id"))
    private List<Address> addresses = new ArrayList<>();
}
