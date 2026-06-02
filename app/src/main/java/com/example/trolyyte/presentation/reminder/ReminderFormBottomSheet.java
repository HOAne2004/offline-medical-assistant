package com.example.trolyyte.presentation.reminder;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.trolyyte.databinding.BottomSheetReminderFormBinding;
import com.example.trolyyte.domain.model.Reminder;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.util.Calendar;
import java.util.Locale;

public class ReminderFormBottomSheet extends BottomSheetDialogFragment {

    public interface OnSaveListener {
        void onSave(String title, String detail, long triggerAtMillis, Reminder.Type type);
    }

    private BottomSheetReminderFormBinding binding;
    private OnSaveListener listener;
    private int selectedHour = 8;
    private int selectedMinute = 0;
    private Reminder editingReminder;

    public static ReminderFormBottomSheet newInstance(OnSaveListener listener) {
        ReminderFormBottomSheet fragment = new ReminderFormBottomSheet();
        fragment.listener = listener;
        return fragment;
    }

    public static ReminderFormBottomSheet newEditInstance(Reminder reminder, OnSaveListener listener) {
        ReminderFormBottomSheet fragment = new ReminderFormBottomSheet();
        fragment.editingReminder = reminder;
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

        if (editingReminder != null) {
            setupEditMode();
        }

        setupTimePicker();
        setupSaveButton();
    }

    private void setupEditMode() {
        binding.tvFormTitle.setText("SỬA LỊCH NHẮC");
        binding.etTitle.setText(editingReminder.getTitle());
        
        // Giả định Detail được lưu trong description hoặc các field khác tùy logic
        if (editingReminder.getType() == Reminder.Type.MEDICINE) {
            binding.rbMedicine.setChecked(true);
            binding.etDetail.setText(editingReminder.getDosage());
        } else {
            binding.rbAppointment.setChecked(true);
            binding.etDetail.setText(editingReminder.getLocation());
        }

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(editingReminder.getTriggerAtMillis());
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
        binding.tvTimeValue.setText(String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute));
    }

    private void setupSaveButton() {
        binding.btnSave.setOnClickListener(v -> {
            String title = binding.etTitle.getText().toString().trim();
            String detail = binding.etDetail.getText().toString().trim();
            Reminder.Type type = binding.rbMedicine.isChecked() ? Reminder.Type.MEDICINE : Reminder.Type.APPOINTMENT;

            if (title.isEmpty()) {
                binding.tilTitle.setError("Vui lòng nhập tên");
                return;
            }

            // Tính toán triggerAtMillis dựa trên giờ đã chọn (giả định là hôm nay)
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, selectedHour);
            cal.set(Calendar.MINUTE, selectedMinute);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);

            if (listener != null) {
                listener.onSave(title, detail, cal.getTimeInMillis(), type);
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
