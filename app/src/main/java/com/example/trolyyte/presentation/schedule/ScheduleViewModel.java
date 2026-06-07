package com.example.trolyyte.presentation.schedule;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.trolyyte.domain.model.Appointment;
import com.example.trolyyte.domain.model.ReminderHistoryWithTemplate;
import com.example.trolyyte.domain.model.ReminderTemplate;
import com.example.trolyyte.domain.usecase.GetAppointmentsUseCase;
import com.example.trolyyte.domain.usecase.GetRemindersUseCase;
import com.example.trolyyte.domain.usecase.ManageAppointmentUseCase;
import com.example.trolyyte.domain.usecase.ManageReminderUseCase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ScheduleViewModel extends ViewModel {

    private final GetRemindersUseCase getRemindersUseCase;
    private final GetAppointmentsUseCase getAppointmentsUseCase;
    private final ManageReminderUseCase manageReminderUseCase;
    private final ManageAppointmentUseCase manageAppointmentUseCase;

    private final MutableLiveData<ScheduleUiState> _uiState = new MutableLiveData<>();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public LiveData<ScheduleUiState> getUiState() { return _uiState; }

    public ScheduleViewModel(GetRemindersUseCase getRemindersUseCase,
                             GetAppointmentsUseCase getAppointmentsUseCase,
                             ManageReminderUseCase manageReminderUseCase,
                             ManageAppointmentUseCase manageAppointmentUseCase) {
        this.getRemindersUseCase = getRemindersUseCase;
        this.getAppointmentsUseCase = getAppointmentsUseCase;
        this.manageReminderUseCase = manageReminderUseCase;
        this.manageAppointmentUseCase = manageAppointmentUseCase;
        loadSchedule();
    }

    public void loadSchedule() {
        _uiState.postValue(new ScheduleUiState.Loading());
        executorService.execute(() -> {
            try {
                List<ReminderHistoryWithTemplate> reminders = getRemindersUseCase.execute();
                List<Appointment> appointments = getAppointmentsUseCase.execute();

                // Lấy mốc thời gian Hôm nay (0h00 - 23h59)
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.set(java.util.Calendar.HOUR_OF_DAY, 0); cal.set(java.util.Calendar.MINUTE, 0); cal.set(java.util.Calendar.SECOND, 0);
                long startOfDay = cal.getTimeInMillis();
                cal.set(java.util.Calendar.HOUR_OF_DAY, 23); cal.set(java.util.Calendar.MINUTE, 59); cal.set(java.util.Calendar.SECOND, 59);
                long endOfDay = cal.getTimeInMillis();

                List<ScheduleItem> combinedList = new ArrayList<>();

                // Lọc Reminder: Chỉ lấy Hôm nay + Chưa bị xóa + Chưa bị Cancel
                for (ReminderHistoryWithTemplate r : reminders) {
                    if (r.template.isActive() &&
                            r.history.getStatus() != com.example.trolyyte.domain.model.ReminderHistory.Status.CANCELLED &&
                            r.history.getScheduledTimeMillis() >= startOfDay &&
                            r.history.getScheduledTimeMillis() <= endOfDay) {
                        combinedList.add(new ScheduleItem(r));
                    }
                }

                for (Appointment a : appointments) {
                    if (a.getTimeMillis() >= startOfDay && a.getTimeMillis() <= endOfDay) {
                        combinedList.add(new ScheduleItem(a));
                    }
                }

                Collections.sort(combinedList, (item1, item2) -> Long.compare(item1.getTimeMillis(), item2.getTimeMillis()));

                if (combinedList.isEmpty()) {
                    _uiState.postValue(new ScheduleUiState.Empty());
                } else {
                    _uiState.postValue(new ScheduleUiState.Success(combinedList));
                }
            } catch (Exception e) {
                _uiState.postValue(new ScheduleUiState.Error(e.getMessage()));
            }
        });
    }

    // --- CÁC HÀM XỬ LÝ SỰ KIỆN CHO NHẮC THUỐC ---
    public void markReminderCompleted(String historyId) { // Nhận ID của History
        executorService.execute(() -> {
            manageReminderUseCase.markAsCompleted(historyId);
            loadSchedule();
        });
    }

    public void deleteReminderTemplate(String templateId) { // Xóa toàn bộ chuỗi theo Template ID
        executorService.execute(() -> {
            manageReminderUseCase.deleteReminderTemplate(templateId);
            loadSchedule();
        });
    }

    public void addOrUpdateReminder(String templateId, String title, String dosage, String instruction,
                                    long time, ReminderTemplate.Type type, ReminderTemplate.RepeatType repeatType, int remindMinutes) {
        executorService.execute(() -> {
            manageReminderUseCase.addOrUpdateReminder(templateId, title, dosage, instruction, time, type, repeatType, remindMinutes);
            loadSchedule();
        });
    }

    // --- CÁC HÀM XỬ LÝ LỊCH KHÁM (Giữ nguyên) ---
    public void markAppointmentCompleted(String id) {
        executorService.execute(() -> {
            manageAppointmentUseCase.markAsCompleted(id);
            loadSchedule();
        });
    }

    public void deleteAppointment(String id) {
        executorService.execute(() -> {
            manageAppointmentUseCase.deleteAppointment(id);
            loadSchedule();
        });
    }

    public void addOrUpdateAppointment(String id, String title, String location, String doctorName, String notes, long timeMillis) {
        executorService.execute(() -> {
            manageAppointmentUseCase.addOrUpdateAppointment(id, title, location, doctorName, timeMillis, notes, Appointment.Status.UPCOMING);
            loadSchedule();
        });
    }
}