package com.example.trolyyte.presentation.common;

import com.example.trolyyte.domain.dialog.DialogueAction;

import java.util.EnumMap;
import java.util.Map;
import java.util.Random;

public class DefaultResponseTextProvider implements ResponseTextProvider {

    // THAY ĐỔI 1: Thay vì Map với 1 String, ta map với một mảng String (String[])
    private final Map<DialogueAction, String[]> responseMap = new EnumMap<>(DialogueAction.class);

    // THAY ĐỔI 2: Khởi tạo bộ sinh số ngẫu nhiên
    private final Random random = new Random();

    public DefaultResponseTextProvider() {
        init();
    }

    private void init() {
        /* ===== Nhóm Hỏi thông tin (Asking) ===== */
        responseMap.put(DialogueAction.ASK_MEDICINE_NAME, new String[]{
                "Bác cho cháu biết tên thuốc được không ạ?",
                "Dạ, tên thuốc bác cần uống là gì thế ạ?",
                "Bác đọc giúp cháu tên loại thuốc nhé.",
                "Bác uống thuốc gì, bác cho cháu biết tên với ạ."
        });

        responseMap.put(DialogueAction.ASK_TIME, new String[]{
                "Bác muốn thực hiện vào lúc mấy giờ ạ?",
                "Dạ, thời gian cụ thể là mấy giờ thưa bác?",
                "Bác cho cháu xin giờ giấc cụ thể nhé.",
                "Cháu nên báo thức cho bác vào lúc mấy giờ ạ?"
        });

        responseMap.put(DialogueAction.ASK_LOCATION, new String[]{
                "Bác sẽ khám ở bệnh viện hay phòng khám nào ạ?",
                "Dạ, địa điểm khám bệnh của bác ở đâu thế ạ?",
                "Bác cho cháu xin tên bệnh viện nhé."
        });

        responseMap.put(DialogueAction.ASK_CONFIRM_EMERGENCY, new String[]{
                "Tình huống này có cần gọi cho người nhà ngay không bác?",
                "Bác thấy mệt lắm không, cháu gọi cấp cứu luôn nhé?",
                "Bác cần cháu gọi điện thoại cho người thân ngay không ạ?"
        });

        /* ===== Nhóm Xác nhận (Confirmation) ===== */
        responseMap.put(DialogueAction.CONFIRM_MEDICINE_REMINDER_CREATED, new String[]{
                "Cháu đã lưu lịch nhắc nhở. Tới giờ cháu sẽ báo bác nhé.",
                "Dạ vâng, cháu đã ghi nhớ lịch uống thuốc của bác rồi ạ.",
                "Xong rồi bác nhé, cháu sẽ nhắc bác uống thuốc đúng giờ.",
                "Dạ cháu đã cài báo thức thuốc thành công rồi ạ."
        });

        responseMap.put(DialogueAction.CONFIRM_APPOINTMENT_CREATED, new String[]{
                "Cháu đã lưu lịch khám bệnh cho bác rồi ạ.",
                "Dạ, lịch đi khám của bác đã được ghi nhận.",
                "Cháu đã cài lịch nhắc đi khám cho bác xong rồi nhé."
        });

        /* ===== Nhóm Luồng chung (Flow) ===== */
        responseMap.put(DialogueAction.COMPLETE_DIALOGUE, new String[]{
                "Vâng, cháu đã hiểu.",
                "Dạ vâng ạ.",
                "Dạ, cháu rõ rồi thưa bác.",
                "Cháu ghi nhận rồi bác nhé."
        });

        responseMap.put(DialogueAction.UNKNOWN_COMMAND, new String[]{
                "Cháu chưa hiểu ý bác. Bác nói lại giúp cháu nhé.",
                "Dạ cháu chưa nghe rõ, bác lặp lại được không ạ?",
                "Âm thanh hơi bị nhiễu, bác nói lại giúp cháu với nhé.",
                "Cháu chưa bắt được thông tin, bác nói lại nha."
        });
    }

    @Override
    public String getText(String keyName) {
        try {
            DialogueAction action = DialogueAction.valueOf(keyName);
            String[] responses = responseMap.get(action);

            // THAY ĐỔI 3: Thuật toán bốc thăm ngẫu nhiên 1 câu trả lời
            if (responses != null && responses.length > 0) {
                int randomIndex = random.nextInt(responses.length);
                return responses[randomIndex];
            }
        } catch (Exception e) {
            // Không làm gì cả, rớt xuống dòng Fallback bên dưới
        }

        // Fallback: Khi luồng bị lỗi hoặc hệ thống chỉ muốn đáp "Vâng ạ" ngắn gọn
        String[] fallbacks = {
                "Vâng ạ.",
                "Dạ vâng.",
                "Cháu nghe đây ạ."
        };
        return fallbacks[random.nextInt(fallbacks.length)];
    }
}