package com.example.trolyyte.domain.usecase;

import com.example.trolyyte.domain.model.Reminder;
import com.example.trolyyte.domain.repository.ReminderRepository;

import java.util.List;

public class GetRemindersUseCase {
    private final ReminderRepository repository;

    public GetRemindersUseCase(ReminderRepository repository) {
        this.repository = repository;
    }

    public List<Reminder> execute() {
        return repository.getAllReminders();
    }
}
