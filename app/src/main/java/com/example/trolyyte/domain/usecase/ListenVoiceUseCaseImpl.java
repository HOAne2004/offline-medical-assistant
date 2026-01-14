package com.example.trolyyte.domain.usecase;

import com.example.trolyyte.domain.repository.AsrRepository;

public class ListenVoiceUseCaseImpl implements ListenVoiceUseCase {
    private final AsrRepository asrRepository;

    public ListenVoiceUseCaseImpl(AsrRepository asrRepository) {
        this.asrRepository = asrRepository;
    }

    @Override
    public void start(Listener listener) {
        asrRepository.startListening(new AsrRepository.AsrCallback() {
            @Override
            public void onReady() {
                listener.onResult(ListenVoiceResult.listening());
            }

            @Override
            public void onPartialResult(String text) {
                // Vosk trả về partial rất nhanh, cần đẩy lên UI
                listener.onResult(ListenVoiceResult.partial(text));
            }

            @Override
            public void onFinalResult(String text) {
                listener.onResult(ListenVoiceResult.finalResult(text));
            }

            @Override
            public void onError(String error) {
                listener.onResult(ListenVoiceResult.error(error));
            }
        });
    }

    @Override
    public void stop() {
        asrRepository.stopListening();
    }
}