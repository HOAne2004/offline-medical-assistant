package com.example.trolyyte.domain.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Objects;
import java.util.UUID;

@Entity(tableName = "reminders")
public class Reminder {

    public enum Type {
        MEDICINE,
        APPOINTMENT
    }

    public enum Status {
        SCHEDULED,
        TRIGGERED,
        COMPLETED,
        CANCELLED
    }

    // --- Core Identity ---
    @PrimaryKey
    @NonNull
    private final String id;
    private final Type type;

    // --- Hiển thị chung ---
    private String title;
    private String description;

    // --- Thời gian & Lập lịch ---
    private long triggerAtMillis;
    private String repeatRule;
    private int remindMinutes;

    // --- Trạng thái vòng đời ---
    private Status status;
    private boolean isActive;

    // --- Optional Structured Fields ---
    private String medicineName;
    private String dosage;
    private String location;

    public Reminder(@NonNull String id, Type type, String title, long triggerAtMillis) {
        this.id = (id != null && !id.isEmpty()) ? id : UUID.randomUUID().toString();
        this.type = type;
        this.title = title;
        this.triggerAtMillis = triggerAtMillis;
        this.status = Status.SCHEDULED;
        this.isActive = true;
        this.remindMinutes = 15;
    }

    @NonNull
    public String getId() { return id; }
    public Type getType() { return type; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public long getTriggerAtMillis() { return triggerAtMillis; }
    public void setTriggerAtMillis(long triggerAtMillis) { this.triggerAtMillis = triggerAtMillis; }
    public String getRepeatRule() { return repeatRule; }
    public void setRepeatRule(String repeatRule) { this.repeatRule = repeatRule; }
    public int getRemindMinutes() { return remindMinutes; }
    public void setRemindMinutes(int remindMinutes) { this.remindMinutes = remindMinutes; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { this.isActive = active; }
    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }
    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reminder reminder = (Reminder) o;
        return Objects.equals(id, reminder.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
