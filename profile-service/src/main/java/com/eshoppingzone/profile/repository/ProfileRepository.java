package com.eshoppingzone.profile.repository;

import com.eshoppingzone.profile.pojo.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileRepository extends JpaRepository<UserProfile, Integer> {

    UserProfile findByMobileNumber(Long mobileNumber);

    UserProfile findByFullName(String fullName);
}
