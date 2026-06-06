package com.example.trolyyte.data.tts;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import java.util.Locale;
import java.util.UUID;

public class AndroidTtsEngine implements TtsEngine, TextToSpeech.OnInitListener {

    private static final String TAG = "AndroidTtsEngine";
    private TextToSpeech textToSpeech;
    private final Context context;
    private final Handler mainHandler; // Dùng để đẩy callback về UI Thread
    private boolean isInitialized = false;

    // Lưu callback hiện tại để gọi khi nói xong
    private Callback currentCallback;

    public AndroidTtsEngine(Context context) {
        this.context = context;
        this.mainHandler = new Handler(Looper.getMainLooper());

        // GỌI KHỞI TẠO NGAY KHI TẠO ENGINE ĐỂ LÀM NÓNG SẴN
        initialize();
    }

    @Override
    public void initialize() {
        Log.d(TAG, "initialize called");

        // SỬA LỖI CHÍ MẠNG Ở ĐÂY: Truyền 'this' để nó trỏ tới hàm onInit() bên dưới
        if (textToSpeech == null) {
            textToSpeech = new TextToSpeech(context, this);
        }
    }

    // Hàm này BẮT BUỘC phải được gọi khi TextToSpeech khởi tạo xong
    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            // Cấu hình tiếng Việt
            int result = textToSpeech.setLanguage(new Locale("vi", "VN"));

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(TAG, "Ngôn ngữ tiếng Việt không được hỗ trợ hoặc thiếu dữ liệu voice.");
            } else {
                isInitialized = true;
                setupUtteranceListener();
                Log.d(TAG, "TTS Initialized successfully (Đã cài đặt Tiếng Việt)");
            }
        } else {
            Log.e(TAG, "TTS Initialization failed");
        }
    }

    private void setupUtteranceListener() {
        textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
                // Bắt đầu nói
            }

            @Override
            public void onDone(String utteranceId) {
                // Quan trọng: Đẩy về Main Thread để ViewModel có thể update UI hoặc bật Mic
                mainHandler.post(() -> {
                    if (currentCallback != null) {
                        currentCallback.onSpeakDone();
                        currentCallback = null; // Clear sau khi dùng
                    }
                });
            }

            @Override
            public void onError(String utteranceId) {
                mainHandler.post(() -> {
                    if (currentCallback != null) {
                        currentCallback.onSpeakError();
                        currentCallback = null;
                    }
                });
            }
        });
    }

    @Override
    public void speak(String text, Callback callback) {
        if (!isInitialized || textToSpeech == null) {
            Log.e(TAG, "TTS chưa sẵn sàng, đang khởi tạo lại...");
            initialize(); // Thử init lại
            if (callback != null) callback.onSpeakError();
            return;
        }

        this.currentCallback = callback;

        // Tạo ID duy nhất cho câu nói này (Bắt buộc để UtteranceListener hoạt động)
        String utteranceId = UUID.randomUUID().toString();

        // Bundle params (API mới của Android)
        Bundle params = new Bundle();
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId);

        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId);
        Log.d(TAG, "Đang đọc: " + text);
    }

    @Override
    public void stop() {
        if (textToSpeech != null) {
            textToSpeech.stop();
        }
        currentCallback = null;
    }

    @Override
    public void shutdown() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }

    @Override
    public void setSpeechRate(float speed) {
        if (textToSpeech != null) {
            // Hàm mặc định của Android TTS: 1.0 là bình thường, < 1.0 là chậm, > 1.0 là nhanh
            textToSpeech.setSpeechRate(speed);
            Log.d(TAG, "Đã cài đặt tốc độ TTS thành: " + speed);
        } else {
            Log.w(TAG, "Chưa thể set tốc độ TTS vì TextToSpeech đang null");
        }
    }
}