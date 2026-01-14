package com.example.trolyyte.domain.model;

public enum NluIntent {

    // Nhắc uống thuốc
    CREATE_MEDICINE_REMINDER,
    UPDATE_MEDICINE_REMINDER,
    CANCEL_MEDICINE_REMINDER,

    // Lịch khám
    CREATE_APPOINTMENT_REMINDER,
    UPDATE_APPOINTMENT_REMINDER,
    CANCEL_APPOINTMENT_REMINDER,

    // Tra cứu
    QUERY_MEDICINE_INFO,
    QUERY_SYMPTOM_INFO,

    // Ghi nhận sức khỏe
    RECORD_SYMPTOM,

    // Khẩn cấp
    EMERGENCY_HELP,

    // Hội thoại
    CONFIRM_YES,
    CONFIRM_NO,
    REPEAT,
    GREETING,
    STOP_INTERACTION,

    // Không xác định
    UNKNOWN
}
