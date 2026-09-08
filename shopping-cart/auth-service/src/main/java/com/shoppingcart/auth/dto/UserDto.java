package com.shoppingcart.auth.dto;

import com.shoppingcart.auth.model.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** A user as the API returns it. There is no password field on purpose. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    private Long userId;
    private String email;
    private Role role;
    private boolean active;
}
