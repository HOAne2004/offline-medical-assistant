package com.example.trolyyte.domain.repository;

public interface AsrRepository {
    void startListening(AsrCallback callback);
    void stopListening();

    interface AsrCallback {
        void onReady();                 // Khi mic đã sẵn sàng
        void onPartialResult(String text); // Kết quả tạm thời
        void onFinalResult(String text);   // Kết quả chốt
        void onError(String error);
    }
}