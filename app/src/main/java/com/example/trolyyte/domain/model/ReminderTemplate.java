package com.example.trolyyte.domain.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import java.util.UUID;

@Entity(tableName = "reminder_templates")
public class ReminderTemplate {

    public enum Type { MEDICINE, ROUTINE }
    public enum RepeatType { NONE, DAILY, WEEKLY, MONTHLY }

    @PrimaryKey
    @NonNull
    private String id;
    private Type type;

    // --- Thông tin cốt lõi ---
    private String title;
    private String description;

    // --- Y lệnh Y khoa (Bổ sung theo đề xuất) ---
    private String dosage;         // VD: "1 viên"
    private String instruction;    // VD: "Uống sau ăn sáng 30 phút"

    // --- Quy tắc Lập lịch ---
    private long baseTriggerTimeOfDay; // Thời gian kích hoạt chuẩn trong ngày (lưu dưới dạng giờ/phút quy đổi ra millis hoặc String "08:00")
    private RepeatType repeatType;
    private int remindMinutes;     // Nhắc trước X phút

    // --- Dung sai Y tế (Bổ sung theo đề xuất) ---
    private int completionWindowMinutes = 60; // Mặc định cho phép ± 60 phút

    private boolean isActive; // Dùng để Tắt/Bật toàn bộ chuỗi nhắc nhở này
    private long createdAtMillis;

    public ReminderTemplate() {
    }

    @Ignore
    public ReminderTemplate(@NonNull String id, Type type, String title) {
        this.id = (id != null && !id.isEmpty()) ? id : UUID.randomUUID().toString();
        this.type = type != null ? type : Type.MEDICINE;
        this.title = title;
        this.isActive = true;
        this.createdAtMillis = System.currentTimeMillis();
    }

    // ========== GETTERS ==========

    @NonNull
    public String getId() {
        return id;
    }

    public Type getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getDosage() {
        return dosage;
    }

    public String getInstruction() {
        return instruction;
    }

    public long getBaseTriggerTimeOfDay() {
        return baseTriggerTimeOfDay;
    }

    public RepeatType getRepeatType() {
        return repeatType;
    }

    public int getRemindMinutes() {
        return remindMinutes;
    }

    public int getCompletionWindowMinutes() {
        return completionWindowMinutes;
    }

    public boolean isActive() {
        return isActive;
    }

    public long getCreatedAtMillis() {
        return createdAtMillis;
    }

    // ========== SETTERS ==========

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public void setInstruction(String instruction) {
        this.instruction = instruction;
    }

    public void setBaseTriggerTimeOfDay(long baseTriggerTimeOfDay) {
        this.baseTriggerTimeOfDay = baseTriggerTimeOfDay;
    }

    public void setRepeatType(RepeatType repeatType) {
        this.repeatType = repeatType;
    }

    public void setRemindMinutes(int remindMinutes) {
        this.remindMinutes = remindMinutes;
    }

    public void setCompletionWindowMinutes(int completionWindowMinutes) {
        this.completionWindowMinutes = completionWindowMinutes;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public void setCreatedAtMillis(long createdAtMillis) {
        this.createdAtMillis = createdAtMillis;
    }
}