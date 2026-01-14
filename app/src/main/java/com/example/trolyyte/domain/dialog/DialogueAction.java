package com.example.trolyyte.domain.dialog;

public enum DialogueAction {

    // ===== Request information =====
    ASK_MEDICINE_NAME,
    ASK_DOSAGE,
    ASK_TIME,
    ASK_FREQUENCY,
    ASK_DATE,
    ASK_LOCATION,
    ASK_SYMPTOM,
    ASK_SEVERITY,
    ASK_CONFIRMATION,
    ASK_CONFIRM_EMERGENCY,

    // ===== Confirmation / Completion =====
    CONFIRM_MEDICINE_REMINDER_CREATED,
    CONFIRM_APPOINTMENT_CREATED,
    CONFIRM_SYMPTOM_RECORDED,
    COMPLETE_DIALOGUE,

    // ===== Information =====
    SHOW_MEDICINE_INFO,
    SHOW_SYMPTOM_INFO,

    // ===== Emergency =====
    TRIGGER_EMERGENCY,

    // ===== Dialogue control =====
    UNKNOWN_COMMAND,
    NEED_REPEAT,
    NO_ACTION
}
