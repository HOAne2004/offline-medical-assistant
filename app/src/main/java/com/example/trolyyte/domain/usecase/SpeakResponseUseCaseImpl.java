package com.example.trolyyte.domain.usecase;

import com.example.trolyyte.domain.dialog.DialogContext;
import com.example.trolyyte.domain.dialog.DialogueAction;
import com.example.trolyyte.domain.dialog.DialogueResult;
import com.example.trolyyte.domain.dialog.ResponseTemplateProvider;
import com.example.trolyyte.domain.model.ResponseKey;
import com.example.trolyyte.domain.model.SpeakResult;

public class SpeakResponseUseCaseImpl implements SpeakResponseUseCase {

    private final ResponseTemplateProvider templateProvider;

    public SpeakResponseUseCaseImpl(ResponseTemplateProvider templateProvider) {
        this.templateProvider = templateProvider;
    }

    @Override
    public SpeakResult execute(DialogueResult dialogueResult) {
        DialogueAction action = dialogueResult.getAction();
        DialogContext context = dialogueResult.getUpdatedContext();

        // 1. Map Action -> Key
        ResponseKey key = DialogueActionMapper.map(action);
        boolean shouldListen = DialogueActionMapper.shouldListenAfter(action);

        // 2. Lấy mẫu câu thô (Raw Template)
        String template = templateProvider.getTemplate(key);

        // 3. Điền thông tin (Slot Filling) - Logic quan trọng cho người già dễ hiểu
        String finalSpeech = fillSlots(template, context, action);

        return new SpeakResult(finalSpeech, shouldListen, key);
    }

    private String fillSlots(String template, DialogContext context, DialogueAction action) {
        if (template == null) return "";

        String result = template;

        // Thay thế tên thuốc nếu có trong ngữ cảnh
        if (context.getMedicineName() != null) {
            result = result.replace("{medicine}", context.getMedicineName());
        } else {
            // Fallback nếu template có placeholder nhưng data null (tránh lỗi nói "null")
            result = result.replace("{medicine}", "thuốc này");
        }

        // Thay thế thời gian
        if (context.getReminderTime() != null) {
            result = result.replace("{time}", formatTimeForSpeech(context.getReminderTime()));
        }

        return result;
    }

    // Helper: Định dạng giờ cho dễ nghe (VD: "14:00" -> "14 giờ")
    private String formatTimeForSpeech(String rawTime) {
        // Logic xử lý chuỗi đơn giản để TTS đọc tự nhiên hơn
        return rawTime.replace(":", " giờ ");
    }
}