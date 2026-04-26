package com.example.trolyyte.presentation.profile;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.example.trolyyte.domain.repository.SettingsRepository;
import com.example.trolyyte.domain.repository.TtsRepository;
public class SettingsViewModelFactory implements ViewModelProvider.Factory
{
    private final SettingsRepository settingsRepository;
    private final TtsRepository ttsRepository;

    public SettingsViewModelFactory(SettingsRepository settingsRepository, TtsRepository ttsRepository)
    {
        this.settingsRepository = settingsRepository;
        this.ttsRepository = ttsRepository;
    }
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(SettingsViewModel.class)) {
            return (T) new SettingsViewModel(settingsRepository, ttsRepository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
