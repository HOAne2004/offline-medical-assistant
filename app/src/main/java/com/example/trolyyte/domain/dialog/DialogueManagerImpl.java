package com.example.trolyyte.domain.dialog;

import com.example.trolyyte.domain.model.DialogState;
import com.example.trolyyte.domain.model.NlpResult;
import com.example.trolyyte.domain.model.NluIntent;
import java.util.Map;

public class DialogueManagerImpl implements DialogueManager {

    @Override
    public DialogueResult handle(NlpResult nlpResult, DialogContext context) {
        NluIntent intent = nlpResult.getIntent();

        // Đảm bảo không bị null pointer
        if (context.getCurrentState() == null) {
            context.setCurrentState(DialogState.IDLE);
        }

        switch (context.getCurrentState()) {
            case IDLE:
                return handleIdleState(intent, nlpResult, context);

            case COLLECTING_MEDICINE_INFO:
                return handleCollectingMedicineInfo(intent, nlpResult, context);

            case CONFIRMING_MEDICINE_REMINDER:
                return handleConfirmingMedicine(intent, context); // Đã dọn dẹp biến nlpResult thừa

            default:
                context.reset();
                return new DialogueResult(DialogueAction.UNKNOWN_COMMAND, context);
        }
    }

    private DialogueResult handleIdleState(
            NluIntent intent,
            NlpResult nlpResult,
            DialogContext context
    ) {

        switch (intent) {

            // =================================================
            // NHẮC THUỐC
            // =================================================
            case SET_OR_UPDATE_MEDICATION:
                context.setCurrentState(DialogState.COLLECTING_MEDICINE_INFO);
                fillSlots(nlpResult, context);
                return checkMissingInfoOrConfirm(context);

            // =================================================
            // HỎI THUỐC
            // =================================================
            case INQUIRE_MEDICINE:
                return new DialogueResult(
                        DialogueAction.SHOW_MEDICINE_INFO,
                        context
                );

            // =================================================
            // TRÒ CHUYỆN
            // =================================================
            case SMALL_TALK:
                return new DialogueResult(
                        DialogueAction.COMPLETE_DIALOGUE,
                        context
                );

            // =================================================
            // KHẨN CẤP
            // =================================================
            case REQUEST_EMERGENCY:
                return new DialogueResult(
                        DialogueAction.ASK_CONFIRM_EMERGENCY,
                        context
                );

            // =================================================
            // KHÔNG HIỂU
            // =================================================
            default:
                return new DialogueResult(
                        DialogueAction.UNKNOWN_COMMAND,
                        context
                );
        }
    }

    private DialogueResult handleCollectingMedicineInfo(NluIntent intent, NlpResult nlpResult, DialogContext context) {
        if (intent == NluIntent.DENY || intent == NluIntent.STOP_ACTION || intent == NluIntent.UNKNOWN) {
            // Đã fix lỗi "empty body": Khi user từ chối/dừng -> Reset trí nhớ và Hủy luồng
            context.reset();
            return new DialogueResult(DialogueAction.COMPLETE_DIALOGUE, context);
        }
        fillSlots(nlpResult, context);
        return checkMissingInfoOrConfirm(context);
    }

    // Đã fix lỗi "Parameter is never used" bằng cách xóa bỏ biến nlpResult khỏi hàm này
    private DialogueResult handleConfirmingMedicine(NluIntent intent, DialogContext context) {
        if (intent == NluIntent.AFFIRM) {
            context.setCurrentState(DialogState.IDLE);
            return new DialogueResult(DialogueAction.CONFIRM_MEDICINE_REMINDER_CREATED, context);
        }
        else if (intent == NluIntent.DENY) {
            context.reset();
            return new DialogueResult(DialogueAction.COMPLETE_DIALOGUE, context);
        }
        return new DialogueResult(DialogueAction.ASK_CONFIRMATION, context);
    }

    private void fillSlots(NlpResult result, DialogContext context) {
        Map<String, String> entities = result.getEntities();
        if (entities.containsKey("medicine_name")) {
            context.setMedicineName(entities.get("medicine_name"));
        }
        if (entities.containsKey("time")) {
            context.setReminderTime(entities.get("time"));
        }
    }

    private DialogueResult checkMissingInfoOrConfirm(DialogContext context) {
        if (context.getMedicineName() == null) {
            return new DialogueResult(DialogueAction.ASK_MEDICINE_NAME, context);
        }
        if (context.getReminderTime() == null) {
            return new DialogueResult(DialogueAction.ASK_TIME, context);
        }
        context.setCurrentState(DialogState.CONFIRMING_MEDICINE_REMINDER);
        return new DialogueResult(DialogueAction.ASK_CONFIRMATION, context);
    }
}