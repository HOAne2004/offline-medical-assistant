package com.example.trolyyte.domain.usecase;

public interface ListenVoiceUseCase {

    void start(Listener listener);

    void stop();

    interface Listener {
        void onResult(ListenVoiceResult result);
    }
}
