package com.example.trolyyte.domain.model;

import androidx.room.TypeConverter;

public class ReminderTypeConverters {
    @TypeConverter
    public static String fromType(Reminder.Type type) {
        return type == null ? null : type.name();
    }

    @TypeConverter
    public static Reminder.Type toType(String type) {
        return type == null ? null : Reminder.Type.valueOf(type);
    }

    @TypeConverter
    public static String fromStatus(Reminder.Status status) {
        return status == null ? null : status.name();
    }

    @TypeConverter
    public static Reminder.Status toStatus(String status) {
        return status == null ? null : Reminder.Status.valueOf(status);
    }
}
