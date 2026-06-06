package com.example.trolyyte.domain.repository;

import com.example.trolyyte.domain.model.Reminder;
import com.example.trolyyte.domain.model.ReminderHistory;
import com.example.trolyyte.domain.model.ReminderHistoryWithTemplate;
import com.example.trolyyte.domain.model.ReminderTemplate;

import java.util.List;

public interface ReminderRepository {
    // --- 1. Nhóm Template (Khuôn mẫu thuốc) ---
    void insertTemplate(ReminderTemplate template);
    void updateTemplate(ReminderTemplate template);
    ReminderTemplate getTemplateById(String id);
    void deleteTemplate(ReminderTemplate template); // Bổ sung để xử lý nút "Xóa"

    // --- 2. Nhóm History (Lịch sử từng cữ uống) ---
    void insertHistory(ReminderHistory history);
    void updateHistory(ReminderHistory history);
    ReminderHistory getHistoryById(String id);

    // --- 3. Nhóm truy vấn dữ liệu gộp cho Giao diện (Timeline) ---
    List<ReminderHistoryWithTemplate> getAllHistoryWithTemplates();
    List<ReminderHistoryWithTemplate> getHistoriesForTimeRange(long startTime, long endTime);
}
