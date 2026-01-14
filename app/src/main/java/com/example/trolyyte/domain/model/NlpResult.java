package com.example.trolyyte.domain.model;

import java.util.Map;

public class NlpResult {
    private final String originalText;
    private final NluIntent intent;
    private final Map<String, String> entities; // Danh sách thực thể (thuốc, giờ...)
    private final float confidence;             // Độ tin cậy (0.0 - 1.0)

    // Constructor đầy đủ 4 tham số (Đây là cái mà Engine đang gọi)
    public NlpResult(String originalText, NluIntent intent, Map<String, String> entities, float confidence) {
        this.originalText = originalText;
        this.intent = intent;
        this.entities = entities;
        this.confidence = confidence;
    }

    public String getOriginalText() { return originalText; }
    public NluIntent getIntent() { return intent; }
    public Map<String, String> getEntities() { return entities; }
    public float getConfidence() { return confidence; }
}