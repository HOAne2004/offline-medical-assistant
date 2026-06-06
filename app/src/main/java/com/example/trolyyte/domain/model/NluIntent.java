package com.example.trolyyte.domain.model;

public enum NluIntent {

    // 🏥 Core - Y tế cốt lõi
    REQUEST_EMERGENCY,            // Yêu cầu cấp cứu
    REPORT_SYMPTOM,               // Báo cáo triệu chứng
    INQUIRE_MEDICINE,             // Hỏi thông tin thuốc
    SET_OR_UPDATE_MEDICATION,     // Đặt/Sửa lịch uống thuốc
    CANCEL_MEDICATION,            // Hủy lịch uống thuốc
    CHECK_MEDICATION,             // Kiểm tra lịch uống thuốc
    SET_OR_UPDATE_APPOINTMENT,    // Đặt/Sửa lịch khám
    CANCEL_APPOINTMENT,           // Hủy lịch khám
    CHECK_APPOINTMENT,            // Kiểm tra lịch khám

    // 💬 Interaction - Giao tiếp cơ bản
    SMALL_TALK,                   // Trò chuyện phím
    ASK_HELP,                     // Hỏi cách sử dụng app
    ASK_DATE_TIME,                // Hỏi ngày giờ

    // 🎛 Control - Điều khiển
    AFFIRM,                       // Đồng ý / Xác nhận
    DENY,                         // Từ chối / Không đồng ý
    ASK_REPEAT,                   // Yêu cầu nhắc lại
    STOP_ACTION,                  // Dừng hành động / Tắt chuông
    FALLBACK,                     // Không hiểu rõ (dưới 70%)
    OUT_OF_SCOPE,                 // Hỏi ngoài lề (Thời tiết, giá vàng...)

    // ⚠️ Fallback System
    UNKNOWN                       // Không xác định (Dùng khi lỗi mapping hoặc crash)
}