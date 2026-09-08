package com.shoppingcart.auth.controller;

import com.shoppingcart.auth.dto.LoginRequest;
import com.shoppingcart.auth.dto.LoginResponse;
import com.shoppingcart.auth.dto.RegisterRequest;
import com.shoppingcart.auth.dto.UserDto;
import com.shoppingcart.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(service.login(request));
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity.ok(service.getAllUsers());
    }

    @PutMapping("/users/{userId}/deactivate")
    public ResponseEntity<UserDto> deactivate(@PathVariable Long userId) {
        return ResponseEntity.ok(service.deactivate(userId));
    }
}
