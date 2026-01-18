package com.example.trolyyte.presentation.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

// Domain thieu UserProfile model va GetUserProfileUseCase
import com.example.trolyyte.domain.model.UserProfile;
import com.example.trolyyte.domain.usecase.GetUserProfileUseCase;

public class ProfileViewModel extends ViewModel {
    private final GetUserProfileUseCase getUserProfileUseCase;

    private final MutableLiveData<ProfileUiState> _uiState = new MutableLiveData<>(new ProfileUiState.Idle());
    public LiveData<ProfileUiState> uiState = _uiState;

    public ProfileViewModel(GetUserProfileUseCase getUserProfileUseCase) {
        this.getUserProfileUseCase = getUserProfileUseCase;
    }

    public void onEvent(ProfileUiEvent event) {
        switch (event) {
            case LOAD_PROFILE:
            case REFRESH_PROFILE:
                loadUserProfile();
                break;
        }
    }

    private void loadUserProfile() {
        _uiState.setValue(new ProfileUiState.Loading());
        try {
            UserProfile profile = getUserProfileUseCase.execute();
            _uiState.setValue(new ProfileUiState.Success(
                    profile.getFullName(),
                    profile.getAge(),
                    profile.getGender(),
                    profile.getMedicalHistory(),
                    profile.getCurrentMedications(),
                    profile.getReminderMessage()
            ));
        } catch (Exception e) {
            _uiState.setValue(new ProfileUiState.Error("Failed to load profile: " + e.getMessage()));
        }
    }
    
    @Override
    protected void onCleared() {
        super.onCleared();
    }
}
