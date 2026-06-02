package com.example.trolyyte.presentation.reminder;

import com.example.trolyyte.domain.model.Reminder;
import java.util.List;

public abstract class ReminderUiState {
    public static class Loading extends ReminderUiState {}
    
    public static class Success extends ReminderUiState {
        private final List<Reminder> reminders;
        public Success(List<Reminder> reminders) { this.reminders = reminders; }
        public List<Reminder> getReminders() { return reminders; }
    }

    public static class Empty extends ReminderUiState {}

    public static class Error extends ReminderUiState {
        private final String message;
        public Error(String message) { this.message = message; }
        public String getMessage() { return message; }
    }
}
