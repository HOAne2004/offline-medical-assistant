package com.example.trolyyte.domain.usecase;

import com.example.trolyyte.domain.model.Appointment;
import com.example.trolyyte.domain.repository.AppointmentRepository;
import java.util.List;

public class GetAppointmentsUseCase {
    private final AppointmentRepository repository;

    public GetAppointmentsUseCase(AppointmentRepository repository) {
        this.repository = repository;
    }

    public List<Appointment> execute() {
        return repository.getAllAppointments();
    }
}