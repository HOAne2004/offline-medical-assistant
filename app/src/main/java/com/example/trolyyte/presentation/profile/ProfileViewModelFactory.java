package com.example.trolyyte.presentation.profile;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

// Domain thieu GetUserProfileUseCase
import com.example.trolyyte.domain.usecase.GetUserProfileUseCase;

public class ProfileViewModelFactory implements ViewModelProvider.Factory {
    private final GetUserProfileUseCase getUserProfileUseCase;

    public ProfileViewModelFactory(GetUserProfileUseCase getUserProfileUseCase) {
        this.getUserProfileUseCase = getUserProfileUseCase;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(ProfileViewModel.class)) {
            return (T) new ProfileViewModel(getUserProfileUseCase);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
