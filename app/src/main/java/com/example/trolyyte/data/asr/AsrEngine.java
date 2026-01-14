package com.example.trolyyte.data.asr;

public interface AsrEngine {

    // Callback để trả kết quả từ Engine về Repository
    interface Callback {
        void onReady();                 // Đã load model xong, sẵn sàng nghe
        void onPartialResult(String text); // Kết quả đang nói (chưa chốt)
        void onFinalResult(String text);   // Kết quả chốt câu
        void onError(Exception e);
    }

    /**
     * Khởi tạo Model (nên gọi khi App start hoặc vào màn hình Home)
     */
    void initialize();

    /**
     * Bắt đầu thu âm
     */
    void startListening(Callback callback);

    /**
     * Dừng thu âm
     */
    void stopListening();

    /**
     * Giải phóng bộ nhớ (Model, Recognizer)
     */
    void release();
}