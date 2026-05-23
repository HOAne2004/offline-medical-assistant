package com.example.trolyyte.domain.repository;

import java.util.List;

public interface HealthRecordRepository {
    // Lưu lại triệu chứng (Ví dụ: "Đau ngực", "Mức độ nặng", timestamp)
    void recordSymptom(String symptomName, String severity, long timestamp);

    // Lấy danh sách triệu chứng gần đây để bác sĩ/người thân xem
    List<Object> getRecentSymptoms(int days);
}