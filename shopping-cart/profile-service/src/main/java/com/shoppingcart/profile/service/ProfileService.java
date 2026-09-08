package com.shoppingcart.profile.service;

import com.shoppingcart.profile.dto.UserProfileDto;

import java.util.List;

public interface ProfileService {

    UserProfileDto create(Long userId, UserProfileDto dto);

    UserProfileDto update(Long userId, UserProfileDto dto);

    UserProfileDto getByUserId(Long userId);

    List<UserProfileDto> getAll();
}
