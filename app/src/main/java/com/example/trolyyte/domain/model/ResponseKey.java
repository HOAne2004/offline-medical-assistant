package com.example.trolyyte.domain.model;

public enum ResponseKey {

    /* ===== Asking for missing information ===== */
    ASK_MEDICINE_NAME,
    ASK_TIME,
    ASK_DOSAGE,
    ASK_FREQUENCY,
    ASK_DATE,
    ASK_LOCATION,
    ASK_SYMPTOM,
    ASK_SEVERITY,
    ASK_CONFIRMATION,
    ASK_CONFIRM_EMERGENCY,

    /* ===== Confirmation / Success ===== */
    MEDICINE_REMINDER_CREATED,
    APPOINTMENT_CREATED,
    SYMPTOM_RECORDED,

    /* ===== Information ===== */
    MEDICINE_INFO,
    SYMPTOM_INFO,

    /* ===== Emergency ===== */
    EMERGENCY_TRIGGERED,

    /* ===== System / Flow ===== */
    GENERIC_CONFIRM,
    END_CONVERSATION,
    UNKNOWN_COMMAND,
    NEED_REPEAT
}
