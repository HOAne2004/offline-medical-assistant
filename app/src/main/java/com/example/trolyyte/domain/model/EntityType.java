package com.example.trolyyte.domain.model;

public enum EntityType {

    // Time & schedule
    TIME,
    DATE,
    DATETIME,
    REMIND_BEFORE,
    FREQUENCY,

    // Medicine
    MEDICINE_NAME,
    DOSAGE,
    MEDICINE_FORM,

    // Appointment
    LOCATION,
    DOCTOR_NAME,
    DEPARTMENT,

    // Symptom
    SYMPTOM_NAME,
    SEVERITY,
    DURATION,

    // Dialogue control
    CONFIRMATION
}
