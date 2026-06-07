package com.example.trolyyte.data.wakeword;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.trolyyte.presentation.main.MainActivity;

import org.json.JSONObject;
import org.vosk.Model;
import org.vosk.Recognizer;
import org.vosk.android.RecognitionListener;
import org.vosk.android.SpeechService;
import org.vosk.android.StorageService;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

public class WakeWordService extends Service implements RecognitionListener {

    private static final String TAG = "WakeWordService";

    private static final String CHANNEL_ID = "wake_word_channel";
    private static final int NOTIFICATION_ID = 1;

    private static final float SAMPLE_RATE = 16000.0f;

    private Model model;
    private SpeechService speechService;

    private boolean wakeWordDetected = false;

    private static final String WAKE_GRAMMAR =
            "[\"bác sĩ ơi\", \"bác sỹ ơi\", \"bác sĩ\", \"bác sỹ\", " +
                    "\"ơi bác sĩ\", \"ơi bác sỹ\", \"bác si ơi\", \"bac si oi\", \"[unk]\"]";

    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannel();
        startAsForegroundService();
        initVoskModel();
    }

    private void startAsForegroundService() {
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Trợ lý y tế")
                .setContentText("Đang lắng nghe: Bác sĩ ơi")
                .setSmallIcon(android.R.drawable.ic_btn_speak_now)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
            );
        } else {
            startForeground(NOTIFICATION_ID, notification);
        }
    }

    private void initVoskModel() {
        StorageService.unpack(
                this,
                "model-vn",
                "model",
                loadedModel -> {
                    model = loadedModel;
                    startWakeListening();
                },
                exception -> {
                    Log.e(TAG, "Lỗi load model VOSK", exception);
                    stopSelf();
                }
        );
    }

    private void startWakeListening() {
        try {
            Recognizer recognizer = new Recognizer(model, SAMPLE_RATE);
            speechService = new SpeechService(recognizer, SAMPLE_RATE);
            speechService.startListening(this);

            Log.d(TAG, "Đang lắng nghe wake word...");

        } catch (Exception e) {
            Log.e(TAG, "Lỗi khởi động VOSK", e);
            stopSelf();
        }
    }

    @Override
    public void onPartialResult(String hypothesis) {
        Log.d(TAG, "RAW PARTIAL: " + hypothesis);

        String text = extractTextFromJson(hypothesis, "partial");

        Log.d(TAG, "PARTIAL TEXT: " + text);

        if (isWakeWord(text)) {
            handleWakeWordDetected();
        }
    }

    @Override
    public void onResult(String hypothesis) {
        Log.d(TAG, "RAW RESULT: " + hypothesis);

        String text = extractTextFromJson(hypothesis, "text");

        Log.d(TAG, "RESULT TEXT: " + text);

        if (isWakeWord(text)) {
            handleWakeWordDetected();
        }
    }

    @Override
    public void onFinalResult(String hypothesis) {
        String text = extractTextFromJson(hypothesis, "text");

        if (isWakeWord(text)) {
            handleWakeWordDetected();
        }
    }

    @Override
    public void onError(Exception e) {
        Log.e(TAG, "Lỗi VOSK", e);
        restartListening();
    }

    @Override
    public void onTimeout() {
        restartListening();
    }

    private void handleWakeWordDetected() {
        if (wakeWordDetected) return;

        wakeWordDetected = true;

        Log.d(TAG, "Đã phát hiện wake word: Bác sĩ ơi");

        if (speechService != null) {
            speechService.stop();
            speechService.shutdown();
            speechService = null;
        }

        openAppAndStartAssistant();
    }

    private void openAppAndStartAssistant() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("WAKE_WORD_DETECTED", true);
        startActivity(intent);
    }

    private boolean isWakeWord(String rawText) {
        if (rawText == null || rawText.trim().isEmpty()) {
            return false;
        }

        String text = normalizeVietnamese(rawText);

        Log.d(TAG, "Nghe được: " + rawText + " | Chuẩn hóa: " + text);

        if (text.equals("bac si oi")) return true;
        if (text.equals("bac sy oi")) return true;
        if (text.equals("oi bac si")) return true;
        if (text.equals("oi bac sy")) return true;

        // Trường hợp VOSK nhận nhầm "Bác sĩ ơi" thành "bật cười"
        if (text.equals("bat cuoi")) return true;
        if (text.equals("bac cuoi")) return true;
        if (text.equals("bat si oi")) return true;
        if (text.equals("bac oi")) return true;

        // Trường hợp câu dài hơn, ví dụ: "xin chào bác sĩ ơi"
        if (text.contains("bac si oi")) return true;
        if (text.contains("bac sy oi")) return true;
        if (text.contains("oi bac si")) return true;
        if (text.contains("oi bac sy")) return true;

        return false;
    }

    private String normalizeVietnamese(String input) {
        String text = input.toLowerCase(Locale.ROOT).trim();

        text = text.replace("đ", "d");

        text = Normalizer.normalize(text, Normalizer.Form.NFD);
        text = Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
                .matcher(text)
                .replaceAll("");

        text = text.replaceAll("[^a-zA-Z0-9\\s]", " ");
        text = text.replaceAll("\\s+", " ").trim();

        text = text.replace("bac sy", "bac si");
        text = text.replace("bac si oi", "bac si oi");

        return text;
    }

    private String extractTextFromJson(String json, String key) {
        try {
            JSONObject object = new JSONObject(json);
            return object.optString(key, "");
        } catch (Exception e) {
            return "";
        }
    }

    private int levenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];

        for (int i = 0; i <= s1.length(); i++) {
            dp[i][0] = i;
        }

        for (int j = 0; j <= s2.length(); j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                int cost = s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1;

                dp[i][j] = Math.min(
                        Math.min(
                                dp[i - 1][j] + 1,
                                dp[i][j - 1] + 1
                        ),
                        dp[i - 1][j - 1] + cost
                );
            }
        }

        return dp[s1.length()][s2.length()];
    }

    private void restartListening() {
        if (wakeWordDetected) return;

        try {
            if (speechService != null) {
                speechService.stop();
                speechService.shutdown();
                speechService = null;
            }

            startWakeListening();

        } catch (Exception e) {
            Log.e(TAG, "Không thể restart VOSK", e);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        try {
            if (speechService != null) {
                speechService.stop();
                speechService.shutdown();
            }

            if (model != null) {
                model.close();
            }

        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi đóng WakeWordService", e);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Wake Word Service",
                    NotificationManager.IMPORTANCE_LOW
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }
}