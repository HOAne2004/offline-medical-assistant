package com.example.trolyyte.presentation.reminder;

import androidx.lifecycle.LiveData;import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.trolyyte.domain.model.Reminder;
import com.example.trolyyte.domain.repository.ReminderRepository;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReminderViewModel extends ViewModel {
    private final ReminderRepository repository;
    private final MutableLiveData<ReminderUiState> _uiState = new MutableLiveData<>();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public LiveData<ReminderUiState> getUiState() { return _uiState; }

    public ReminderViewModel(ReminderRepository repository) {
        this.repository = repository;
        loadReminders();
    }

    public void loadReminders() {
        _uiState.setValue(new ReminderUiState.Loading());
        executorService.execute(() -> {
            try {
                List<Reminder> list = repository.getAllReminders();
                if (list.isEmpty()) {
                    _uiState.postValue(new ReminderUiState.Empty());
                } else {
                    _uiState.postValue(new ReminderUiState.Success(list));
                }
            } catch (Exception e) {
                _uiState.postValue(new ReminderUiState.Error(e.getMessage()));
            }
        });
    }

    public void addOrUpdateReminder(String id, String title, String detail, long time, Reminder.Type type) {
        executorService.execute(() -> {
            Reminder reminder = new Reminder(id, type, title, time);
            if (type == Reminder.Type.MEDICINE) {
                reminder.setDosage(detail);
            } else {
                reminder.setLocation(detail);
            }

            if (id == null || id.isEmpty()) {
                repository.addReminder(reminder);
            } else {
                repository.updateReminder(reminder);
            }
            loadReminders(); // Tải lại danh sách sau khi lưu
        });
    }

    public void deleteReminder(String id) {
        executorService.execute(() -> {
            repository.deleteReminder(id);
            loadReminders();
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown(); // Giải phóng tài nguyên
    }
}