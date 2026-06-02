package com.example.trolyyte.domain.repository;

import com.example.trolyyte.domain.model.Reminder;
import java.util.List;

public interface ReminderRepository {
    List<Reminder> getAllReminders();
    void addReminder(Reminder reminder);
    void updateReminder(Reminder reminder);
    void deleteReminder(String id);
}
