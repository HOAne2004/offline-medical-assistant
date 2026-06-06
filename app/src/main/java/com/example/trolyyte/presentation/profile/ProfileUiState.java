package com.example.trolyyte.presentation.profile;

import com.example.trolyyte.domain.model.UserProfile;

public abstract class ProfileUiState {

    // Trạng thái đang tải
    public static class Loading extends ProfileUiState {}

    // Trạng thái thành công, mang theo dữ liệu Profile
    public static class Success extends ProfileUiState {
        private final UserProfile profile;

        public Success(UserProfile profile) {
            this.profile = profile;
        }

        public UserProfile getProfile() {
            return profile;
        }
    }

    // Trạng thái lỗi
    public static class Error extends ProfileUiState {
        private final String message;

        public Error(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}