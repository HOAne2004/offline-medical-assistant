package com.example.trolyyte.domain.model;

import java.util.Objects;

public class Reminder {
    public enum Type {
        MEDICINE,
        APPOINTMENT
    }

    private final String id;
    private final Type type;
    private final String title;
    private final String time;
    private final boolean isDone;
    private int remindMinutes;
    private boolean isActive;
    public Reminder(String id, Type type, String title, String time, boolean isDone, int remindMinutes, boolean isActive) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.time = time;
        this.isDone = isDone;
        this.remindMinutes = remindMinutes;
        this.isActive = isActive;
    }

    public String getId() { return id; }
    public Type getType() { return type; }
    public String getTitle() { return title; }
    public String getTime() { return time; }
    public boolean isDone() { return isDone; }
    public int getRemindMinutes() { return remindMinutes; }
    public void setRemindMinutes(int remindMinutes) { this.remindMinutes = remindMinutes; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { this.isActive = active; }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reminder reminder = (Reminder) o;
        return isDone == reminder.isDone &&
                Objects.equals(id, reminder.id) &&
                type == reminder.type &&
                Objects.equals(title, reminder.title) &&
                Objects.equals(time, reminder.time);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type, title, time, isDone, remindMinutes, isActive);
    }
}
