package com.example.trolyyte.domain.repository;

public interface SettingsRepository {
    // Lưu tốc độ đọc (Ví dụ: 0.5 là chậm, 1.0 là bình thường, 2.0 là nhanh)
    void saveTtsSpeed(float speed);

    // Lấy tốc độ đọc đang lưu (TV4 nhớ set giá trị mặc định là 1.0f nhé)
    float getTtsSpeed();
}