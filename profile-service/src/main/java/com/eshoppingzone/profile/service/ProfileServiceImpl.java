package com.eshoppingzone.profile.service;

import com.eshoppingzone.profile.dto.UserProfileDto;
import com.eshoppingzone.profile.pojo.Role;
import com.eshoppingzone.profile.pojo.UserProfile;
import com.eshoppingzone.profile.repository.ProfileRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ProfileServiceImpl implements ProfileService {

    private final ProfileRepository repository;

    public ProfileServiceImpl(ProfileRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserProfileDto addNewCustomerProfile(UserProfileDto profile) {
        return addWithRole(profile, Role.CUSTOMER);
    }

    @Override
    public UserProfileDto addNewMerchantProfile(UserProfileDto profile) {
        return addWithRole(profile, Role.MERCHANT);
    }

    @Override
    public UserProfileDto addNewDeliveryProfile(UserProfileDto profile) {
        return addWithRole(profile, Role.DELIVERY_AGENT);
    }

    private UserProfileDto addWithRole(UserProfileDto dto, String role) {
        if (dto.getMobileNumber() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "mobileNumber is required");
        }
        if (repository.findByMobileNumber(dto.getMobileNumber()) != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "mobile number already registered");
        }
        UserProfile entity = toEntity(dto);
        entity.setProfileId(0);
        entity.setRole(role);
        return toDto(repository.save(entity));
    }

    @Override
    public List<UserProfileDto> getAllProfiles() {
        return repository.findAll().stream().map(this::toDto).toList();
    }

    @Override
    public UserProfileDto getByProfileId(int profileId) {
        return toDto(findOrThrow(profileId));
    }

    @Override
    public UserProfileDto updateProfile(UserProfileDto dto) {
        UserProfile existing = findOrThrow(dto.getProfileId());
        UserProfile updated = toEntity(dto);
        // role and password are not editable through this endpoint
        updated.setRole(existing.getRole());
        if (updated.getPassword() == null) {
            updated.setPassword(existing.getPassword());
        }
        return toDto(repository.save(updated));
    }

    @Override
    public void deleteProfile(int profileId) {
        repository.delete(findOrThrow(profileId));
    }

    @Override
    public UserProfileDto findByMobileNo(Long mobileNumber) {
        UserProfile profile = repository.findByMobileNumber(mobileNumber);
        if (profile == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "no profile for mobile " + mobileNumber);
        }
        return toDto(profile);
    }

    @Override
    public UserProfileDto getByUserName(String fullName) {
        UserProfile profile = repository.findByFullName(fullName);
        if (profile == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "no profile named " + fullName);
        }
        return toDto(profile);
    }

    private UserProfile findOrThrow(int profileId) {
        return repository.findById(profileId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "no profile " + profileId));
    }

    private UserProfileDto toDto(UserProfile entity) {
        UserProfileDto dto = new UserProfileDto();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    private UserProfile toEntity(UserProfileDto dto) {
        UserProfile entity = new UserProfile();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }
}
