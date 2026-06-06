package com.example.trolyyte.domain.repository;

import com.example.trolyyte.domain.model.UserProfile;

public interface UserProfileRepository {
    UserProfile getProfile();
    void saveProfile(UserProfile profile);
}