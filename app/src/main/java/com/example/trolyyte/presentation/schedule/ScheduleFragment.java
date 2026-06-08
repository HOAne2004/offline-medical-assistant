package com.example.trolyyte.presentation.schedule;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.trolyyte.MedicalAssistantApplication;
import com.example.trolyyte.databinding.FragmentScheduleBinding;
import com.example.trolyyte.di.AppContainer;
import com.example.trolyyte.domain.model.Appointment;
import com.example.trolyyte.domain.model.ReminderHistoryWithTemplate;
import com.example.trolyyte.presentation.appointment.AppointmentFormBottomSheet;
import com.example.trolyyte.presentation.reminder.ReminderFormBottomSheet;

public class ScheduleFragment extends Fragment implements ScheduleAdapter.ScheduleCallback {

    private FragmentScheduleBinding binding;
    private ScheduleViewModel viewModel;
    private ScheduleAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentScheduleBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView();
        setupViewModel();
        observeUiState();
        setupListeners();
    }

    private void setupRecyclerView() {
        adapter = new ScheduleAdapter(this);
        binding.rvSchedule.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvSchedule.setAdapter(adapter);
        binding.tvTitle.setText("Lịch trình của bác");
    }

    private void setupViewModel() {
        AppContainer container = ((MedicalAssistantApplication) requireActivity().getApplication()).appContainer;
        ScheduleViewModelFactory factory = container.getScheduleViewModelFactory();
        viewModel = new ViewModelProvider(this, factory).get(ScheduleViewModel.class);
    }

    private void observeUiState() {
        viewModel.getUiState().observe(getViewLifecycleOwner(), state -> {
            if (state instanceof ScheduleUiState.Loading) {
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.tvEmptyState.setVisibility(View.GONE);
            } else if (state instanceof ScheduleUiState.Success) {
                binding.progressBar.setVisibility(View.GONE);
                binding.tvEmptyState.setVisibility(View.GONE);
                adapter.submitList(((ScheduleUiState.Success) state).getItems());
            } else if (state instanceof ScheduleUiState.Empty) {
                binding.progressBar.setVisibility(View.GONE);
                binding.tvEmptyState.setVisibility(View.VISIBLE);
                binding.tvEmptyState.setText("Hôm nay bác không có lịch nhắc hay lịch khám nào.");
                adapter.submitList(null);
            } else if (state instanceof ScheduleUiState.Error) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Lỗi hệ thống: " + ((ScheduleUiState.Error) state).getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupListeners() {
        binding.fabAddSchedule.setOnClickListener(v -> {
            String[] options = {"💊 Thêm lịch nhắc thuốc / sinh hoạt", "🏥 Thêm lịch đi khám bệnh"};
            new AlertDialog.Builder(requireContext())
                    .setTitle("Bác muốn thêm lịch nào?")
                    .setItems(options, (dialog, which) -> {
                        if (which == 0) {
                            showReminderForm(null);
                        } else {
                            showAppointmentForm(null);
                        }
                    }).show();
        });
    }

    // --- ĐIỀU PHỐI ĐÓNG GÓI FORM NHẬP ---
    private void showReminderForm(@Nullable ReminderHistoryWithTemplate data) {
        ReminderFormBottomSheet bottomSheet;
        if (data == null) {
            bottomSheet = ReminderFormBottomSheet.newInstance(null, (title, dosage, instruction, time, type, repeatType, remindMinutes) ->
                    viewModel.addOrUpdateReminder(null, title, dosage, instruction, time, type, repeatType, remindMinutes)
            );
        } else {
            bottomSheet = ReminderFormBottomSheet.newEditInstance(data, (title, dosage, instruction, time, type, repeatType, remindMinutes) ->
                    viewModel.addOrUpdateReminder(data.template.getId(), title, dosage, instruction, time, type, repeatType, remindMinutes)
            );
        }
        bottomSheet.show(getChildFragmentManager(), "REMINDER_FORM");
    }

    private void showAppointmentForm(@Nullable Appointment appointment) {
        AppointmentFormBottomSheet bottomSheet;
        if (appointment == null) {
            bottomSheet = AppointmentFormBottomSheet.newInstance(null, (title, location, doctorName, notes, timeMillis) ->
                    viewModel.addOrUpdateAppointment(null, title, location, doctorName, notes, timeMillis)
            );
        } else {
            bottomSheet = AppointmentFormBottomSheet.newEditInstance(appointment, (title, location, doctorName, notes, timeMillis) ->
                    viewModel.addOrUpdateAppointment(appointment.getId(), title, location, doctorName, notes, timeMillis)
            );
        }
        bottomSheet.show(getChildFragmentManager(), "APPOINTMENT_FORM");
    }

    // --- IMPLEMENTS CALLBACK INTERFACE CHO LỊCH TRÌNH ---
    @Override
    public void onEditReminder(ReminderHistoryWithTemplate data) { showReminderForm(data); }

    @Override
    public void onDeleteReminder(ReminderHistoryWithTemplate data) {
        // Khi xóa, ta xóa bỏ tận gốc đơn thuốc (Template)
        viewModel.deleteReminderTemplate(data.template.getId());
    }

    @Override
    public void onCompleteReminder(ReminderHistoryWithTemplate data) {
        // Khi bấm "Đã uống", ta cập nhật thời gian cho đúng Cữ (History) đó
        viewModel.markReminderCompleted(data.history.getId());
    }

    // --- IMPLEMENTS CALLBACK INTERFACE CHO LỊCH KHÁM ---
    @Override public void onEditAppointment(Appointment appointment) { showAppointmentForm(appointment); }
    @Override public void onDeleteAppointment(Appointment appointment) { viewModel.deleteAppointment(appointment.getId()); }
    @Override public void onCompleteAppointment(Appointment appointment) { viewModel.markAppointmentCompleted(appointment.getId()); }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null) {
            viewModel.loadSchedule();
        }
    }
}