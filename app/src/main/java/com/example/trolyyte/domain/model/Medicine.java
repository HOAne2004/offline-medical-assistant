package com.example.trolyyte.domain.model;

public class Medicine {
    private String time;
    private String name;
    private int remindMinutes;
    private boolean isMuted;

    public Medicine(String time, String name, int remindMinutes) {
        this.time = time;
        this.name = name;
        this.remindMinutes = remindMinutes;
        this.isMuted = false;
    }

    public String getTime() { return time; }
    public String getName() { return name; }
    public int getRemindMinutes() { return remindMinutes; }
    public void setRemindMinutes(int remindMinutes) { this.remindMinutes = remindMinutes; }
    public boolean isMuted() { return isMuted; }
    public void setMuted(boolean muted) { isMuted = muted; }
}
