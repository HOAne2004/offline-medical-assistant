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

public class PersonalInfoFragment extends Fragment {
    private FragmentPersonalInfoBinding binding;
    private ProfileViewModel viewModel;

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
        // Lấy container từ Activity chứa Fragment này
        AppContainer container = ((MedicalAssistantApplication) requireActivity().getApplication()).appContainer;
        ProfileViewModelFactory factory = new ProfileViewModelFactory(container.userProfileRepository);
        viewModel = new ViewModelProvider(this, factory).get(ProfileViewModel.class);
    }

    private void observeUiState() {
        // LUÔN DÙNG getViewLifecycleOwner() trong Fragment để tránh rò rỉ bộ nhớ
        viewModel.getUiState().observe(getViewLifecycleOwner(), state -> {
            if (binding.etName.getText().toString().isEmpty() && !state.getName().isEmpty()) {
                binding.etName.setText(state.getName());
                binding.etPhone.setText(state.getEmergencyPhone());
                binding.etHistory.setText(state.getMedicalHistory());
            }

            binding.loadingBar.setVisibility(state.isLoading() ? View.VISIBLE : View.GONE);
            binding.btnSave.setEnabled(!state.isLoading());

            if (state.isSaved()) {
                Toast.makeText(getContext(), "Đã cập nhật hồ sơ thành công!", Toast.LENGTH_SHORT).show();
                viewModel.resetSaveState();
            }
        });
    }

    private void setupListeners() {
        binding.btnSave.setOnClickListener(v -> {
            String name = binding.etName.getText().toString();
            String phone = binding.etPhone.getText().toString();
            String history = binding.etHistory.getText().toString();
            viewModel.saveProfile(name, phone, history);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Bắt buộc giải phóng ViewBinding khi Fragment bị hủy
    }
}