package com.shoppingcart.auth.service;

import com.shoppingcart.auth.dto.LoginRequest;
import com.shoppingcart.auth.dto.LoginResponse;
import com.shoppingcart.auth.dto.RegisterRequest;
import com.shoppingcart.auth.dto.UserDto;

import java.util.List;

public interface AuthService {

    UserDto register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    List<UserDto> getAllUsers();

    UserDto deactivate(Long userId);
}
