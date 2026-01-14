package com.example.trolyyte.domain.model;

public enum DialogState {

    // Common
    IDLE, // Rảnh rỗi, chờ lệnh mới
    COMPLETED,

    // Medicine reminder
    COLLECTING_MEDICINE_INFO,
    CONFIRMING_MEDICINE_REMINDER,

    // Appointment reminder
    COLLECTING_APPOINTMENT_INFO,
    CONFIRMING_APPOINTMENT,

    // Symptom recording
    COLLECTING_SYMPTOM_INFO,
    CONFIRMING_SYMPTOM_RECORD,

    // Emergency
    EMERGENCY_CONFIRMATION
}
