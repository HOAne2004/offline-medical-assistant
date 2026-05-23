package com.example.trolyyte.presentation.profile;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.example.trolyyte.domain.repository.UserProfileRepository;

public class ProfileViewModelFactory implements ViewModelProvider.Factory {
    private final UserProfileRepository repository;

    public ProfileViewModelFactory(UserProfileRepository repository) {
        this.repository = repository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(ProfileViewModel.class)) {
            return (T) new ProfileViewModel(repository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
