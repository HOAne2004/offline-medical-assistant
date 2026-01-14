package com.example.trolyyte.presentation.home;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.trolyyte.MedicalAssistantApplication;
import com.example.trolyyte.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class HomeActivity extends AppCompatActivity {

    private HomeViewModel viewModel;

    // UI Components
    private TextView greetingText;
    private TextView instructionText;
    private FloatingActionButton micButton;
    private TextView buttonLabel;

    // Permission Launcher
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    viewModel.startListening();
                } else {
                    Toast.makeText(this, "Ứng dụng cần quyền ghi âm để hoạt động", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        setupDependencies();
        setupViews();
        observeUiState();
    }

    private void setupDependencies() {
        // Lấy Factory từ AppContainer (Manual DI)
        MedicalAssistantApplication app = (MedicalAssistantApplication) getApplication();
        HomeViewModelFactory factory = app.appContainer.getHomeViewModelFactory();

        // Tạo ViewModel
        viewModel = new ViewModelProvider(this, factory).get(HomeViewModel.class);
    }

    private void setupViews() {
        greetingText = findViewById(R.id.greetingText);
        instructionText = findViewById(R.id.instructionText);
        micButton = findViewById(R.id.micButton);
        buttonLabel = findViewById(R.id.buttonLabel);

        micButton.setOnClickListener(v -> {
            checkPermissionAndStart();
        });
    }

    private void checkPermissionAndStart() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            // Nếu mic đang bật thì tắt, tắt thì bật (Toggle logic)
            // Tuy nhiên logic Clean Architecture của bạn đang tự động tắt khi có kết quả
            // Nên nút này chủ yếu dùng để BẮT ĐẦU
            viewModel.startListening();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO);
        }
    }

    private void observeUiState() {
        viewModel.getUiState().observe(this, state -> {
            // Reset trạng thái mặc định trước khi update
            micButton.setEnabled(true);

            if (state instanceof HomeUiState.Idle) {
                updateUiIdle();
            }
            else if (state instanceof HomeUiState.Listening) {
                updateUiListening();
            }
            else if (state instanceof HomeUiState.PartialResult) {
                String text = ((HomeUiState.PartialResult) state).text;
                updateUiPartialResult(text);
            }
            else if (state instanceof HomeUiState.Processing) {
                updateUiProcessing();
            }
            else if (state instanceof HomeUiState.Speaking) {
                String text = ((HomeUiState.Speaking) state).text;
                updateUiSpeaking(text);
            }
            else if (state instanceof HomeUiState.Success) {
                String msg = ((HomeUiState.Success) state).message;
                updateUiSuccess(msg);
            }
            else if (state instanceof HomeUiState.Error) {
                String err = ((HomeUiState.Error) state).message;
                updateUiError(err);
            }
        });
    }

    // --- Các hàm cập nhật UI cụ thể ---

    private void updateUiIdle() {
        micButton.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.primary_blue))); // Màu xanh
        micButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_mic));
        buttonLabel.setText("Nhấn để nói");
        instructionText.setText("Bác cần giúp gì không ạ?");
    }

    private void updateUiListening() {
        micButton.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.red_listening))); // Màu đỏ (đang thu)
        // Có thể thêm animation nhấp nháy tại đây
        buttonLabel.setText("Đang nghe...");
        instructionText.setText("Cháu đang lắng nghe bác...");
    }

    private void updateUiPartialResult(String text) {
        // Hiện chữ mờ mờ khi đang nói
        if (!text.isEmpty()) {
            instructionText.setText(text);
        }
    }

    private void updateUiProcessing() {
        micButton.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.gray_processing))); // Màu xám
        buttonLabel.setText("Đang suy nghĩ...");
        instructionText.setText("Cháu đang xử lý...");
    }

    private void updateUiSpeaking(String text) {
        micButton.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.green_speaking))); // Màu xanh lá (đang nói)
        buttonLabel.setText("Trợ lý đang nói");
        // Hiển thị câu đang nói lên màn hình (Subtitle) -> Rất tốt cho người già
        instructionText.setText(text);

        // Tạm khóa nút mic để tránh bấm nhầm khi máy đang nói
        micButton.setEnabled(false);
    }

    private void updateUiSuccess(String msg) {
        instructionText.setText(msg);
        // Có thể hiện Toast hoặc Dialog nhỏ
    }

    private void updateUiError(String err) {
        micButton.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.red_error)));
        instructionText.setText("Có lỗi: " + err);
        buttonLabel.setText("Thử lại");
    }
}