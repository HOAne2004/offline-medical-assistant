package com.example.trolyyte.data.utils;

public class NluTextNormalizer {
    /**
     * Chuẩn hóa văn bản dành riêng cho NLU (Đầu vào từ ASR)
     * Chuyển đổi số dạng chữ thành số dạng nguyên để Regex dễ bắt Entity.
     */
    public static String normalizeForNlu(String input) {
        if (input == null) return "";
        String text = input.toLowerCase().trim();

        // Chuyển đổi số đếm cơ bản (1-10) và vài số chẵn
        String[][] numberMap = {
                {"một", "1"}, {"hai", "2"}, {"ba", "3"}, {"bốn", "4"}, {"năm", "5"},
                {"sáu", "6"}, {"bảy", "7"}, {"tám", "8"}, {"chín", "9"}, {"mười", "10"},
                {"mười lăm", "15"}, {"hai mươi", "20"}, {"ba mươi", "30"}, {"nửa", "30"}
        };

        for (String[] pair : numberMap) {
            text = text.replaceAll("\\b" + pair[0] + "\\b", pair[1]);
        }

        // Sửa vài lỗi ASR kinh điển
        text = text.replaceAll("rưỡi", "30"); // 8 giờ rưỡi -> 8 giờ 30

        return text;
    }
}