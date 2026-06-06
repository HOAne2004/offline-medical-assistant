package com.example.trolyyte.domain.usecase;

import com.example.trolyyte.domain.model.Appointment;
import com.example.trolyyte.domain.repository.AppointmentRepository;

public class ManageAppointmentUseCase {
    private final AppointmentRepository repository;

    public ManageAppointmentUseCase(AppointmentRepository repository) {
        this.repository = repository;
    }

    public void addOrUpdateAppointment(String id, String title, String location, String doctorName, long timeMillis, String notes, Appointment.Status status) {
        // Nếu tạo mới
        if (id == null || id.isEmpty()) {
            Appointment newAppointment = new Appointment(null, title, timeMillis);
            newAppointment.setLocation(location);
            newAppointment.setDoctorName(doctorName);
            newAppointment.setNotes(notes);

            repository.addAppointment(newAppointment);
        } else {
            // Nếu cập nhật: Cần lấy Object cũ lên (tùy thuộc vào cách bạn truyền từ UI)
            // Để đơn giản, ta tái tạo Object và thiết lập thời gian cập nhật
            Appointment updatedAppointment = new Appointment(id, title, timeMillis);
            updatedAppointment.setLocation(location);
            updatedAppointment.setDoctorName(doctorName);
            updatedAppointment.setNotes(notes);

            // Nếu UI có truyền status thì cập nhật, không thì giữ mặc định
            if (status != null) {
                updatedAppointment.setStatus(status);
            }

            // Cập nhật lại thời gian sửa đổi cuối cùng
            updatedAppointment.setUpdatedAt(System.currentTimeMillis());

            repository.updateAppointment(updatedAppointment);
        }
    }

    public void cancelAppointment(String id) {

        Appointment appointment = repository.getAppointmentById(id);
        if(appointment != null) {
            appointment.setStatus(Appointment.Status.CANCELLED);
            appointment.setUpdatedAt(System.currentTimeMillis());
            repository.updateAppointment(appointment);
        }
    }

    public void deleteAppointment(String id) {
        repository.deleteAppointment(id);
    }

    public void markAsCompleted(String id) {
        Appointment appointment = repository.getAppointmentById(id);
        if (appointment != null) {
            appointment.setStatus(Appointment.Status.COMPLETED);
            appointment.setUpdatedAt(System.currentTimeMillis());
            repository.updateAppointment(appointment);
        }
    }
}