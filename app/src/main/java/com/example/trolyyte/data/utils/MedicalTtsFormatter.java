package com.example.trolyyte.data.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MedicalTtsFormatter {

    /**
     * Đường ống xử lý dữ liệu động cho TTS (Dynamic Regex Pipeline)
     */
    public static String formatForTts(String input) {
        if (input == null || input.isEmpty()) return "";

        String text = input;

        // THỨ TỰ THỰC THI RẤT QUAN TRỌNG:
        text = formatPhoneNumber(text); // Số điện thoại (xử lý trước để không bị nhầm với số lượng)
        text = formatDate(text);        // Ngày tháng (Xử lý dấu / trước)
        text = formatBloodPressure(text); // Huyết áp (Xử lý dấu / còn lại)
        text = formatTime(text);        // Giờ phút (Xử lý dấu : và chữ h)
        text = formatFractions(text);   // Phân số (1/2, 1/4...)
        text = formatMedicalUnits(text); // Đơn vị đo lường y tế

        return text;
    }

    /**
     * 1. Số điện thoại: 0987654321 -> 0 9 8 7 6 5 4 3 2 1
     */
    private static String formatPhoneNumber(String text) {
        // Tìm chuỗi 10 hoặc 11 chữ số bắt đầu bằng số 0
        Pattern p = Pattern.compile("\\b(0\\d{9,10})\\b");
        Matcher m = p.matcher(text);
        StringBuffer sb = new StringBuffer();

        while (m.find()) {
            String phone = m.group(1);
            StringBuilder spacedPhone = new StringBuilder();
            for (char c : phone.toCharArray()) {
                spacedPhone.append(c).append(" ");
            }
            m.appendReplacement(sb, spacedPhone.toString().trim());
        }
        m.appendTail(sb);
        return sb.toString();
    }

    /**
     * 2. Ngày tháng: 25/12/2026 -> ngày 25 tháng 12 năm 2026
     */
    private static String formatDate(String text) {
        Pattern p = Pattern.compile("\\b(\\d{1,2})\\s*[/-]\\s*(\\d{1,2})\\s*[/-]\\s*(\\d{4})\\b");
        Matcher m = p.matcher(text);
        StringBuffer sb = new StringBuffer();

        while (m.find()) {
            String replacement = "ngày " + m.group(1) + " tháng " + m.group(2) + " năm " + m.group(3);
            m.appendReplacement(sb, replacement);
        }
        m.appendTail(sb);
        return sb.toString();
    }

    /**
     * 3. Chỉ số huyết áp: 120/80 -> 120 trên 80
     */
    private static String formatBloodPressure(String text) {
        // Tìm 2-3 chữ số, dấu /, 2-3 chữ số
        Pattern p = Pattern.compile("\\b(\\d{2,3})\\s*/\\s*(\\d{2,3})\\b");
        Matcher m = p.matcher(text);
        StringBuffer sb = new StringBuffer();

        while (m.find()) {
            String replacement = m.group(1) + " trên " + m.group(2);
            m.appendReplacement(sb, replacement);
        }
        m.appendTail(sb);
        return sb.toString();
    }

    /**
     * 4. Giờ phút: 8h30 hoặc 08:30 -> 8 giờ 30 phút
     */
    private static String formatTime(String text) {
        // Xử lý định dạng HH:mm (VD: 08:30, 14:15)
        text = text.replaceAll("\\b(\\d{1,2}):(\\d{2})\\b", "$1 giờ $2 phút");

        // Xử lý định dạng 8h, 8h30
        Pattern p = Pattern.compile("(?i)\\b(\\d{1,2})\\s*h\\s*(\\d{1,2})?\\b");
        Matcher m = p.matcher(text);
        StringBuffer sb = new StringBuffer();

        while (m.find()) {
            String hour = m.group(1);
            String minute = m.group(2);

            String replacement = hour + " giờ";
            if (minute != null && !minute.isEmpty() && !minute.equals("00")) {
                replacement += " " + minute + " phút";
            }
            m.appendReplacement(sb, replacement);
        }
        m.appendTail(sb);

        // Dọn dẹp nếu có "00 phút"
        text = sb.toString().replaceAll(" 00 phút", "");
        return text;
    }

    /**
     * 5. Phân số y tế phổ biến
     */
    private static String formatFractions(String text) {
        text = text.replaceAll("\\b1/4\\b", "một phần tư");
        text = text.replaceAll("\\b3/4\\b", "ba phần tư");
        text = text.replaceAll("\\b2/3\\b", "hai phần ba");
        text = text.replaceAll("\\b1/2\\b", "nửa");
        text = text.replaceAll("\\b0[.,]5\\b", "nửa");
        text = text.replaceAll("\\b1[.,]5\\b", "một phẩy năm");
        return text;
    }

    /**
     * 6. Đơn vị đo lường y tế chuyên sâu
     */
    private static String formatMedicalUnits(String text) {
        // Các đơn vị chuẩn
        text = text.replaceAll("(?i)\\bmmhg\\b", "mi li mét thủy ngân");
        text = text.replaceAll("(?i)\\bbpm\\b", "nhịp mỗi phút");
        text = text.replaceAll("(?i)\\biu\\b", "đơn vị quốc tế");
        text = text.replaceAll("(?i)\\bmcg\\b", "mi crô gam");
        text = text.replaceAll("(?i)\\bmg\\b", "mi li gam");
        text = text.replaceAll("(?i)\\bml\\b", "mi li lít");
        text = text.replaceAll("(?i)\\bkg\\b", "ki lô gam");

        // Đơn vị "g" (gam) -> Phải đứng ngay sau số để tránh nhầm lẫn chữ cái
        text = text.replaceAll("(?i)(?<=\\d)\\s*g\\b", " gam");

        // Độ C
        text = text.replaceAll("(?i)°C", "độ xê");
        text = text.replaceAll("(?i)\\bdo c\\b", "độ xê");
        text = text.replaceAll("(?i)\\bđộ c\\b", "độ xê");

        return text;
    }
}