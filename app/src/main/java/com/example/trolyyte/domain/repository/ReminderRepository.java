package com.example.trolyyte.domain.repository;

import com.example.trolyyte.domain.model.Reminder;
import java.util.List;

public interface ReminderRepository {
    // 1. Lấy toàn bộ danh sách
    List<Reminder> getAllReminders();

    // 2. Truy vấn lịch nhắc theo ngày/thời gian (Phục vụ Intent: CHECK_MEDICATION)
    List<Reminder> getRemindersByTime(String timeKeyword);

    // 3. Thêm mới
    void addReminder(Reminder reminder);

    // 4. Cập nhật (Đổi giờ, Bật/Tắt chuông)
    void updateReminder(Reminder reminder);

    // 5. Xóa lịch nhắc (Bằng ID)
    void deleteReminder(String id);
}