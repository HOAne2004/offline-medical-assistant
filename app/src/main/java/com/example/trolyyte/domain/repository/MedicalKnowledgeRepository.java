package com.example.trolyyte.domain.repository;

import com.example.trolyyte.domain.model.MedicineInfo;

public interface MedicalKnowledgeRepository {

    // Tìm kiếm thông tin chi tiết của một loại thuốc dựa vào tên
    // Trả về null nếu hệ thống chưa có dữ liệu về thuốc này
    MedicineInfo getMedicineInfo(String medicineName);

    // (Tùy chọn) Tìm kiếm mẹo sơ cứu cơ bản dựa vào triệu chứng
    // VD: input "bỏng nước sôi" -> output "Xả nước lạnh ngay lập tức..."
    String getFirstAidTip(String symptomKeyword);
}