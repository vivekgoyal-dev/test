package com.shoppingcart.profile.controller;

import com.shoppingcart.profile.dto.UserProfileDto;
import com.shoppingcart.profile.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/profiles")
public class ProfileController {

    private final ProfileService service;

    public ProfileController(ProfileService service) {
        this.service = service;
    }

    @PostMapping("/me")
    public ResponseEntity<UserProfileDto> createMyProfile(@RequestHeader("X-User-Id") Long userId,
                                                          @Valid @RequestBody UserProfileDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(userId, dto));
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileDto> getMyProfile(@RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(service.getByUserId(userId));
    }

    @PutMapping("/me")
    public ResponseEntity<UserProfileDto> updateMyProfile(@RequestHeader("X-User-Id") Long userId,
                                                          @Valid @RequestBody UserProfileDto dto) {
        return ResponseEntity.ok(service.update(userId, dto));
    }

    @GetMapping
    public ResponseEntity<List<UserProfileDto>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserProfileDto> getByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(service.getByUserId(userId));
    }
}
