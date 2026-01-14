package com.example.trolyyte.domain.model;

public class SpeakResult {
    private final String textToSpeak;   // Văn bản để đưa vào TTS
    private final boolean shouldListen; // Cờ báo: Nói xong có cần bật mic nghe tiếp không?
    private final ResponseKey responseKey; // Key để UI có thể hiển thị icon/text tương ứng

    public SpeakResult(String textToSpeak, boolean shouldListen, ResponseKey responseKey) {
        this.textToSpeak = textToSpeak;
        this.shouldListen = shouldListen;
        this.responseKey = responseKey;
    }

    public String getTextToSpeak() { return textToSpeak; }
    public boolean shouldListen() { return shouldListen; }
    public ResponseKey getResponseKey() { return responseKey; }

    // Helper để kiểm tra nhanh
    public boolean shouldSpeakImmediately() {
        return textToSpeak != null && !textToSpeak.isEmpty();
    }
}