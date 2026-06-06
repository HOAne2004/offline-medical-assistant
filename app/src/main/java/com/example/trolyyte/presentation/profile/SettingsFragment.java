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
import com.example.trolyyte.databinding.FragmentSettingsBinding;
import com.example.trolyyte.di.AppContainer;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private SettingsViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViewModel();
        setupObservers();
        setupListeners();
    }

    private void setupViewModel() {
        AppContainer container = ((MedicalAssistantApplication) requireActivity().getApplication()).appContainer;

        // Cần tạo SettingsViewModelFactory nhận vào settingsRepo và ttsRepo
        SettingsViewModelFactory factory = new SettingsViewModelFactory(
                container.settingsRepository,
                container.ttsRepository
        );
        viewModel = new ViewModelProvider(this, factory).get(SettingsViewModel.class);
    }

    private void setupObservers() {
        viewModel.getCurrentSpeed().observe(getViewLifecycleOwner(), speed -> {
            // Cập nhật vị trí thanh trượt nếu nó chưa khớp
            if (binding.sliderVoiceSpeed.getValue() != speed) {
                binding.sliderVoiceSpeed.setValue(speed);
            }

            // Cập nhật text hiển thị (Bình thường, Chậm, Nhanh)
            String label = "Hiện tại: Bình thường";
            if (speed < 1.0f) label = "Hiện tại: Chậm (" + speed + "x)";
            else if (speed > 1.0f) label = "Hiện tại: Nhanh (" + speed + "x)";
            binding.tvSpeedLabel.setText(label);
        });

        viewModel.getRetentionDays().observe(getViewLifecycleOwner(), days -> {
            if (binding.sliderRetentionDays.getValue() != days) {
                binding.sliderRetentionDays.setValue(days);
            }
            binding.tvRetentionLabel.setText("Hiện tại: Giữ trong " + days + " ngày");
        });

        viewModel.getIsSaved().observe(getViewLifecycleOwner(), isSaved -> {
            if (isSaved) {
                Toast.makeText(requireContext(), "Đã lưu cài đặt giọng nói!", Toast.LENGTH_SHORT).show();
                viewModel.resetSaveState();

                // Trở về Menu Hub sau khi lưu
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        });
    }

    private void setupListeners() {
        // Lắng nghe sự kiện kéo Slider
        binding.sliderVoiceSpeed.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser) {
                viewModel.updateSpeedTemporary(value);
            }
        });

        // Nút Nghe Thử
        binding.btnTestVoice.setOnClickListener(v -> {
            float currentSelectedSpeed = binding.sliderVoiceSpeed.getValue();
            viewModel.testVoice(currentSelectedSpeed);
        });

        binding.sliderRetentionDays.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser) viewModel.updateRetentionTemporary((int) value);
        });

        // Nút Lưu Cài Đặt
        binding.btnSaveSettings.setOnClickListener(v -> {
            float finalSpeed = binding.sliderVoiceSpeed.getValue();
            int finalDays = (int)binding.sliderRetentionDays.getValue();
            viewModel.saveSettings(finalSpeed, finalDays);
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