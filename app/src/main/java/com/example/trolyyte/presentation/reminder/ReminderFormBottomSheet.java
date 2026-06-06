package com.example.trolyyte.presentation.reminder;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.trolyyte.databinding.BottomSheetReminderFormBinding;
import com.example.trolyyte.domain.model.ReminderHistoryWithTemplate; // UPDATE
import com.example.trolyyte.domain.model.ReminderTemplate; // UPDATE
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.util.Calendar;
import java.util.Locale;

public class ReminderFormBottomSheet extends BottomSheetDialogFragment {

    // UPDATE: Thay đổi Enum cho chuẩn Model mới
    public interface OnSaveListener {
        void onSave(String title, String dosage, String instruction, long triggerAtMillis,
                    ReminderTemplate.Type type, ReminderTemplate.RepeatType repeatType, int remindMinutes);
    }

    private BottomSheetReminderFormBinding binding;
    private OnSaveListener listener;
    private int selectedHour = 8;
    private int selectedMinute = 0;

    // UPDATE: Sử dụng đối tượng gộp
    private ReminderHistoryWithTemplate editingData;

    // Dữ liệu cho Spinner
    private final String[] repeatOptions = {"Không lặp lại", "Hàng ngày", "Hàng tuần", "Hàng tháng"};
    private final ReminderTemplate.RepeatType[] repeatValues = {ReminderTemplate.RepeatType.NONE, ReminderTemplate.RepeatType.DAILY, ReminderTemplate.RepeatType.WEEKLY, ReminderTemplate.RepeatType.MONTHLY};

    private final String[] remindOptions = {"Đúng giờ", "Trước 5 phút", "Trước 10 phút", "Trước 15 phút", "Trước 30 phút"};
    private final int[] remindValues = {0, 5, 10, 15, 30};

    public static ReminderFormBottomSheet newInstance(java.util.Map<String, String> entities, OnSaveListener listener) {
        ReminderFormBottomSheet fragment = new ReminderFormBottomSheet();
        fragment.listener = listener;

        if (entities != null) {
            Bundle args = new Bundle();
            // Lấy thực thể (NLU có thể trả về key khác nhau tùy cách train)
            String medName = entities.get("medicine_name");
            String routineName = entities.get("routine_name");
            if (routineName == null) routineName = entities.get("activity"); // Dự phòng key

            if (medName != null && !medName.isEmpty()) {
                args.putString("pre_title", medName);
                args.putString("pre_type", "MEDICINE");
            } else if (routineName != null && !routineName.isEmpty()) {
                args.putString("pre_title", routineName);
                args.putString("pre_type", "ROUTINE");
            }

            args.putString("pre_time", entities.get("time"));
            fragment.setArguments(args);
        }
        return fragment;
    }

    public static ReminderFormBottomSheet newEditInstance(ReminderHistoryWithTemplate data, OnSaveListener listener) {
        ReminderFormBottomSheet fragment = new ReminderFormBottomSheet();
        fragment.editingData = data;
        fragment.listener = listener;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = BottomSheetReminderFormBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupSpinners();
        setupTypeToggle();

        if (editingData != null) {
            setupEditMode();
        }
        setupTimePicker();
        setupSaveButton();

        // TỰ ĐỘNG ĐIỀN DỮ LIỆU TỪ AI
        if (getArguments() != null) {
            String title = getArguments().getString("pre_title");
            String type = getArguments().getString("pre_type");
            String timeStr = getArguments().getString("pre_time");

            if (title != null) binding.etTitle.setText(title);

            if ("ROUTINE".equals(type)) {
                binding.rbRoutine.setChecked(true); // Tự động chọn Sinh hoạt
            } else if ("MEDICINE".equals(type)) {
                binding.rbMedicine.setChecked(true); // Tự động chọn Uống thuốc
            }

            // Xử lý điền giờ (VD: AI trả về "08:00")
            if (timeStr != null && timeStr.contains(":")) {
                try {
                    String[] parts = timeStr.split(":");
                    selectedHour = Integer.parseInt(parts[0].trim());
                    selectedMinute = Integer.parseInt(parts[1].trim());
                    updateTimeDisplay();
                } catch (Exception ignored) {}
            }
        }
    }

    private void setupSpinners() {
        ArrayAdapter<String> repeatAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, repeatOptions);
        binding.spRepeatType.setAdapter(repeatAdapter);

        ArrayAdapter<String> remindAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, remindOptions);
        binding.spRemindMinutes.setAdapter(remindAdapter);
        binding.spRemindMinutes.setSelection(3); // Mặc định là Trước 15 phút
    }

    private void setupTypeToggle() {
        binding.rgType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == binding.rbMedicine.getId()) {
                binding.tilDosage.setVisibility(View.VISIBLE);
            } else {
                binding.tilDosage.setVisibility(View.GONE);
                binding.etDosage.setText("");
            }
        });
    }

    private void setupEditMode() {
        binding.tvFormTitle.setText("SỬA LỊCH NHẮC");
        binding.etTitle.setText(editingData.template.getTitle());
        binding.etDescription.setText(editingData.template.getInstruction()); // Map sang Y lệnh (trước/sau ăn)

        if (editingData.template.getType() == ReminderTemplate.Type.MEDICINE) {
            binding.rbMedicine.setChecked(true);
            binding.etDosage.setText(editingData.template.getDosage());
        } else {
            binding.rbRoutine.setChecked(true);
        }

        // Setup Spinner cho Repeat
        for (int i = 0; i < repeatValues.length; i++) {
            if (repeatValues[i] == editingData.template.getRepeatType()) {
                binding.spRepeatType.setSelection(i);
                break;
            }
        }

        // Setup Spinner cho RemindMinutes
        for (int i = 0; i < remindValues.length; i++) {
            if (remindValues[i] == editingData.template.getRemindMinutes()) {
                binding.spRemindMinutes.setSelection(i);
                break;
            }
        }

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(editingData.history.getScheduledTimeMillis());
        selectedHour = cal.get(Calendar.HOUR_OF_DAY);
        selectedMinute = cal.get(Calendar.MINUTE);
        updateTimeDisplay();
    }

    private void setupTimePicker() {
        binding.btnSelectTime.setOnClickListener(v -> {
            MaterialTimePicker picker = new MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_24H)
                    .setHour(selectedHour)
                    .setMinute(selectedMinute)
                    .setTitleText("Chọn giờ nhắc")
                    .build();
            picker.addOnPositiveButtonClickListener(view -> {
                selectedHour = picker.getHour();
                selectedMinute = picker.getMinute();
                updateTimeDisplay();
            });
            picker.show(getParentFragmentManager(), "TIME_PICKER");
        });
    }

    private void updateTimeDisplay() {
        binding.btnSelectTime.setText(String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute));
    }

    private void setupSaveButton() {
        binding.btnSave.setOnClickListener(v -> {
            String title = binding.etTitle.getText().toString().trim();
            String dosage = binding.etDosage.getText().toString().trim();
            String instruction = binding.etDescription.getText().toString().trim();

            ReminderTemplate.Type type = binding.rbMedicine.isChecked() ? ReminderTemplate.Type.MEDICINE : ReminderTemplate.Type.ROUTINE;
            ReminderTemplate.RepeatType repeatType = repeatValues[binding.spRepeatType.getSelectedItemPosition()];
            int remindMinutes = remindValues[binding.spRemindMinutes.getSelectedItemPosition()];

            if (title.isEmpty()) {
                binding.tilTitle.setError("Vui lòng nhập tên");
                return;
            }

            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, selectedHour);
            cal.set(Calendar.MINUTE, selectedMinute);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);

            if (listener != null) {
                listener.onSave(title, dosage, instruction, cal.getTimeInMillis(), type, repeatType, remindMinutes);
            }
            dismiss();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}