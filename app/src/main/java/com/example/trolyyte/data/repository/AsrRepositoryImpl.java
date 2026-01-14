package com.example.trolyyte.data.repository;

import com.example.trolyyte.data.asr.AsrEngine;
import com.example.trolyyte.domain.repository.AsrRepository;

public class AsrRepositoryImpl implements AsrRepository {

    private final AsrEngine asrEngine;

    public AsrRepositoryImpl(AsrEngine asrEngine) {
        this.asrEngine = asrEngine;
        // Tự động init model khi Repository được tạo (hoặc gọi lazy tuỳ bạn)
        this.asrEngine.initialize();
    }

    @Override
    public void startListening(AsrCallback callback) {
        // Gọi xuống Engine
        asrEngine.startListening(new AsrEngine.Callback() {
            @Override
            public void onReady() {
                callback.onReady();
            }

            @Override
            public void onPartialResult(String text) {
                callback.onPartialResult(text);
            }

            @Override
            public void onFinalResult(String text) {
                callback.onFinalResult(text);
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e.getMessage());
            }
        });
    }

    @Override
    public void stopListening() {
        asrEngine.stopListening();
    }
}