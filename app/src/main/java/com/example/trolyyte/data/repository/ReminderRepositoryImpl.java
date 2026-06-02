package com.example.trolyyte.data.repository;

import com.example.trolyyte.data.local.dao.ReminderDao;
import com.example.trolyyte.data.utils.ReminderAlarmScheduler;
import com.example.trolyyte.domain.model.Reminder;
import com.example.trolyyte.domain.repository.ReminderRepository;

import java.util.List;

public class ReminderRepositoryImpl implements ReminderRepository {
    private final ReminderDao reminderDao;
    private final ReminderAlarmScheduler scheduler;

    public ReminderRepositoryImpl(ReminderDao reminderDao, ReminderAlarmScheduler scheduler) {
        this.reminderDao = reminderDao;
        this.scheduler = scheduler;
    }

    @Override
    public List<Reminder> getAllReminders() {
        return reminderDao.getAllReminders();
    }

    @Override
    public void addReminder(Reminder reminder) {
        reminderDao.insert(reminder);
        scheduler.schedule(reminder);
    }

    @Override
    public void updateReminder(Reminder reminder) {

        reminderDao.update(reminder);
        scheduler.schedule(reminder);
    }

    @Override
    public void deleteReminder(String id) {
        Reminder reminder = reminderDao.getReminderById(id);
        if (reminder != null) {
            reminderDao.delete(reminder);
            scheduler.cancel(id);
        }
    }
}
