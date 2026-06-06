package com.example.trolyyte.data.repository;

import com.example.trolyyte.data.local.dao.AppointmentDao;
import com.example.trolyyte.domain.model.Appointment;
import com.example.trolyyte.domain.repository.AppointmentRepository;

import java.util.List;

public class AppointmentRepositoryImpl implements AppointmentRepository {
    private final AppointmentDao appointmentDao;

    // TODO: Bổ sung AppointmentAlarmScheduler vào constructor sau nếu muốn đặt lịch thông báo (giống Nhắc thuốc)

    public AppointmentRepositoryImpl(AppointmentDao appointmentDao) {
        this.appointmentDao = appointmentDao;
    }

    @Override
    public List<Appointment> getAllAppointments() {
        return appointmentDao.getAllAppointments();
    }

    @Override
    public Appointment getAppointmentById(String id){
        return appointmentDao.getAppointmentById(id);
    }

    @Override
    public void addAppointment(Appointment appointment) {
        appointmentDao.insert(appointment);
        // TODO: scheduler.schedule(appointment);
    }

    @Override
    public void updateAppointment(Appointment appointment) {
        appointmentDao.update(appointment);
        // TODO: scheduler.schedule(appointment);
    }

    @Override
    public void deleteAppointment(String id) {
        Appointment appointment = appointmentDao.getAppointmentById(id);
        if (appointment != null) {
            appointmentDao.delete(appointment);
            // TODO: scheduler.cancel(id);
        }
    }
}