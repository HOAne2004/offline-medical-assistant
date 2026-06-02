package com.example.trolyyte.presentation.reminder;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.example.trolyyte.domain.repository.ReminderRepository;

public class ReminderViewModelFactory implements ViewModelProvider.Factory {
    private final ReminderRepository repository;

    public ReminderViewModelFactory(ReminderRepository repository) {
        this.repository = repository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(ReminderViewModel.class)) {
            return (T) new ReminderViewModel(repository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
