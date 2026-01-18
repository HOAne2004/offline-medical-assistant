package com.example.trolyyte.presentation.profile;

public abstract class ProfileUiState {

    // =============================
    // Trạng thái nghỉ (chưa load)
    // =============================
    public static class Idle extends ProfileUiState {}

    // =============================
    // Trạng thái đang tải dữ liệu
    // =============================
    public static class Loading extends ProfileUiState {}

    // =============================
    // Trạng thái hiển thị hồ sơ
    // =============================
    public static class Success extends ProfileUiState {
        public final String fullName;
        public final int age;
        public final String gender;
        public final String medicalHistory;
        public final String currentMedications;
        public final String reminderMessage;

        public Success(
                String fullName,
                int age,
                String gender,
                String medicalHistory,
                String currentMedications,
                String reminderMessage
        ) {
            this.fullName = fullName;
            this.age = age;
            this.gender = gender;
            this.medicalHistory = medicalHistory;
            this.currentMedications = currentMedications;
            this.reminderMessage = reminderMessage;
        }
    }

    // =============================
    // Trạng thái lỗi
    // =============================
    public static class Error extends ProfileUiState {
        public final String message;
        public Error(String message) {
            this.message = message;
        }
    }
}
