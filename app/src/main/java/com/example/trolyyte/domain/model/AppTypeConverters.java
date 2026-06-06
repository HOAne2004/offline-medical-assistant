package com.example.trolyyte.domain.model;

import androidx.room.TypeConverter;

public class AppTypeConverters {

    // --- Chuyển đổi cho ReminderTemplate.Type ---
    @TypeConverter
    public static String fromTemplateType(ReminderTemplate.Type type) {
        return type == null ? null : type.name();
    }

    @TypeConverter
    public static ReminderTemplate.Type toTemplateType(String type) {
        return type == null ? null : ReminderTemplate.Type.valueOf(type);
    }

    // --- Chuyển đổi cho ReminderTemplate.RepeatType ---
    @TypeConverter
    public static String fromRepeatType(ReminderTemplate.RepeatType type) {
        return type == null ? null : type.name();
    }

    @TypeConverter
    public static ReminderTemplate.RepeatType toRepeatType(String type) {
        return type == null ? null : ReminderTemplate.RepeatType.valueOf(type);
    }

    // --- Chuyển đổi cho ReminderHistory.Status ---
    @TypeConverter
    public static String fromHistoryStatus(ReminderHistory.Status status) {
        return status == null ? null : status.name();
    }

    @TypeConverter
    public static ReminderHistory.Status toHistoryStatus(String status) {
        return status == null ? null : ReminderHistory.Status.valueOf(status);
    }

    // --- Chuyển đổi cho Appointment.Status ---
    @TypeConverter
    public static String fromApptStatus(Appointment.Status status) {
        return status == null ? null : status.name();
    }

    @TypeConverter
    public static Appointment.Status toApptStatus(String status) {
        return status == null ? null : Appointment.Status.valueOf(status);
    }
}