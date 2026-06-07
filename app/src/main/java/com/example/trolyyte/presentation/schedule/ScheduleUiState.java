package com.example.trolyyte.presentation.schedule;

import java.util.List;

public abstract class ScheduleUiState {
    public static class Loading extends ScheduleUiState {}

    public static class Success extends ScheduleUiState {
        private final List<ScheduleItem> items;
        public Success(List<ScheduleItem> items) { this.items = items; }
        public List<ScheduleItem> getItems() { return items; }
    }

    public static class Empty extends ScheduleUiState {}

    public static class Error extends ScheduleUiState {
        private final String message;
        public Error(String message) { this.message = message; }
        public String getMessage() { return message; }
    }
}