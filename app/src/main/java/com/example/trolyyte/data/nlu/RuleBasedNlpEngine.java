package com.example.trolyyte.data.nlu;

import com.example.trolyyte.domain.model.NlpResult;
import com.example.trolyyte.domain.model.NluIntent;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RuleBasedNlpEngine implements NlpEngine {

    // Thêm hàm initialize rỗng nếu interface yêu cầu (default method trong interface thì không cần)
    @Override
    public void initialize() {
        // Không làm gì vì Rule-based không cần load file nặng
    }

    @Override

    /**
     * Hàm chính: Phân tích câu nói thành Intent và Entities
     */
    public NlpResult analyze(String text) {
        String normalizedText = text.toLowerCase().trim();
        Map<String, String> entities = new HashMap<>();

        // 1. Kiểm tra Khẩn cấp (Ưu tiên cao nhất)
        if (matches(normalizedText, "cứu", "khẩn cấp", "ngã", "đau tim", "đột quỵ")) {
            return new NlpResult(text, NluIntent.EMERGENCY_HELP, entities, 1.0f);
        }

        // 2. Nhóm Tạo mới (Create) - Thuốc & Lịch khám
        if (matches(normalizedText, "nhắc", "đặt lịch", "hẹn giờ", "tạo")) {
            // Ưu tiên thuốc
            if (normalizedText.contains("thuốc") || normalizedText.contains("uống")) {
                // extractTime(normalizedText, entities); // (Sẽ làm sau)
                // extractMedicineName(normalizedText, entities); // Nên gọi luôn ở đây nếu có logic
                return new NlpResult(text, NluIntent.CREATE_MEDICINE_REMINDER, entities, 1.0f);
            }
            // Lịch khám
            if (matches(normalizedText, "khám", "bác sĩ", "bệnh viện")) {
                // extractTime(normalizedText, entities);
                return new NlpResult(text, NluIntent.CREATE_APPOINTMENT_REMINDER, entities, 1.0f);
            }
        }

        // 3. Nhóm Cập nhật (Update) - Đã bổ sung Lịch khám
        if (matches(normalizedText, "đổi giờ", "sửa lại", "dời lịch", "chỉnh lại")) {
            // Cập nhật thuốc
            if (normalizedText.contains("thuốc")) {
                return new NlpResult(text, NluIntent.UPDATE_MEDICINE_REMINDER, entities, 1.0f);
            }
            // Cập nhật lịch khám (BỔ SUNG)
            if (matches(normalizedText, "khám", "bác sĩ", "bệnh viện")) {
                return new NlpResult(text, NluIntent.UPDATE_APPOINTMENT_REMINDER, entities, 1.0f);
            }
        }

        // 4. Nhóm Hủy (Cancel) - Đã bổ sung Lịch khám
        if (matches(normalizedText, "hủy", "xóa", "thôi không")) {
            // Hủy thuốc
            if (normalizedText.contains("thuốc")) {
                return new NlpResult(text, NluIntent.CANCEL_MEDICINE_REMINDER, entities, 1.0f);
            }
            // Hủy lịch khám (BỔ SUNG)
            if (matches(normalizedText, "khám", "bác sĩ", "bệnh viện")) {
                return new NlpResult(text, NluIntent.CANCEL_APPOINTMENT_REMINDER, entities, 1.0f);
            }
        }

        // 5. Tra cứu thông tin
        if (matches(normalizedText, "là thuốc gì", "công dụng", "tác dụng", "liều dùng")) {
            extractMedicineName(normalizedText, entities);
            return new NlpResult(text, NluIntent.QUERY_MEDICINE_INFO, entities, 1.0f);
        }

        // 6. Ghi nhận triệu chứng
        if (matches(normalizedText, "tôi bị", "cảm thấy", "đau", "nhức", "mỏi")) {
            entities.put("symptom_desc", text);
            return new NlpResult(text, NluIntent.RECORD_SYMPTOM, entities, 1.0f);
        }

        // 7. Hội thoại cơ bản
        if (matches(normalizedText, "đúng", "đồng ý", "ok", "được", "có")) {
            return new NlpResult(text, NluIntent.CONFIRM_YES, entities, 1.0f);
        }
        if (matches(normalizedText, "sai", "không", "hủy", "chưa")) {
            return new NlpResult(text, NluIntent.CONFIRM_NO, entities, 1.0f);
        }
        if (matches(normalizedText, "nhắc lại", "nói lại", "gì cơ")) {
            return new NlpResult(text, NluIntent.REPEAT, entities, 1.0f);
        }
        if (matches(normalizedText, "xin chào", "chào bạn", "alo")) {
            return new NlpResult(text, NluIntent.GREETING, entities, 1.0f);
        }
        if (matches(normalizedText, "dừng", "thoát", "cảm ơn")) {
            return new NlpResult(text, NluIntent.UNKNOWN, entities, 1.0f); // Hoặc STOP_INTERACTION nếu đã thêm vào enum
        }

        // Không hiểu
        return new NlpResult(text, NluIntent.UNKNOWN, entities, 0.0f);
    }

    // Hàm tiện ích kiểm tra xem text có chứa bất kỳ từ khóa nào trong list không
    private boolean matches(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) return true;
        }
        return false;
    }

    // Ví dụ hàm trích xuất entity cơ bản
    private void extractMedicineName(String text, Map<String, String> entities) {
        // Logic tìm tên thuốc: Ví dụ lấy từ sau chữ "thuốc"
        // "công dụng của thuốc paracetamol là gì" -> paracetamol
        Pattern p = Pattern.compile("thuốc\\s+([a-zA-Z0-9]+)");
        Matcher m = p.matcher(text);
        if (m.find()) {
            entities.put("medicine_name", m.group(1));
        }
    }
}