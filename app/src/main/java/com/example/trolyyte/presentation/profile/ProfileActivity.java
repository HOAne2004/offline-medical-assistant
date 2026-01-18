package com.example.trolyyte.presentation.profile;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.trolyyte.R;

// Domain thieeus GetUserProfileUseCase
import com.example.trolyyte.domain.usecase.GetUserProfileUseCase;


public class ProfileActivity extends AppCompatActivity {

    private ProfileViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Init ViewModel
        GetUserProfileUseCase getUserProfileUseCase = new GetUserProfileUseCase();
        ProfileViewModelFactory factory =
                new ProfileViewModelFactory(getUserProfileUseCase);

        viewModel = new ViewModelProvider(this, factory)
                .get(ProfileViewModel.class);

        observeViewModel();

        // 🔥 TRIGGER TEST
        viewModel.onEvent(ProfileUiEvent.LOAD_PROFILE);
    }

    private void observeViewModel() {
        viewModel.getUiState().observe(this, uiState -> {

            Log.d("PROFILE_TEST", "State = " + uiState.getClass().getSimpleName());

            if (uiState instanceof ProfileUiState.Idle) {
                Log.d("PROFILE_TEST", "UI đang ở trạng thái IDLE");
            }

            else if (uiState instanceof ProfileUiState.Loading) {
                showLoading();
            }

            else if (uiState instanceof ProfileUiState.Success) {
                renderProfile((ProfileUiState.Success) uiState);
            }

            else if (uiState instanceof ProfileUiState.Error) {
                ProfileUiState.Error error = (ProfileUiState.Error) uiState;
                Log.e("PROFILE_TEST", "Error = " + error.message);
            }
        });
    }

    private void renderProfile(ProfileUiState.Success state) {
        Log.d("PROFILE_TEST", "Load profile thành công");
        Log.d("PROFILE_TEST", "Name = " + state.fullName);
        Log.d("PROFILE_TEST", "Age = " + state.age);
        Log.d("PROFILE_TEST", "Gender = " + state.gender);
    }

    private void showLoading() {
        Log.d("PROFILE_TEST", "Đang load profile...");
    }
}
