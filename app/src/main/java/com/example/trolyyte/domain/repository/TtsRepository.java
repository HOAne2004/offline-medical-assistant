package com.example.trolyyte.domain.repository;

public interface TtsRepository {

    // Hàm nói đơn giản
    void speak(String text);

    // Hàm nói có callback (Để biết khi nào nói xong)
    void speak(String text, Callback callback);

    // Hàm dừng nói ngay lập tức
    void stop();

    // Interface callback
    interface Callback {
        void onDone();      // Khi nói xong
        void onError();     // Khi lỗi
    }
}