package com.example.trolyyte.data.nlu;

import com.example.trolyyte.domain.model.NlpResult;
import com.example.trolyyte.domain.model.NluIntent;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RuleBasedNlpEngine implements NlpEngine {

    @Override
    public void initialize() {
        // Không làm gì vì Rule-based không cần load file nặng
    }

    @Override
    public NlpResult analyze(String text) {
        String normalizedText = text.toLowerCase().trim();
        Map<String, String> entities = new HashMap<>();

        // 1. Kiểm tra Khẩn cấp (Ưu tiên cao nhất)
        if (matches(normalizedText, "cứu", "khẩn cấp", "ngã", "đau tim", "đột quỵ")) {
            return new NlpResult(text, NluIntent.REQUEST_EMERGENCY, entities, 1.0f);
        }

        // 2. Nhóm Đặt/Sửa lịch (Gộp chung CREATE và UPDATE thành SET_OR_UPDATE theo chuẩn mới)
        if (matches(normalizedText, "nhắc", "đặt lịch", "hẹn giờ", "tạo", "đổi giờ", "sửa lại", "dời lịch", "chỉnh lại")) {
            if (normalizedText.contains("thuốc") || normalizedText.contains("uống")) {
                return new NlpResult(text, NluIntent.SET_OR_UPDATE_MEDICATION, entities, 1.0f);
            }
            if (matches(normalizedText, "khám", "bác sĩ", "bệnh viện")) {
                return new NlpResult(text, NluIntent.SET_OR_UPDATE_APPOINTMENT, entities, 1.0f);
            }
        }

        // 3. Nhóm Hủy (Cancel)
        if (matches(normalizedText, "hủy", "xóa", "thôi không")) {
            if (normalizedText.contains("thuốc")) {
                return new NlpResult(text, NluIntent.CANCEL_MEDICATION, entities, 1.0f);
            }
            if (matches(normalizedText, "khám", "bác sĩ", "bệnh viện")) {
                return new NlpResult(text, NluIntent.CANCEL_APPOINTMENT, entities, 1.0f);
            }
        }

        // 4. Tra cứu thông tin thuốc
        if (matches(normalizedText, "là thuốc gì", "công dụng", "tác dụng", "liều dùng")) {
            extractMedicineName(normalizedText, entities);
            return new NlpResult(text, NluIntent.INQUIRE_MEDICINE, entities, 1.0f);
        }

        // 5. Ghi nhận triệu chứng
        if (matches(normalizedText, "tôi bị", "cảm thấy", "đau", "nhức", "mỏi")) {
            entities.put("symptom_desc", text);
            return new NlpResult(text, NluIntent.REPORT_SYMPTOM, entities, 1.0f);
        }

        // 6. Hội thoại & Điều khiển
        if (matches(normalizedText, "đúng", "đồng ý", "ok", "được", "có")) {
            return new NlpResult(text, NluIntent.AFFIRM, entities, 1.0f);
        }
        if (matches(normalizedText, "sai", "không", "chưa")) {
            return new NlpResult(text, NluIntent.DENY, entities, 1.0f);
        }
        if (matches(normalizedText, "nhắc lại", "nói lại", "gì cơ")) {
            return new NlpResult(text, NluIntent.ASK_REPEAT, entities, 1.0f);
        }
        if (matches(normalizedText, "xin chào", "chào bạn", "alo")) {
            return new NlpResult(text, NluIntent.SMALL_TALK, entities, 1.0f);
        }
        if (matches(normalizedText, "dừng", "thoát", "cảm ơn", "im đi")) {
            return new NlpResult(text, NluIntent.STOP_ACTION, entities, 1.0f);
        }

        // Không hiểu
        return new NlpResult(text, NluIntent.UNKNOWN, entities, 0.0f);
    }

    private boolean matches(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) return true;
        }
        return false;
    }

    private void extractMedicineName(String text, Map<String, String> entities) {
        Pattern p = Pattern.compile("thuốc\\s+([a-zA-Z0-9]+)");
        Matcher m = p.matcher(text);
        if (m.find()) {
            entities.put("medicine_name", m.group(1));
        }
    }
}