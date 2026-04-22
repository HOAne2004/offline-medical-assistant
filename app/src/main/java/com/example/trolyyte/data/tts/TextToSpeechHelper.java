package com.example.trolyyte.data.tts;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import java.util.Locale;

public class TextToSpeechHelper {
    private TextToSpeech tts;
    private boolean isReady = false;

    public TextToSpeechHelper(Context context) {
        tts = new TextToSpeech(context, status -> {
            if (status != TextToSpeech.ERROR) {
                // Chốt cấu hình tiếng Việt
                int result = tts.setLanguage(new Locale("vi", "VN"));
                if (result != TextToSpeech.LANG_MISSING_DATA
                        && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                    isReady = true;
                }
            }
        });
    }

    public void speak(String text) {
        if (isReady && tts != null) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "TTS_ID");
        }
    }

    // Thêm hàm giải phóng bộ nhớ khi đóng App
    public void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }
}