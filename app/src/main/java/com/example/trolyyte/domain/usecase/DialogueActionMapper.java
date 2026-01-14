package com.example.trolyyte.domain.usecase;

import com.example.trolyyte.domain.dialog.DialogueAction;
import com.example.trolyyte.domain.model.ResponseKey;

public class DialogueActionMapper {

    public static ResponseKey map(DialogueAction action) {

        switch (action) {

            case ASK_MEDICINE_NAME:
                return ResponseKey.ASK_MEDICINE_NAME;

            case ASK_DOSAGE:
                return ResponseKey.ASK_DOSAGE;

            case ASK_TIME:
                return ResponseKey.ASK_TIME;

            case ASK_FREQUENCY:
                return ResponseKey.ASK_FREQUENCY;

            case ASK_DATE:
                return ResponseKey.ASK_DATE;

            case ASK_LOCATION:
                return ResponseKey.ASK_LOCATION;

            case ASK_SYMPTOM:
                return ResponseKey.ASK_SYMPTOM;

            case ASK_SEVERITY:
                return ResponseKey.ASK_SEVERITY;

            case ASK_CONFIRMATION:
                return ResponseKey.ASK_CONFIRMATION;

            case ASK_CONFIRM_EMERGENCY:
                return ResponseKey.ASK_CONFIRM_EMERGENCY;

            case CONFIRM_MEDICINE_REMINDER_CREATED:
                return ResponseKey.MEDICINE_REMINDER_CREATED;

            case CONFIRM_APPOINTMENT_CREATED:
                return ResponseKey.APPOINTMENT_CREATED;

            case CONFIRM_SYMPTOM_RECORDED:
                return ResponseKey.SYMPTOM_RECORDED;

            case SHOW_MEDICINE_INFO:
                return ResponseKey.MEDICINE_INFO;

            case SHOW_SYMPTOM_INFO:
                return ResponseKey.SYMPTOM_INFO;

            case TRIGGER_EMERGENCY:
                return ResponseKey.EMERGENCY_TRIGGERED;

            case UNKNOWN_COMMAND:
                return ResponseKey.UNKNOWN_COMMAND;

            case NEED_REPEAT:
                return ResponseKey.NEED_REPEAT;

            case NO_ACTION:
            default:
                return null;
        }
    }
    /**
     * Xác định xem sau khi nói câu này, có cần bật Mic nghe tiếp không?
     */
    public static boolean shouldListenAfter(DialogueAction action) {
        switch (action) {
            case ASK_MEDICINE_NAME:
            case ASK_TIME:
            case ASK_DATE:
            case ASK_CONFIRMATION:
            case ASK_SYMPTOM:
            case UNKNOWN_COMMAND: // Không hiểu thì phải nghe lại
                return true;
            default:
                return false; // Các câu xác nhận thành công/kết thúc thì không cần nghe
        }
    }
}
