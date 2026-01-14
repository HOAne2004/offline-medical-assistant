package com.example.trolyyte.data.tts;

public interface TtsEngine {

    interface Callback {
        void onSpeakDone();
        void onSpeakError();
    }

    void initialize();

    void speak(String text, Callback callback);

    void stop();

    void shutdown();
}