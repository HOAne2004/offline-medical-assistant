package com.example.trolyyte.presentation.profile;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.trolyyte.domain.usecase.GetUserProfileUseCase;
import com.example.trolyyte.domain.usecase.SaveUserProfileUseCase;

public class ProfileViewModelFactory implements ViewModelProvider.Factory {
    private final GetUserProfileUseCase getUserProfileUseCase;
    private final SaveUserProfileUseCase saveUserProfileUseCase;

    public ProfileViewModelFactory(GetUserProfileUseCase getUserProfileUseCase, SaveUserProfileUseCase saveUserProfileUseCase) {
        this.getUserProfileUseCase = getUserProfileUseCase;
        this.saveUserProfileUseCase = saveUserProfileUseCase;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(ProfileViewModel.class)) {
            return (T) new ProfileViewModel(getUserProfileUseCase, saveUserProfileUseCase);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}