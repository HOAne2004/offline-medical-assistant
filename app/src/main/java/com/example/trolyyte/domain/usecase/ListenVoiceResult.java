package com.example.trolyyte.domain.usecase;

public class ListenVoiceResult {
    // Enum để UI biết đang ở giai đoạn nào
    public enum Status {
        LISTENING,      // Mic đã bật, đang chờ nói
        PARTIAL,        // Đang nói (trả về từng từ) -> Rất quan trọng cho UX
        FINAL,          // Đã ngắt câu (câu hoàn chỉnh)
        ERROR
    }

    private final Status status;
    private final String text;
    private final String error;

    private ListenVoiceResult(Status status, String text, String error) {
        this.status = status;
        this.text = text;
        this.error = error;
    }

    public static ListenVoiceResult listening() {
        return new ListenVoiceResult(Status.LISTENING, null, null);
    }

    public static ListenVoiceResult partial(String text) {
        return new ListenVoiceResult(Status.PARTIAL, text, null);
    }

    public static ListenVoiceResult finalResult(String text) {
        return new ListenVoiceResult(Status.FINAL, text, null);
    }

    public static ListenVoiceResult error(String error) {
        return new ListenVoiceResult(Status.ERROR, null, error);
    }

    public Status getStatus() { return status; }
    public String getText() { return text; }
    public String getError() { return error; }
}