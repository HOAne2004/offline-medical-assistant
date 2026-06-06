package com.example.trolyyte.presentation.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.trolyyte.domain.repository.SettingsRepository;
import com.example.trolyyte.domain.repository.TtsRepository;

public class SettingsViewModel extends ViewModel {
    private final SettingsRepository settingsRepository;
    private final TtsRepository ttsRepository;

    // Quản lý trạng thái tốc độ trên UI
    private final MutableLiveData<Float> _currentSpeed = new MutableLiveData<>();
    public LiveData<Float> getCurrentSpeed() { return _currentSpeed; }

    // Quản lý trạng thái số ngày lưu lịch sử
    private final MutableLiveData<Integer> _retentionDays = new MutableLiveData<>();
    public LiveData<Integer> getRetentionDays() { return _retentionDays; }

    // Thông báo lưu thành công
    private final MutableLiveData<Boolean> _isSaved = new MutableLiveData<>(false);
    public LiveData<Boolean> getIsSaved() { return _isSaved; }

    public SettingsViewModel(SettingsRepository settingsRepository, TtsRepository ttsRepository) {
        this.settingsRepository = settingsRepository;
        this.ttsRepository = ttsRepository;
        loadSettings();
    }

    private void loadSettings() {
        float speed = settingsRepository.getTtsSpeed();
        _currentSpeed.setValue(speed);
        ttsRepository.setSpeed(speed); // Khởi tạo tốc độ cho engine
    }

    // Khi người dùng kéo thanh trượt (Slider)
    public void updateSpeedTemporary(float speed) {
        _currentSpeed.setValue(speed);
    }

    // Khi người dùng bấm "Nghe thử"
    public void testVoice(float speed) {
        ttsRepository.setSpeed(speed);
        ttsRepository.speak("Xin chào, đây là tốc độ giọng nói bác vừa chọn.");
    }
    public void updateRetentionTemporary(int days) { _retentionDays.setValue(days); }
    // Khi người dùng bấm "Lưu cài đặt"
    public void saveSettings(float speed, int days) {
        settingsRepository.saveTtsSpeed(speed);
        settingsRepository.saveHistoryRetentionDays(days);
        ttsRepository.setSpeed(speed); // Chốt tốc độ mới cho toàn hệ thống
        _isSaved.setValue(true);
    }

    public void resetSaveState() {
        _isSaved.setValue(false);
    }
}