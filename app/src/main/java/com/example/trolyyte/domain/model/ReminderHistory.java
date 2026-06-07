package com.example.trolyyte.domain.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import java.util.UUID;

@Entity(tableName = "reminder_histories",
        foreignKeys = @ForeignKey(entity = ReminderTemplate.class,
                parentColumns = "id",
                childColumns = "templateId",
                onDelete = ForeignKey.CASCADE),
        indices = {@Index("templateId")})
public class ReminderHistory {

    public enum Status {
        SCHEDULED,          // Đã lên lịch cho hôm nay
        TRIGGERED,          // Đang reo chuông
        SNOOZED,            // Đã bấm báo lại (Tạm hoãn)
        COMPLETED_PENDING,  // Mới bấm Đã uống (Có thể Undo trong 15p)
        COMPLETED,          // Khóa vĩnh viễn (Đã uống)
        MISSED,             // Bỏ lỡ (Quá window mà không uống)
        CANCELLED           // Bác sĩ/User chủ động hủy cữ thuốc này
    }

    @PrimaryKey
    @NonNull
    private String id;

    @NonNull
    private String templateId; // Liên kết tới Template gốc để lấy Tên thuốc, liều lượng

    private long scheduledTimeMillis; // Giờ lý thuyết phải uống (VD: 8h00)
    private long actualTriggerTimeMillis; // Giờ thực tế sẽ reo chuông (Nếu SNOOZE, giờ này sẽ +10 phút)

    private Long completedTimeMillis; // Giờ thực tế bấm nút Đã uống
    private Status status;

    @Ignore
    public ReminderHistory(@NonNull String templateId, long scheduledTimeMillis) {
        this.id = UUID.randomUUID().toString();
        this.templateId = templateId;
        this.scheduledTimeMillis = scheduledTimeMillis;
        this.actualTriggerTimeMillis = scheduledTimeMillis; // Ban đầu bằng nhau
        this.status = Status.SCHEDULED;
    }

    // Constructor mặc định cho Room
    public ReminderHistory() {
        // Room cần constructor không tham số
    }

    // ========== GETTERS ==========

    @NonNull
    public String getId() {
        return id;
    }

    @NonNull
    public String getTemplateId() {
        return templateId;
    }

    public long getScheduledTimeMillis() {
        return scheduledTimeMillis;
    }

    public long getActualTriggerTimeMillis() {
        return actualTriggerTimeMillis;
    }

    public Long getCompletedTimeMillis() {
        return completedTimeMillis;
    }

    public Status getStatus() {
        return status;
    }

    // ========== SETTERS ==========

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public void setTemplateId(@NonNull String templateId) {
        this.templateId = templateId;
    }

    public void setScheduledTimeMillis(long scheduledTimeMillis) {
        this.scheduledTimeMillis = scheduledTimeMillis;
    }

    public void setActualTriggerTimeMillis(long actualTriggerTimeMillis) {
        this.actualTriggerTimeMillis = actualTriggerTimeMillis;
    }

    public void setCompletedTimeMillis(Long completedTimeMillis) {
        this.completedTimeMillis = completedTimeMillis;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    // ========== PHƯƠNG THỨC TIỆN ÍCH (Tùy chọn) ==========

    /**
     * Kiểm tra xem lịch sử nhắc nhở này đã hoàn thành hay chưa
     */
    public boolean isCompleted() {
        return status == Status.COMPLETED || status == Status.COMPLETED_PENDING;
    }

    /**
     * Kiểm tra xem có thể undo hay không (chỉ trong vòng 15 phút sau khi COMPLETED_PENDING)
     */
    public boolean canUndo() {
        return status == Status.COMPLETED_PENDING &&
                completedTimeMillis != null &&
                (System.currentTimeMillis() - completedTimeMillis) <= 15 * 60 * 1000; // 15 phút
    }

    /**
     * Kiểm tra xem nhắc nhở đã bị bỏ lỡ hay chưa
     */
    public boolean isMissed() {
        return status == Status.MISSED;
    }

    /**
     * Kiểm tra xem có thể snooze hay không
     */
    public boolean canSnooze() {
        return status == Status.TRIGGERED &&
                actualTriggerTimeMillis > 0;
    }
}