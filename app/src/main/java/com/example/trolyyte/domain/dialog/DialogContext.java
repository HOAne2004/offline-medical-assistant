package com.example.trolyyte.domain.dialog;

import com.example.trolyyte.domain.model.DialogState;
import java.util.HashMap;
import java.util.Map;

public class DialogContext {

    private DialogState currentState = DialogState.IDLE;
    private String currentIntent; // Lưu lại xem đang ở luồng Đặt lịch hay Uống thuốc

    // --- Thông tin Thuốc & Sinh hoạt ---
    private String medicineName;
    private String routineName;
    private String reminderTime;

    // --- Thông tin Lịch khám ---
    private String appointmentReason;
    private String appointmentLocation;
    private String appointmentDate;

    // --- Trạng thái Khẩn cấp ---
    private boolean isWaitingEmergencyConfirm = false;

    // Getters & Setters
    public String getCurrentIntent() { return currentIntent; }
    public void setCurrentIntent(String currentIntent) { this.currentIntent = currentIntent; }

    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }

    public String getRoutineName() { return routineName; }
    public void setRoutineName(String routineName) { this.routineName = routineName; }

    public String getReminderTime() { return reminderTime; }
    public void setReminderTime(String reminderTime) { this.reminderTime = reminderTime; }

    public String getAppointmentReason() { return appointmentReason; }
    public void setAppointmentReason(String appointmentReason) { this.appointmentReason = appointmentReason; }

    public String getAppointmentLocation() { return appointmentLocation; }
    public void setAppointmentLocation(String appointmentLocation) { this.appointmentLocation = appointmentLocation; }

    public String getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(String appointmentDate) { this.appointmentDate = appointmentDate; }

    public boolean isWaitingEmergencyConfirm() { return isWaitingEmergencyConfirm; }
    public void setWaitingEmergencyConfirm(boolean waitingEmergencyConfirm) { this.isWaitingEmergencyConfirm = waitingEmergencyConfirm; }

    // Gộp tất cả thông tin đã nhớ thành Entity Map để đẩy qua Form
    public Map<String, String> getFilledEntities() {
        Map<String, String> map = new HashMap<>();
        if (medicineName != null) map.put("medicine_name", medicineName);
        if (routineName != null) map.put("routine_name", routineName);
        if (reminderTime != null) map.put("time", reminderTime);
        if (appointmentReason != null) map.put("appointment_name", appointmentReason);
        if (appointmentLocation != null) map.put("location", appointmentLocation);
        if (appointmentDate != null) map.put("date", appointmentDate);
        return map;
    }

    public void reset() {
        currentState = DialogState.IDLE;
        currentIntent = null;
        medicineName = null;
        routineName = null;
        reminderTime = null;
        appointmentReason = null;
        appointmentLocation = null;
        appointmentDate = null;
        isWaitingEmergencyConfirm = false;
    }
    public DialogState getCurrentState() {
        return currentState;
    }

    public void setCurrentState(DialogState currentState) {
        this.currentState = currentState;
    }
}