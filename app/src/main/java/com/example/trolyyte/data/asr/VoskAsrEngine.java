package com.example.trolyyte.data.asr;

import android.content.Context;
import android.util.Log;
import com.example.trolyyte.domain.usecase.ListenVoiceResult; // Đảm bảo import đúng

import org.json.JSONObject;
import org.vosk.Model;
import org.vosk.Recognizer;
import org.vosk.android.RecognitionListener;
import org.vosk.android.SpeechService;
import org.vosk.android.StorageService;

import java.io.IOException;

public class VoskAsrEngine implements AsrEngine, RecognitionListener {

    private static final String TAG = "VoskAsrEngine";
    private final Context context;

    private Model model;
    private SpeechService speechService;
    private Callback callback;

    private boolean isModelLoaded = false;
    private boolean isInitializing = false; // Cờ mới để chặn init nhiều lần

    public VoskAsrEngine(Context context) {
        this.context = context;
    }

    @Override
    public void initialize() {
        // Nếu đã load xong hoặc đang load thì không làm gì cả
        if (isModelLoaded || isInitializing) return;

        isInitializing = true; // Đánh dấu đang load
        Log.d(TAG, "Bắt đầu giải nén Model Vosk...");

        StorageService.unpack(context, "model-vn", "model",
                (model) -> {
                    this.model = model;
                    isModelLoaded = true;
                    isInitializing = false; // Load xong
                    Log.d(TAG, "Vosk Model loaded successfully");
                    if (callback != null) callback.onReady();
                },
                (exception) -> {
                    isInitializing = false; // Lỗi cũng phải reset cờ
                    Log.e(TAG, "Failed to load model: " + exception.getMessage());
                    if (callback != null) callback.onError(exception);
                });
    }

    @Override
    public void startListening(Callback callback) {
        this.callback = callback;

        if (!isModelLoaded) {
            if (!isInitializing) {
                initialize(); // Nếu chưa chạy thì chạy init
            }
            // Báo lỗi nhẹ để UI hiển thị "Đang khởi động..." thay vì crash
            callback.onError(new Exception("Hệ thống đang khởi động, vui lòng đợi 2 giây..."));
            return;
        }

        try {
            if (speechService != null) {
                speechService.stop();
                speechService = null;
            }

            // Tạo Recognizer mới mỗi lần nghe để tránh lỗi state cũ
            Recognizer recognizer = new Recognizer(model, 16000.0f);
            speechService = new SpeechService(recognizer, 16000.0f);
            speechService.startListening(this);

            callback.onReady(); // Báo UI là đã bắt đầu nghe

        } catch (IOException e) {
            callback.onError(e);
        }
    }

    @Override
    public void stopListening() {
        if (speechService != null) {
            speechService.stop();
            speechService = null;
        }
    }

    // ... (Giữ nguyên các hàm RecognitionListener bên dưới của bạn) ...
    @Override
    public void onResult(String hypothesis) {
        String cleanText = parseVoskJson(hypothesis, "text");
        if (!cleanText.isEmpty() && callback != null) {
            callback.onFinalResult(cleanText);
        }
    }

    @Override
    public void onPartialResult(String hypothesis) {
        String cleanText = parseVoskJson(hypothesis, "partial");
        if (!cleanText.isEmpty() && callback != null) {
            callback.onPartialResult(cleanText);
        }
    }

    @Override
    public void onFinalResult(String hypothesis) {
        String cleanText = parseVoskJson(hypothesis, "text");
        if (!cleanText.isEmpty() && callback != null) {
            callback.onFinalResult(cleanText);
        }
    }

    @Override
    public void onError(Exception exception) {
        if (callback != null) callback.onError(exception);
    }

    @Override
    public void onTimeout() {}

    @Override
    public void release() {
        stopListening();
        if (model != null) {
            model.close();
            model = null;
        }
        isModelLoaded = false;
    }

    private String parseVoskJson(String jsonString, String key) {
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            return jsonObject.optString(key, "");
        } catch (Exception e) {
            return "";
        }
    }
}