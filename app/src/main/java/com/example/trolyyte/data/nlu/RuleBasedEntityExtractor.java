package com.example.trolyyte.data.nlu;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RuleBasedEntityExtractor {

    public static Map<String, String> extract(String text) {
        Map<String, String> entities = new HashMap<>();
        String lowerText = text.toLowerCase().trim();

        // ==========================================
        // 1. NHÓM THỜI GIAN & CHU KỲ (TIME & REPEAT)
        // ==========================================
        Pattern timePattern = Pattern.compile("(\\d{1,2})\\s*(giờ|h)\\s*(\\d{1,2})?");
        Matcher timeMatcher = timePattern.matcher(lowerText);
        if (timeMatcher.find()) {
            String hour = timeMatcher.group(1);
            String minute = timeMatcher.group(3) != null ? timeMatcher.group(3) : "00";
            entities.put("time", String.format("%02d:%02d", Integer.parseInt(hour), Integer.parseInt(minute)));
        }

        // Ngày tháng
        if (lowerText.contains("ngày mai")) entities.put("date", "ngày mai");
        else if (lowerText.contains("hôm nay")) entities.put("date", "hôm nay");

        // Chu kỳ lặp
        if (lowerText.contains("mỗi ngày") || lowerText.contains("hằng ngày")) {
            entities.put("repeat", "DAILY");
        }

        // ==========================================
        // 2. NHÓM THUỐC (MEDICATION)
        // ==========================================
        // Tên thuốc: Bắt từ nằm ngay sau chữ "thuốc" hoặc "uống"
        Pattern medPattern = Pattern.compile("(?:thuốc|uống)\\s+([a-zA-ZÀ-ỹ0-9]+(?:\\s+[a-zA-ZÀ-ỹ0-9]+)*?)(?=\\s+(?:lúc|vào|ngày|sáng|trưa|chiều|tối|mỗi|hằng|viên|gói|ml|$))");
        Matcher medMatcher = medPattern.matcher(lowerText);
        if (medMatcher.find()) {
            String medName = medMatcher.group(1).trim();
            if (!medName.matches("gì|này|xong|đi")) entities.put("medicine_name", medName);
        }

        // Liều lượng (Dosage): VD: 2 viên, 1 gói, 10 ml
        Pattern dosagePattern = Pattern.compile("(\\d+|nửa|một nửa)\\s+(viên|gói|ống|ml|mg)");
        Matcher dosageMatcher = dosagePattern.matcher(lowerText);
        if (dosageMatcher.find()) {
            entities.put("dosage", dosageMatcher.group(0));
        }

        // Chỉ định (Instruction): trước/sau ăn
        if (lowerText.contains("sau ăn") || lowerText.contains("sau khi ăn")) entities.put("instruction", "Uống sau ăn");
        if (lowerText.contains("trước ăn") || lowerText.contains("trước khi ăn")) entities.put("instruction", "Uống trước ăn");

        // ==========================================
        // 3. NHÓM LỊCH KHÁM (APPOINTMENT)
        // ==========================================
        // Tên bác sĩ
        Pattern doctorPattern = Pattern.compile("bác sĩ\\s+([a-zA-ZÀ-ỹ\\s]+)(?=\\s+(?:lúc|vào|ở|tại|$))");
        Matcher doctorMatcher = doctorPattern.matcher(lowerText);
        if (doctorMatcher.find()) entities.put("doctor", "Bác sĩ " + doctorMatcher.group(1).trim());

        // Tên bệnh viện / Địa điểm
        Pattern hospitalPattern = Pattern.compile("(?:bệnh viện|phòng khám|trạm y tế)\\s+([a-zA-ZÀ-ỹ\\s]+)(?=\\s+(?:lúc|vào|gặp|$))");
        Matcher hospitalMatcher = hospitalPattern.matcher(lowerText);
        if (hospitalMatcher.find()) entities.put("hospital", hospitalMatcher.group(0).trim());

        // ==========================================
        // 4. NHÓM TRIỆU CHỨNG & CẤP CỨU (SYMPTOM / EMERGENCY)
        // ==========================================
        String[] symptoms = {"đau đầu", "chóng mặt", "khó thở", "đau ngực", "sốt", "buồn nôn", "ho", "mệt mỏi"};
        for (String s : symptoms) {
            if (lowerText.contains(s)) {
                entities.put("symptom", s);
                break; // Tìm thấy 1 triệu chứng chính là dừng
            }
        }

        String[] emergencies = {"đột quỵ", "bất tỉnh", "ngã", "chảy máu", "tai nạn"};
        for (String e : emergencies) {
            if (lowerText.contains(e)) {
                entities.put("emergency_type", e);
                break;
            }
        }

        return entities;
    }
}