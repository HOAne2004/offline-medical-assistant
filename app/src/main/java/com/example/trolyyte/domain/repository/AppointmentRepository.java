package com.example.trolyyte.domain.repository;

import com.example.trolyyte.domain.model.Appointment;
import java.util.List;

public interface AppointmentRepository {
    List<Appointment> getAllAppointments();
    Appointment getAppointmentById(String id);
    void addAppointment(Appointment appointment);
    void updateAppointment(Appointment appointment);
    void deleteAppointment(String id);
}