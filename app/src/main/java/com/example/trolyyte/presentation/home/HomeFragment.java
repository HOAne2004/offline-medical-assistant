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
import com.example.trolyyte.domain.model.NlpResult;
import com.example.trolyyte.domain.model.actions.AppointmentAction;
import com.example.trolyyte.domain.model.actions.EmergencyAction;
import com.example.trolyyte.domain.model.actions.MedicationAction;
import com.example.trolyyte.presentation.appointment.AppointmentFormBottomSheet;
import com.example.trolyyte.presentation.reminder.ReminderFormBottomSheet;
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
        setupWaveAnimation();
        observeUiState();
        observeIntentActions();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (viewModel != null) {
            viewModel.stopListening();
        }
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

        // THAY ĐỔI SỰ KIỆN CLICK MIC
        micButton.setOnClickListener(v -> {
            HomeUiState state = viewModel.getUiState().getValue();
            // Nếu đang nghe -> Bấm phát nữa là Tắt ngay lập tức
            if (state instanceof HomeUiState.Listening || state instanceof HomeUiState.PartialResult) {
                viewModel.stopListening();
                stopWaveAnimation();
            } else {
                // Nếu đang nghỉ -> Bật mic lên
                checkPermissionAndStart();
            }
        });

        // Nhấn giữ (Long click) -> Mở hộp thoại nhập Text dự phòng
        micButton.setOnLongClickListener(v -> {
            showTextInputDialog();
            return true;
        });
    }

    private void showTextInputDialog() {
        android.widget.EditText input = new android.widget.EditText(requireContext());
        input.setHint("Nhập câu lệnh (VD: Nhắc tôi uống thuốc...)");

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Chế độ Demo (Nhập Text)")
                .setView(input)
                .setPositiveButton("Gửi", (dialog, which) -> {
                    String text = input.getText().toString().trim();
                    if (!text.isEmpty()) {
                        // Bắn thẳng Text vào ViewModel, bỏ qua Vosk
                        viewModel.testTextDirectly(text);
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
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

    private void observeIntentActions() {
        viewModel.getActionLiveData().observe(getViewLifecycleOwner(), action -> {
            if (action instanceof MedicationAction) {
                NlpResult result = ((MedicationAction) action).getResult();
                showMedicineForm(result.getEntities());

            } else if (action instanceof AppointmentAction) {
                NlpResult result = ((AppointmentAction) action).getResult();
                showAppointmentForm(result.getEntities());

            } else if (action instanceof EmergencyAction) {
                executeEmergencyCall();
            }
        });
    }

    // Trong HomeFragment.java

    private void showMedicineForm(java.util.Map<String, String> entities) {
        ReminderFormBottomSheet bottomSheet = ReminderFormBottomSheet.newInstance(entities,
                (title, dosage, instruction, triggerAt, type, repeat, minutes) -> {
                    // GỌI VIEWMODEL ĐỂ LƯU THẬT SỰ
                    viewModel.saveMedicineReminder(title, dosage, instruction, triggerAt, type, repeat, minutes);
                    Toast.makeText(requireContext(), "Đã lưu thuốc: " + title, Toast.LENGTH_SHORT).show();
                });
        bottomSheet.show(getParentFragmentManager(), "MED_FORM");
    }

    private void showAppointmentForm(java.util.Map<String, String> entities) {
        AppointmentFormBottomSheet bottomSheet = AppointmentFormBottomSheet.newInstance(entities,
                (title, location, doctorName, notes, timeMillis) -> {
                    // GỌI VIEWMODEL ĐỂ LƯU THẬT SỰ
                    viewModel.saveAppointment(title, location, doctorName, notes, timeMillis);
                    Toast.makeText(requireContext(), "Đã lưu lịch khám: " + title, Toast.LENGTH_SHORT).show();
                });
        bottomSheet.show(getParentFragmentManager(), "APPT_FORM");
    }

    private void executeEmergencyCall() {
        // Tương lai lấy từ UserProfile, nay gán cứng số điện thoại người thân để test (VD số của bác)
        String emergencyPhone = "0987654321";

        // 1. Kiểm tra xem app đã có quyền tự động gọi chưa
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            // CÓ QUYỀN -> GỌI THẲNG KHÔNG CẦN CHẠM TAY
            android.content.Intent callIntent = new android.content.Intent(android.content.Intent.ACTION_CALL);
            callIntent.setData(android.net.Uri.parse("tel:" + emergencyPhone));
            startActivity(callIntent);
        } else {
            // CHƯA CÓ QUYỀN -> Fallback về việc mở màn hình quay số (ACTION_DIAL) cho an toàn
            Toast.makeText(requireContext(), "Bác chưa cấp quyền Gọi điện tự động!", Toast.LENGTH_LONG).show();
            android.content.Intent dialIntent = new android.content.Intent(android.content.Intent.ACTION_DIAL);
            dialIntent.setData(android.net.Uri.parse("tel:" + emergencyPhone));
            startActivity(dialIntent);
        }
    }
}