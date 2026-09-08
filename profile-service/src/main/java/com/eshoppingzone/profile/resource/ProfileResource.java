package com.eshoppingzone.profile.resource;

import com.eshoppingzone.profile.dto.UserProfileDto;
import com.eshoppingzone.profile.service.ProfileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/profiles")
public class ProfileResource {

    private final ProfileService service;

    public ProfileResource(ProfileService service) {
        this.service = service;
    }

    @PostMapping("/customer")
    public ResponseEntity<UserProfileDto> addNewCustomerProfile(@RequestBody UserProfileDto profile) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addNewCustomerProfile(profile));
    }

    @PostMapping("/merchant")
    public ResponseEntity<UserProfileDto> addNewMerchantProfile(@RequestBody UserProfileDto profile) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addNewMerchantProfile(profile));
    }

    @PostMapping("/delivery")
    public ResponseEntity<UserProfileDto> addNewDeliveryProfile(@RequestBody UserProfileDto profile) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addNewDeliveryProfile(profile));
    }

    @GetMapping
    public ResponseEntity<List<UserProfileDto>> getAllProfiles() {
        return ResponseEntity.ok(service.getAllProfiles());
    }

    @GetMapping("/{profileId}")
    public ResponseEntity<UserProfileDto> getByProfileId(@PathVariable int profileId) {
        return ResponseEntity.ok(service.getByProfileId(profileId));
    }

    @GetMapping("/phone/{mobileNumber}")
    public ResponseEntity<UserProfileDto> getByPhoneNumber(@PathVariable Long mobileNumber) {
        return ResponseEntity.ok(service.findByMobileNo(mobileNumber));
    }

    @GetMapping("/username/{fullName}")
    public ResponseEntity<UserProfileDto> getByUserName(@PathVariable String fullName) {
        return ResponseEntity.ok(service.getByUserName(fullName));
    }

    @PutMapping
    public ResponseEntity<UserProfileDto> updateProfile(@RequestBody UserProfileDto profile) {
        return ResponseEntity.ok(service.updateProfile(profile));
    }

    @DeleteMapping("/{profileId}")
    public ResponseEntity<Void> deleteProfile(@PathVariable int profileId) {
        service.deleteProfile(profileId);
        return ResponseEntity.noContent().build();
    }
}
