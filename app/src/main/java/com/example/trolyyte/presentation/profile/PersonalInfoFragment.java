package com.example.trolyyte.presentation.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.trolyyte.MedicalAssistantApplication;
import com.example.trolyyte.databinding.FragmentPersonalInfoBinding;
import com.example.trolyyte.di.AppContainer;
import com.example.trolyyte.domain.model.UserProfile;

public class PersonalInfoFragment extends Fragment {
    private FragmentPersonalInfoBinding binding;
    private ProfileViewModel viewModel;

    // Lưu lại profile hiện tại để khi cập nhật không làm mất các trường khác (như tuổi, giới tính...)
    private UserProfile currentUserProfile = new UserProfile();

    // Biến cờ để biết người dùng vừa bấm nút lưu, dùng để hiển thị Toast đúng lúc
    private boolean isSaving = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPersonalInfoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViewModel();
        observeUiState();
        setupListeners();
    }

    private void setupViewModel() {
        // CẬP NHẬT: Lấy factory đã chứa sẵn các UseCase từ AppContainer
        AppContainer container = ((MedicalAssistantApplication) requireActivity().getApplication()).appContainer;
        ProfileViewModelFactory factory = container.getProfileViewModelFactory();

        viewModel = new ViewModelProvider(this, factory).get(ProfileViewModel.class);
    }

    private void observeUiState() {
        viewModel.getUiState().observe(getViewLifecycleOwner(), state -> {
            if (state instanceof ProfileUiState.Loading) {
                // Đang tải hoặc đang lưu dữ liệu
                binding.loadingBar.setVisibility(View.VISIBLE);
                binding.btnSave.setEnabled(false);

            } else if (state instanceof ProfileUiState.Success) {
                // Xử lý thành công (Lấy dữ liệu lên hoặc lưu xong)
                binding.loadingBar.setVisibility(View.GONE);
                binding.btnSave.setEnabled(true);

                currentUserProfile = ((ProfileUiState.Success) state).getProfile();

                // Chỉ set text nếu ô đang trống (tránh việc ghi đè khi user đang gõ chữ)
                if (binding.etName.getText().toString().isEmpty() && currentUserProfile.getName() != null) {
                    binding.etName.setText(currentUserProfile.getName());
                    binding.etPhone.setText(currentUserProfile.getEmergencyPhone());
                    binding.etHistory.setText(currentUserProfile.getMedicalHistory());
                }

                // Nếu trước đó vừa bấm nút lưu, hiển thị thông báo thành công
                if (isSaving) {
                    Toast.makeText(getContext(), "Đã cập nhật hồ sơ thành công!", Toast.LENGTH_SHORT).show();
                    isSaving = false; // Reset cờ
                }

            } else if (state instanceof ProfileUiState.Error) {
                // Xử lý lỗi
                binding.loadingBar.setVisibility(View.GONE);
                binding.btnSave.setEnabled(true);
                isSaving = false;

                String errorMsg = ((ProfileUiState.Error) state).getMessage();
                Toast.makeText(getContext(), "Lỗi: " + errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupListeners() {
        binding.btnSave.setOnClickListener(v -> {
            // Cập nhật các trường trên giao diện vào Object profile hiện tại
            currentUserProfile.setName(binding.etName.getText().toString());
            currentUserProfile.setEmergencyPhone(binding.etPhone.getText().toString());
            currentUserProfile.setMedicalHistory(binding.etHistory.getText().toString());

            isSaving = true; // Bật cờ đang lưu

            // Gọi ViewModel đẩy xuống DB
            viewModel.saveProfile(currentUserProfile);
        });

        binding.btnBack.setOnClickListener(v -> {
            if (getParentFragmentManager() != null) {
                getParentFragmentManager().popBackStack();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}