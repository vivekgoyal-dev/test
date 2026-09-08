package com.shoppingcart.profile.service;

import com.shoppingcart.profile.dto.UserProfileDto;
import com.shoppingcart.profile.exception.ApiException;
import com.shoppingcart.profile.model.UserProfile;
import com.shoppingcart.profile.repository.UserProfileRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProfileServiceImpl implements ProfileService {

    private final UserProfileRepository repository;

    public ProfileServiceImpl(UserProfileRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserProfileDto create(Long userId, UserProfileDto dto) {
        if (repository.existsById(userId)) {
            throw ApiException.conflict("profile already exists, use PUT to update it");
        }
        return save(userId, dto);
    }

    @Override
    public UserProfileDto update(Long userId, UserProfileDto dto) {
        if (!repository.existsById(userId)) {
            throw ApiException.notFound("no profile yet, use POST to create it");
        }
        return save(userId, dto);
    }

    /** The id always comes from the token, so a caller cannot write to someone else's profile. */
    private UserProfileDto save(Long userId, UserProfileDto dto) {
        UserProfile profile = new UserProfile();
        BeanUtils.copyProperties(dto, profile);
        profile.setUserId(userId);
        return toDto(repository.save(profile));
    }

    @Override
    public UserProfileDto getByUserId(Long userId) {
        return toDto(repository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("no profile for user " + userId)));
    }

    @Override
    public List<UserProfileDto> getAll() {
        return repository.findAll().stream().map(this::toDto).toList();
    }

    private UserProfileDto toDto(UserProfile profile) {
        UserProfileDto dto = new UserProfileDto();
        BeanUtils.copyProperties(profile, dto);
        return dto;
    }
}
