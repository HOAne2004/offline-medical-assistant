package com.example.trolyyte.presentation.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.trolyyte.domain.repository.UserProfileRepository;

public class ProfileViewModel extends ViewModel {
    private final UserProfileRepository repository;
    private final MutableLiveData<ProfileUiState> _uiState = new MutableLiveData<>(ProfileUiState.idle());

    public ProfileViewModel(UserProfileRepository repository) {
        this.repository = repository;
        loadProfile();
    }

    public LiveData<ProfileUiState> getUiState() { return _uiState; }

    private void loadProfile() {
        String name = repository.getUserName();
        String phone = repository.getEmergencyPhone();
        String history = repository.getMedicalHistory();
        _uiState.setValue(new ProfileUiState(name, phone, history, false, false, null));
    }

    public void saveProfile(String name, String phone, String history) {
        _uiState.setValue(_uiState.getValue().copyWith(null, null, null, true, false, null));
        try {
            repository.saveProfile(name, phone, history);
            _uiState.setValue(new ProfileUiState(name, phone, history, false, true, null));
        } catch (Exception e) {
            _uiState.setValue(_uiState.getValue().copyWith(null, null, null, false, false, e.getMessage()));
        }
    }

    public void resetSaveState() {
        ProfileUiState current = _uiState.getValue();
        if (current != null) {
            _uiState.setValue(current.copyWith(null, null, null, false, false, null));
        }
    }
}
