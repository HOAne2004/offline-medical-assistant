package com.example.trolyyte.domain.dialog;

import com.example.trolyyte.domain.model.DialogState;
import com.example.trolyyte.domain.model.EntityType; // Nhớ import
import com.example.trolyyte.domain.model.NlpResult;
import com.example.trolyyte.domain.model.NluIntent;
import java.util.Map;

public class DialogueManagerImpl implements DialogueManager {

    @Override
    public DialogueResult handle(NlpResult nlpResult, DialogContext context) {
        NluIntent intent = nlpResult.getIntent();

        switch (context.getCurrentState()) {
            case IDLE:
                return handleIdleState(intent, nlpResult, context);

            case COLLECTING_MEDICINE_INFO: // Đã sửa tên đúng với Enum
                return handleCollectingMedicineInfo(intent, nlpResult, context);

            case CONFIRMING_MEDICINE_REMINDER:
                return handleConfirmingMedicine(intent, nlpResult, context);

            default:
                context.reset();
                return new DialogueResult(DialogueAction.UNKNOWN_COMMAND, context);
        }
    }

    // --- Xử lý trạng thái IDLE ---
    private DialogueResult handleIdleState(NluIntent intent, NlpResult nlpResult, DialogContext context) {
        if (intent == NluIntent.CREATE_MEDICINE_REMINDER) {
            // Chuyển trạng thái sang đang thu thập thông tin
            context.setCurrentState(DialogState.COLLECTING_MEDICINE_INFO);

            // Trích xuất entity nếu có (Fill slots ngay lập tức)
            fillSlots(nlpResult, context);

            // Kiểm tra xem đã đủ thông tin chưa
            return checkMissingInfoOrConfirm(context);
        }

        // Các intent khác...
        return new DialogueResult(DialogueAction.UNKNOWN_COMMAND, context);
    }

    // --- Xử lý trạng thái ĐANG THU THẬP THÔNG TIN ---
    private DialogueResult handleCollectingMedicineInfo(NluIntent intent, NlpResult nlpResult, DialogContext context) {
        // Nếu người dùng muốn hủy
        if (intent == NluIntent.CONFIRM_NO || intent == NluIntent.UNKNOWN) { // Hoặc intent STOP
            // Có thể xử lý logic hủy hoặc cố gắng trích xuất thông tin từ câu nói
        }

        // Cố gắng lấy thêm thông tin từ câu nói mới
        fillSlots(nlpResult, context);

        return checkMissingInfoOrConfirm(context);
    }

    // --- Xử lý trạng thái CHỜ XÁC NHẬN ---
    private DialogueResult handleConfirmingMedicine(NluIntent intent, NlpResult nlpResult, DialogContext context) {
        if (intent == NluIntent.CONFIRM_YES) {
            // Người dùng đồng ý -> Hoàn tất
            context.setCurrentState(DialogState.IDLE); // Reset về IDLE
            // Ở đây giữ lại thông tin trong context để ViewModel lưu DB
            return new DialogueResult(DialogueAction.CONFIRM_MEDICINE_REMINDER_CREATED, context);
        }
        else if (intent == NluIntent.CONFIRM_NO) {
            context.reset();
            return new DialogueResult(DialogueAction.COMPLETE_DIALOGUE, context); // Hủy
        }

        // Người dùng nói lung tung -> Hỏi lại
        return new DialogueResult(DialogueAction.ASK_CONFIRMATION, context);
    }

    // --- Hàm phụ trợ logic ---

    private void fillSlots(NlpResult result, DialogContext context) {
        Map<String, String> entities = result.getEntities(); // Giả sử NlpResult trả về Map key là String

        // Logic mapping từ Map entities của NLP sang Context
        // Lưu ý: Cần đảm bảo key trong Map khớp với logic ở đây
        if (entities.containsKey("medicine_name")) {
            context.setMedicineName(entities.get("medicine_name"));
        }
        if (entities.containsKey("time")) {
            context.setReminderTime(entities.get("time"));
        }
        // Nếu NlpResult của bạn dùng enum EntityType làm key cho map thì sửa lại:
        // if (result.hasEntity(EntityType.MEDICINE_NAME)) context.setMedicineName(...)
    }

    private DialogueResult checkMissingInfoOrConfirm(DialogContext context) {
        // Ưu tiên 1: Hỏi tên thuốc
        if (context.getMedicineName() == null) {
            return new DialogueResult(DialogueAction.ASK_MEDICINE_NAME, context);
        }

        // Ưu tiên 2: Hỏi giờ
        if (context.getReminderTime() == null) {
            return new DialogueResult(DialogueAction.ASK_TIME, context);
        }

        // Đủ thông tin -> Chuyển sang xác nhận
        context.setCurrentState(DialogState.CONFIRMING_MEDICINE_REMINDER);
        return new DialogueResult(DialogueAction.ASK_CONFIRMATION, context);
    }
}