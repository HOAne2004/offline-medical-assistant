package com.example.trolyyte.data.repository;

import com.example.trolyyte.domain.model.Reminder;
import com.example.trolyyte.domain.repository.ReminderRepository;

import java.util.ArrayList;
import java.util.List;

public class ReminderRepositoryImpl implements ReminderRepository {
    // Dùng ArrayList để mô phỏng Database trong bộ nhớ RAM
    private final List<Reminder> reminders = new ArrayList<>();

    public ReminderRepositoryImpl() {
        // Dummy data for testing (Dữ liệu giả để Leader test UI)
        reminders.add(new Reminder("mock_1", Reminder.Type.MEDICINE, "Paracetamol", "08:00", false, 15, true));
        reminders.add(new Reminder("mock_2", Reminder.Type.MEDICINE, "Vitamin C", "12:00", false, 15, true));
        reminders.add(new Reminder("mock_3", Reminder.Type.MEDICINE, "Thuốc bổ", "19:00", false, 15, false));
    }

    @Override
    public List<Reminder> getAllReminders() {
        return new ArrayList<>(reminders);
    }

    @Override
    public List<Reminder> getRemindersByTime(String timeKeyword) {
        List<Reminder> filteredList = new ArrayList<>();
        // Tìm kiếm các lịch nhắc có chứa từ khóa thời gian
        for (Reminder reminder : reminders) {
            if (reminder.getTime().contains(timeKeyword)) {
                filteredList.add(reminder);
            }
        }
        return filteredList;
    }

    @Override
    public void addReminder(Reminder reminder) {
        reminders.add(reminder);
    }

    @Override
    public void updateReminder(Reminder reminder) {
        // Tìm lịch nhắc cũ có cùng ID và ghi đè bằng lịch nhắc mới
        for (int i = 0; i < reminders.size(); i++) {
            if (reminders.get(i).getId().equals(reminder.getId())) {
                reminders.set(i, reminder);
                break;
            }
        }
    }

    @Override
    public void deleteReminder(String id) {
        reminders.removeIf(reminder -> reminder.getId().equals(id));
    }
}