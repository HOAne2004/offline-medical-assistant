package com.example.trolyyte.domain.model;

import androidx.room.Embedded;
import androidx.room.Relation;

public class ReminderHistoryWithTemplate {

    // Thuộc tính chính: Bản ghi lịch sử (Chứa thời gian, trạng thái đã uống/chưa uống)
    @Embedded
    public ReminderHistory history;

    // Thuộc tính phụ: Thông tin gốc của viên thuốc đó
    @Relation(
            parentColumn = "templateId", // Trường ID nối trong bảng History
            entityColumn = "id"          // Trường ID gốc trong bảng Template
    )
    public ReminderTemplate template;
}