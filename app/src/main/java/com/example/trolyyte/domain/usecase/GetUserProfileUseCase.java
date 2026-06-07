package com.example.trolyyte.domain.usecase;

import com.example.trolyyte.domain.model.UserProfile;
import com.example.trolyyte.domain.repository.UserProfileRepository;

public class GetUserProfileUseCase {

    private final UserProfileRepository repository;

    public GetUserProfileUseCase(UserProfileRepository repository) {
        this.repository = repository;
    }

    public UserProfile execute() {
        // Trả về dữ liệu từ Repository (đã bao gồm logic mặc định trả về "Ông/Bà" nếu null)
        return repository.getProfile();
    }
}