package com.example.trolyyte.presentation.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.trolyyte.domain.model.UserProfile;
import com.example.trolyyte.domain.usecase.GetUserProfileUseCase;
import com.example.trolyyte.domain.usecase.SaveUserProfileUseCase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProfileViewModel extends ViewModel {
    private final GetUserProfileUseCase getUserProfileUseCase;
    private final SaveUserProfileUseCase saveUserProfileUseCase;

    private final MutableLiveData<ProfileUiState> _uiState = new MutableLiveData<>();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public ProfileViewModel(GetUserProfileUseCase getUserProfileUseCase, SaveUserProfileUseCase saveUserProfileUseCase) {
        this.getUserProfileUseCase = getUserProfileUseCase;
        this.saveUserProfileUseCase = saveUserProfileUseCase;
        loadProfile();
    }

    public LiveData<ProfileUiState> getUiState() { return _uiState; }

    public void loadProfile() {
        _uiState.postValue(new ProfileUiState.Loading()); // Cập nhật UI ở Main Thread

        executorService.execute(() -> {
            try {
                // Lấy profile từ Database thông qua UseCase
                UserProfile profile = getUserProfileUseCase.execute();
                _uiState.postValue(new ProfileUiState.Success(profile)); // postValue từ Background Thread
            } catch (Exception e) {
                _uiState.postValue(new ProfileUiState.Error(e.getMessage()));
            }
        });
    }

    public void saveProfile(UserProfile profile) {
        _uiState.postValue(new ProfileUiState.Loading());

        executorService.execute(() -> {
            try {
                // Lưu profile xuống Database
                saveUserProfileUseCase.execute(profile);

                // Tải lại dữ liệu để cập nhật UI thành công
                loadProfile();
            } catch (Exception e) {
                _uiState.postValue(new ProfileUiState.Error("Lỗi khi lưu: " + e.getMessage()));
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // Ngắt luồng ngầm để chống tràn bộ nhớ (Memory Leak)
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdownNow();
        }
    }
}