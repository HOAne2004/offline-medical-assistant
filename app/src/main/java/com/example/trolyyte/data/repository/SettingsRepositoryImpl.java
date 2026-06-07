package com.example.trolyyte.data.repository;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.trolyyte.domain.repository.SettingsRepository;

public class SettingsRepositoryImpl implements SettingsRepository {
    private final SharedPreferences prefs;
    private static final String PREF_NAME = "trolyyte_settings";
    private static final String KEY_TTS_SPEED = "tts_speed";

    public SettingsRepositoryImpl(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    @Override
    public void saveTtsSpeed(float speed) {
        prefs.edit().putFloat(KEY_TTS_SPEED, speed).apply();
    }

    @Override
    public float getTtsSpeed() {
        // Nếu chưa lưu lần nào, trả về tốc độ mặc định là 1.0f (Bình thường)
        return prefs.getFloat(KEY_TTS_SPEED, 1.0f);
    }

    @Override
    public int getHistoryRetentionDays(){
        return 3;
    }

    @Override
    public void saveHistoryRetentionDays(int days){
        prefs.edit().putInt("history_retention_days", days).apply();
    }
}