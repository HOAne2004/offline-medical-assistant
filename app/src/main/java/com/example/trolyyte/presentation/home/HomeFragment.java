package com.example.trolyyte.presentation.home;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.trolyyte.MedicalAssistantApplication;
import com.example.trolyyte.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class HomeFragment extends Fragment {

    private HomeViewModel viewModel;

    private TextView greetingText;
    private TextView instructionText;
    private FloatingActionButton micButton;
    private View waveBackground;
    private ObjectAnimator waveAnimator; // Trình diễn hiệu ứng sóng

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    viewModel.startListening();
                } else {
                    Toast.makeText(requireContext(), "Cần quyền ghi âm!", Toast.LENGTH_SHORT).show();
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupDependencies();
        setupViews(view);
        setupWaveAnimation(); // Khởi tạo hiệu ứng sóng
        observeUiState();
    }

    private void setupDependencies() {
        MedicalAssistantApplication app = (MedicalAssistantApplication) requireActivity().getApplication();
        HomeViewModelFactory factory = app.appContainer.getHomeViewModelFactory();
        viewModel = new ViewModelProvider(this, factory).get(HomeViewModel.class);
    }

    private void setupViews(View view) {
        greetingText = view.findViewById(R.id.greetingText);
        instructionText = view.findViewById(R.id.instructionText);
        micButton = view.findViewById(R.id.micButton);
        waveBackground = view.findViewById(R.id.waveBackground);

        micButton.setOnClickListener(v -> checkPermissionAndStart());
    }

    // TẠO HIỆU ỨNG SÓNG ÂM
    private void setupWaveAnimation() {
        waveAnimator = ObjectAnimator.ofPropertyValuesHolder(
                waveBackground,
                PropertyValuesHolder.ofFloat("scaleX", 1f, 1.5f),
                PropertyValuesHolder.ofFloat("scaleY", 1f, 1.5f),
                PropertyValuesHolder.ofFloat("alpha", 1f, 0f)
        );
        waveAnimator.setDuration(1200);
        waveAnimator.setRepeatCount(ValueAnimator.INFINITE);
        waveAnimator.setRepeatMode(ValueAnimator.RESTART);
    }

    private void checkPermissionAndStart() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            viewModel.startListening();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO);
        }
    }

    private void observeUiState() {
        viewModel.getUiState().observe(getViewLifecycleOwner(), state -> {
            micButton.setEnabled(true);
            stopWaveAnimation(); // Tắt sóng theo mặc định

            if (state instanceof HomeUiState.Idle) {
                updateUiIdle();
            } else if (state instanceof HomeUiState.Listening) {
                updateUiListening();
            } else if (state instanceof HomeUiState.PartialResult) {
                instructionText.setText(((HomeUiState.PartialResult) state).text);
            } else if (state instanceof HomeUiState.Processing) {
                updateUiProcessing();
            } else if (state instanceof HomeUiState.Speaking) {
                updateUiSpeaking(((HomeUiState.Speaking) state).text);
            }
        });
    }

    private void startWaveAnimation() {
        waveBackground.setVisibility(View.VISIBLE);
        if (!waveAnimator.isRunning()) waveAnimator.start();
    }

    private void stopWaveAnimation() {
        if (waveAnimator.isRunning()) waveAnimator.cancel();
        waveBackground.setVisibility(View.INVISIBLE);
        waveBackground.setScaleX(1f);
        waveBackground.setScaleY(1f);
    }

    private void updateUiIdle() {
        micButton.setBackgroundTintList(ColorStateList.valueOf(requireContext().getColor(R.color.primary_blue)));
        instructionText.setText("Nhấn nút micro để nói");
        greetingText.setText("Xin chào bác!");
    }

    private void updateUiListening() {
        micButton.setBackgroundTintList(ColorStateList.valueOf(requireContext().getColor(R.color.red_listening)));
        instructionText.setText("Cháu đang lắng nghe...");
        startWaveAnimation(); // BẬT SÓNG ÂM KHI ĐANG NGHE
    }

    private void updateUiProcessing() {
        micButton.setBackgroundTintList(ColorStateList.valueOf(requireContext().getColor(R.color.gray_processing)));
        instructionText.setText("Cháu đang suy nghĩ...");
    }

    private void updateUiSpeaking(String text) {
        micButton.setBackgroundTintList(ColorStateList.valueOf(requireContext().getColor(R.color.green_speaking)));
        instructionText.setText(text); // Subtitle
        micButton.setEnabled(false);
    }
}