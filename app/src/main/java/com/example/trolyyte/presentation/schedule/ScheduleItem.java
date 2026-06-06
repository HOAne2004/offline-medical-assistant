package com.example.trolyyte.presentation.schedule;

import com.example.trolyyte.domain.model.Appointment;
import com.example.trolyyte.domain.model.ReminderHistoryWithTemplate;

public class ScheduleItem {
    public static final int TYPE_REMINDER = 0;
    public static final int TYPE_APPOINTMENT = 1;

    private final int type;
    private ReminderHistoryWithTemplate reminder;
    private Appointment appointment;

    // Constructor dành cho Nhắc thuốc
    public ScheduleItem(ReminderHistoryWithTemplate reminder) {
        this.type = TYPE_REMINDER;
        this.reminder = reminder;
    }

    // Constructor dành cho Lịch khám
    public ScheduleItem(Appointment appointment) {
        this.type = TYPE_APPOINTMENT;
        this.appointment = appointment;
    }

    public int getType() {
        return type;
    }

    public ReminderHistoryWithTemplate getReminderData() {
        return reminder;
    }

    public Appointment getAppointment() {
        return appointment;
    }

    // Hàm cực kỳ quan trọng để sắp xếp danh sách gộp theo đúng thời gian thực
    public long getTimeMillis() {
        if (type == TYPE_REMINDER && reminder != null) {
            return reminder.history.getScheduledTimeMillis();
        } else if (type == TYPE_APPOINTMENT && appointment != null) {
            return appointment.getTimeMillis();
        }
        return 0;
    }
}