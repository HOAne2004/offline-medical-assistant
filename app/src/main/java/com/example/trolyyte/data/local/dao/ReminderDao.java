package com.example.trolyyte.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.example.trolyyte.domain.model.ReminderHistory;
import com.example.trolyyte.domain.model.ReminderHistoryWithTemplate;
import com.example.trolyyte.domain.model.ReminderTemplate;

import java.util.List;

@Dao
public interface ReminderDao {

    // ==========================================
    // CÁC LỆNH CHO BẢNG MẪU (TEMPLATE)
    // ==========================================
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertTemplate(ReminderTemplate template);

    @Update
    void updateTemplate(ReminderTemplate template);

    @Query("SELECT * FROM reminder_templates WHERE id = :id")
    ReminderTemplate getTemplateById(String id);

    // ==========================================
    // CÁC LỆNH CHO BẢNG LỊCH SỬ (HISTORY)
    // ==========================================
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertHistory(ReminderHistory history);

    @Update
    void updateHistory(ReminderHistory history);

    @Delete
    void deleteTemplate(ReminderTemplate template);
    @Query("SELECT * FROM reminder_histories WHERE id = :id")
    ReminderHistory getHistoryById(String id);

    // ==========================================
    // CÁC LỆNH JOIN CHO GIAO DIỆN (UI) VÀ NCKH
    // ==========================================

    // Lấy toàn bộ lịch sử kèm thông tin thuốc, sắp xếp theo thời gian (dùng cho Timeline)
    @Transaction
    @Query("SELECT * FROM reminder_histories ORDER BY scheduledTimeMillis ASC")
    List<ReminderHistoryWithTemplate> getAllHistoryWithTemplates();

    // Lọc lịch sử trong 1 khoảng thời gian (VD: Chỉ lấy lịch trình của ngày hôm nay)
    @Transaction
    @Query("SELECT * FROM reminder_histories WHERE scheduledTimeMillis >= :startTime AND scheduledTimeMillis <= :endTime ORDER BY scheduledTimeMillis ASC")
    List<ReminderHistoryWithTemplate> getHistoriesForTimeRange(long startTime, long endTime);
}