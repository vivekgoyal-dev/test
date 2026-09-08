package com.shoppingcart.profile.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_profile")
public class UserProfile {

    /** Not generated: this is the userId from auth-service, so the two line up without a lookup. */
    @Id
    private Long userId;

    @Column(nullable = false)
    private String fullName;

    private String phone;
    private String gender;
    private LocalDate dateOfBirth;
}
