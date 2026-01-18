package com.example.trolyyte.presentation.profile;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.trolyyte.R;
import com.example.trolyyte.domain.usecase.GetUserProfileUseCase;

import android.util.Log;

public class ProfileActivity extends AppCompatActivity {
    private ProfileViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        GetUserProfileUseCase getUserProfileUseCase = new GetUserProfileUseCase();
        ProfileViewModelFactory factory = new ProfileViewModelFactory(getUserProfileUseCase);
        viewModel = new ViewModelProvider(this, factory).get(ProfileViewModel.class);

        observeViewModel();
        viewModel.onEvent(ProfileUiEvent.LOAD_PROFILE);
    
    }
    private void observeViewModel() {
        viewModel.uiState.observe(this, uiState -> {
            if (uiState instanceof ProfileUiState.Loading) {
                // Hiển thị trạng thái đang tải
            } else if (uiState instanceof ProfileUiState.Success) {
                ProfileUiState.Success successState = (ProfileUiState.Success) uiState;
                // Cập nhật giao diện với dữ liệu hồ sơ người dùng
            } else if (uiState instanceof ProfileUiState.Error) {
                ProfileUiState.Error errorState = (ProfileUiState.Error) uiState;
                // Hiển thị thông báo lỗi
            }
        });
    }
    // private void renderProfile(ProfileUiState.Success state) {
    //     // Cập nhật giao diện người dùng với dữ liệu hồ sơ
    // }
    // private void showLoading() {
    //     // Hiển thị chỉ báo tải
    // }
}

