package com.eshoppingzone.profile.service;

import com.eshoppingzone.profile.dto.UserProfileDto;

import java.util.List;

public interface ProfileService {

    UserProfileDto addNewCustomerProfile(UserProfileDto profile);

    UserProfileDto addNewMerchantProfile(UserProfileDto profile);

    UserProfileDto addNewDeliveryProfile(UserProfileDto profile);

    List<UserProfileDto> getAllProfiles();

    UserProfileDto getByProfileId(int profileId);

    UserProfileDto updateProfile(UserProfileDto profile);

    void deleteProfile(int profileId);

    UserProfileDto findByMobileNo(Long mobileNumber);

    UserProfileDto getByUserName(String fullName);
}
