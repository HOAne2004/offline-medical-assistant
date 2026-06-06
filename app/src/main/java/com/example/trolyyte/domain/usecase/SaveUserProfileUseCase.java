package com.example.trolyyte.domain.usecase;

import com.example.trolyyte.domain.model.UserProfile;
import com.example.trolyyte.domain.repository.UserProfileRepository;

public class SaveUserProfileUseCase {

    private final UserProfileRepository repository;

    public SaveUserProfileUseCase(UserProfileRepository repository) {
        this.repository = repository;
    }

    public void execute(UserProfile profile) {
        // Thực hiện các Business Logic/Validation trước khi lưu (nếu cần thiết)
        // Ví dụ: Đảm bảo tên không bao giờ bị bỏ trống hoàn toàn
        if (profile.getName() == null || profile.getName().trim().isEmpty()) {
            profile.setName("Ông/Bà");
        }

        // Gọi xuống Repository để thực hiện lưu trữ (Repository sẽ lo việc set id = 1)
        repository.saveProfile(profile);
    }
}