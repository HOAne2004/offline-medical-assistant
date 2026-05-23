package com.example.trolyyte.domain.repository;

public interface UserProfileRepository {
    // Lưu thông tin
    void saveProfile(String name, String emergencyPhone, String medicalHistory);

    // Lấy thông tin (Kèm giá trị mặc định nếu chưa cài đặt)
    String getUserName();
    String getEmergencyPhone();
    String getMedicalHistory();
}