package com.example.trolyyte.data.repository;

import com.example.trolyyte.data.tts.TtsEngine;
import com.example.trolyyte.domain.repository.TtsRepository;

public class TtsRepositoryImpl implements TtsRepository {

    private final TtsEngine ttsEngine;

    public TtsRepositoryImpl(TtsEngine ttsEngine) {
        this.ttsEngine = ttsEngine;
        this.ttsEngine.initialize(); // Tự động khởi tạo khi Repository được tạo
    }

    @Override
    public void speak(String text) {
        // Gọi hàm speak không cần callback
        ttsEngine.speak(text, null);
    }

    @Override
    public void speak(String text, Callback callback) {
        // Map callback của Domain sang callback của Data Engine
        ttsEngine.speak(text, new TtsEngine.Callback() {
            @Override
            public void onSpeakDone() {
                if (callback != null) callback.onDone();
            }

            @Override
            public void onSpeakError() {
                if (callback != null) callback.onError();
            }
        });
    }

    @Override
    public void stop() {
        ttsEngine.stop();
    }

    // Nên có thêm hàm release để gọi ở onDestroy của Activity/ViewModel
    public void release() {
        ttsEngine.shutdown();
    }
}