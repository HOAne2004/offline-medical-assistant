package com.example.trolyyte.domain.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Objects;
import java.util.UUID;

@Entity(tableName = "appointments")
public class Appointment {

    // Định nghĩa rõ các trạng thái của lịch khám
    public enum Status {
        UPCOMING,
        COMPLETED_PENDING, // Chờ Undo (Giống Reminder)
        COMPLETED,
        CANCELLED,
        MISSED
    }

    @PrimaryKey
    @NonNull
    private String id;

    private String title;
    private String location;
    private String doctorName; // Có thể null
    private long timeMillis;
    private String notes;

    // --- Bổ sung---
    private boolean enableReminder; // Có bật báo thức không?
    private int remindMinutes;      // Nhắc trước (Có thể là 1440 = 1 ngày)

    // Các trường vòng đời dữ liệu
    private Status status;
    private long createdAt;
    private long updatedAt;

    public Appointment(){}
    @Ignore
    public Appointment(@NonNull String id, String title, long timeMillis) {
        this.id = (id != null && !id.isEmpty()) ? id : UUID.randomUUID().toString();
        this.title = title;
        this.timeMillis = timeMillis;
        this.enableReminder = true;
        this.remindMinutes = 15;
        this.status = Status.UPCOMING; // Mặc định khi tạo mới là Sắp tới

        long now = System.currentTimeMillis();
        this.createdAt = now;
        this.updatedAt = now;
    }

    // --- Getters & Setters ---

    @NonNull
    public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

    public long getTimeMillis() { return timeMillis; }
    public void setTimeMillis(long timeMillis) { this.timeMillis = timeMillis; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public boolean isEnableReminder() { return enableReminder; }
    public void setEnableReminder(boolean enableReminder) { this.enableReminder = enableReminder; }

    public int getRemindMinutes() { return remindMinutes; }
    public void setRemindMinutes(int remindMinutes) { this.remindMinutes = remindMinutes; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Appointment that = (Appointment) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}