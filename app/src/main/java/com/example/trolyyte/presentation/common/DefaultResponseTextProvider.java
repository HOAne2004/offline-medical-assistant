package com.example.trolyyte.presentation.common;

import com.example.trolyyte.domain.model.ResponseKey;

import java.util.EnumMap;
import java.util.Map;

public class DefaultResponseTextProvider implements ResponseTextProvider {

    private final Map<ResponseKey, String> responseMap = new EnumMap<>(ResponseKey.class);

    public DefaultResponseTextProvider() {
        init();
    }

    private void init() {

        /* ===== Asking ===== */

        responseMap.put(
                ResponseKey.ASK_MEDICINE_NAME,
                "Bác cho cháu biết tên thuốc được không ạ."
        );

        responseMap.put(
                ResponseKey.ASK_TIME,
                "Bác cho cháu biết thời gian nhé."
        );

        responseMap.put(
                ResponseKey.ASK_DOSAGE,
                "Bác cho cháu biết liều lượng cần dùng."
        );

        responseMap.put(
                ResponseKey.ASK_LOCATION,
                "Bác cho cháu biết địa điểm nhé."
        );

        responseMap.put(
                ResponseKey.ASK_CONFIRMATION,
                "Bác có xác nhận lại thông tin này không ạ."
        );

        responseMap.put(
                ResponseKey.ASK_CONFIRM_EMERGENCY,
                "Tình huống này có cần gọi khẩn cấp không bác."
        );

        /* ===== Confirmation ===== */

        responseMap.put(
                ResponseKey.MEDICINE_REMINDER_CREATED,
                "Cháu đã ghi nhớ và sẽ nhắc bác đúng giờ."
        );

        responseMap.put(
                ResponseKey.APPOINTMENT_CREATED,
                "Cháu đã lưu lịch khám cho bác."
        );

        responseMap.put(
                ResponseKey.SYMPTOM_RECORDED,
                "Cháu đã ghi nhận tình trạng sức khỏe của bác."
        );

        /* ===== Information ===== */

        responseMap.put(
                ResponseKey.MEDICINE_INFO,
                "Cháu sẽ cung cấp thông tin về thuốc này cho bác."
        );

        responseMap.put(
                ResponseKey.SYMPTOM_INFO,
                "Cháu sẽ giải thích triệu chứng này cho bác."
        );

        /* ===== Emergency ===== */

        responseMap.put(
                ResponseKey.EMERGENCY_TRIGGERED,
                "Bác giữ bình tĩnh. Cháu đang gọi số khẩn cấp."
        );

        /* ===== Flow ===== */

        responseMap.put(
                ResponseKey.GENERIC_CONFIRM,
                "Vâng, cháu đã hiểu."
        );

        responseMap.put(
                ResponseKey.END_CONVERSATION,
                "Khi nào cần, bác cứ gọi cháu nhé."
        );

        responseMap.put(
                ResponseKey.UNKNOWN_COMMAND,
                "Cháu chưa hiểu ý bác. Bác nói lại giúp cháu nhé."
        );

        responseMap.put(
                ResponseKey.NEED_REPEAT,
                "Bác nói lại chậm hơn giúp cháu nhé."
        );
    }

    @Override
    public String getText(ResponseKey key) {
        return key == null ? "" : responseMap.getOrDefault(key, "");
    }
    @Override
    public String getText(String keyName) {
        try {
            // Thử biến đổi String (ví dụ "ASK_TIME") thành Enum ResponseKey.ASK_TIME
            ResponseKey key = ResponseKey.valueOf(keyName);
            return getText(key);
        } catch (IllegalArgumentException | NullPointerException e) {
            // Nếu không tìm thấy key tương ứng trong Enum -> Trả về câu mặc định
            return "Dạ cháu chưa rõ, bác nói lại giúp cháu nhé.";
        }
    }
}
