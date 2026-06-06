package com.example.trolyyte.presentation.appointment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.trolyyte.databinding.BottomSheetAppointmentFormBinding;
import com.example.trolyyte.domain.model.Appointment;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class AppointmentFormBottomSheet extends BottomSheetDialogFragment {

    // Interface để truyền dữ liệu ngược lại cho Fragment/ViewModel
    public interface OnSaveAppointmentListener {
        void onSave(String title, String location, String doctorName, String notes, long timeMillis);
    }

    private BottomSheetAppointmentFormBinding binding;
    private OnSaveAppointmentListener listener;
    private Appointment editingAppointment;

    // Sử dụng chung 1 đối tượng Calendar để quản lý cả ngày và giờ
    private final Calendar calendar = Calendar.getInstance();

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    public static AppointmentFormBottomSheet newInstance(java.util.Map<String, String> entities, OnSaveAppointmentListener listener) {
        AppointmentFormBottomSheet fragment = new AppointmentFormBottomSheet();
        fragment.listener = listener;

        if (entities != null) {
            Bundle args = new Bundle();
            // Lấy thông tin mục đích khám (AI có thể trả về "appointment_name", "reason" hoặc "symptom")
            String title = entities.get("appointment_name");
            if (title == null) title = entities.get("reason");
            if (title == null) title = "Tái khám"; // Fallback nếu AI không bóc được chữ tái khám

            args.putString("pre_title", title);
            args.putString("pre_time", entities.get("time"));
            args.putString("pre_date", entities.get("date")); // Lấy Entity ngày (nếu có)
            fragment.setArguments(args);
        }
        return fragment;
    }

    public static AppointmentFormBottomSheet newEditInstance(Appointment appointment, OnSaveAppointmentListener listener) {
        AppointmentFormBottomSheet fragment = new AppointmentFormBottomSheet();
        fragment.editingAppointment = appointment;
        fragment.listener = listener;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Ánh xạ layout thông qua ViewBinding
        binding = BottomSheetAppointmentFormBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (editingAppointment != null) {
            setupEditMode();
        } else {
            updateDateTimeDisplay();
        }

        setupDatePicker();
        setupTimePicker();
        setupSaveButton();

        // TỰ ĐỘNG ĐIỀN LỊCH KHÁM TỪ AI
        if (getArguments() != null) {
            if (getArguments().getString("pre_title") != null) {
                binding.etApptTitle.setText(getArguments().getString("pre_title")); // Điền "Tái khám"
            }

            // Tự động điền Giờ
            String timeStr = getArguments().getString("pre_time");
            if (timeStr != null && timeStr.contains(":")) {
                try {
                    String[] parts = timeStr.split(":");
                    calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(parts[0].trim()));
                    calendar.set(Calendar.MINUTE, Integer.parseInt(parts[1].trim()));
                } catch (Exception ignored) {}
            }

            // Tự động điền Ngày (Nhận diện chữ "ngày mai")
            String dateStr = getArguments().getString("pre_date");
            if (dateStr != null && dateStr.toLowerCase().contains("mai")) {
                calendar.add(Calendar.DAY_OF_YEAR, 1); // Tự động cộng 1 ngày
            }

            updateDateTimeDisplay(); // Cập nhật lại UI hiển thị giờ/ngày
        }
    }

    private void setupEditMode() {
        binding.tvApptFormTitle.setText("SỬA LỊCH KHÁM");
        binding.etApptTitle.setText(editingAppointment.getTitle());
        binding.etApptLocation.setText(editingAppointment.getLocation());
        binding.etApptDoctor.setText(editingAppointment.getDoctorName());
        binding.etApptNotes.setText(editingAppointment.getNotes());

        // Cập nhật lại biến calendar theo thời gian đã lưu
        calendar.setTimeInMillis(editingAppointment.getTimeMillis());
        updateDateTimeDisplay();
    }

    private void setupDatePicker() {
        binding.btnApptSelectDate.setOnClickListener(v -> {
            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Chọn ngày khám")
                    .setSelection(calendar.getTimeInMillis())
                    .build();

            datePicker.addOnPositiveButtonClickListener(selection -> {
                // MaterialDatePicker trả về timestamp chuẩn UTC, ta cần set nó vào Calendar
                // và bóc tách năm/tháng/ngày để tránh lỗi lệch múi giờ
                Calendar utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                utc.setTimeInMillis(selection);

                calendar.set(Calendar.YEAR, utc.get(Calendar.YEAR));
                calendar.set(Calendar.MONTH, utc.get(Calendar.MONTH));
                calendar.set(Calendar.DAY_OF_MONTH, utc.get(Calendar.DAY_OF_MONTH));

                updateDateTimeDisplay();
            });

            datePicker.show(getParentFragmentManager(), "DATE_PICKER");
        });
    }

    private void setupTimePicker() {
        binding.btnApptSelectTime.setOnClickListener(v -> {
            MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_24H)
                    .setHour(calendar.get(Calendar.HOUR_OF_DAY))
                    .setMinute(calendar.get(Calendar.MINUTE))
                    .setTitleText("Chọn giờ khám")
                    .build();

            timePicker.addOnPositiveButtonClickListener(view -> {
                calendar.set(Calendar.HOUR_OF_DAY, timePicker.getHour());
                calendar.set(Calendar.MINUTE, timePicker.getMinute());

                updateDateTimeDisplay();
            });

            timePicker.show(getParentFragmentManager(), "TIME_PICKER");
        });
    }

    private void updateDateTimeDisplay() {
        // Hiển thị ngày và giờ lên 2 nút bấm tương ứng
        binding.btnApptSelectDate.setText(dateFormat.format(calendar.getTime()));
        binding.btnApptSelectTime.setText(timeFormat.format(calendar.getTime()));
    }

    private void setupSaveButton() {
        binding.btnApptSave.setOnClickListener(v -> {
            String title = binding.etApptTitle.getText().toString().trim();
            String location = binding.etApptLocation.getText().toString().trim();
            String doctor = binding.etApptDoctor.getText().toString().trim();
            String notes = binding.etApptNotes.getText().toString().trim();

            // Validate dữ liệu bắt buộc (Ít nhất phải có Mục đích khám và Địa điểm)
            if (title.isEmpty()) {
                binding.tilApptTitle.setError("Vui lòng nhập mục đích khám");
                return;
            } else {
                binding.tilApptTitle.setError(null);
            }

            if (location.isEmpty()) {
                binding.tilApptLocation.setError("Vui lòng nhập địa điểm khám");
                return;
            } else {
                binding.tilApptLocation.setError(null);
            }

            // Gọi Listener truyền dữ liệu ra ngoài
            if (listener != null) {
                listener.onSave(title, location, doctor, notes, calendar.getTimeInMillis());
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