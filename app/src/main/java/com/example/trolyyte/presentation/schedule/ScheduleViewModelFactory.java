package com.example.trolyyte.presentation.schedule;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.trolyyte.domain.usecase.GetAppointmentsUseCase;
import com.example.trolyyte.domain.usecase.GetRemindersUseCase;
import com.example.trolyyte.domain.usecase.ManageAppointmentUseCase;
import com.example.trolyyte.domain.usecase.ManageReminderUseCase;

public class ScheduleViewModelFactory implements ViewModelProvider.Factory {
    private final GetRemindersUseCase getRemindersUseCase;
    private final GetAppointmentsUseCase getAppointmentsUseCase;
    private final ManageReminderUseCase manageReminderUseCase;
    private final ManageAppointmentUseCase manageAppointmentUseCase;

    public ScheduleViewModelFactory(GetRemindersUseCase getRemindersUseCase,
                                    GetAppointmentsUseCase getAppointmentsUseCase,
                                    ManageReminderUseCase manageReminderUseCase,
                                    ManageAppointmentUseCase manageAppointmentUseCase) {
        this.getRemindersUseCase = getRemindersUseCase;
        this.getAppointmentsUseCase = getAppointmentsUseCase;
        this.manageReminderUseCase = manageReminderUseCase;
        this.manageAppointmentUseCase = manageAppointmentUseCase;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(ScheduleViewModel.class)) {
            return (T) new ScheduleViewModel(getRemindersUseCase, getAppointmentsUseCase, manageReminderUseCase, manageAppointmentUseCase);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}