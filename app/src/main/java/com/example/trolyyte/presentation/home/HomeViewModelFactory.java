package com.example.trolyyte.presentation.home;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.trolyyte.di.AppContainer; // NHỚ IMPORT CÁI NÀY
import com.example.trolyyte.domain.repository.TtsRepository;
import com.example.trolyyte.domain.usecase.*;
import com.example.trolyyte.presentation.common.ResponseTextProvider;

public class HomeViewModelFactory implements ViewModelProvider.Factory {

    // 1. Thêm biến AppContainer vào đây
    private final AppContainer appContainer;
    private final ListenVoiceUseCase listenVoiceUseCase;
    private final ProcessTextUseCase processTextUseCase;
    private final HandleDialogueUseCase handleDialogueUseCase;
    private final SpeakResponseUseCase speakResponseUseCase;
    private final TtsRepository ttsRepository;
    private final ResponseTextProvider responseTextProvider;

    // 2. Thêm vào Constructor
    public HomeViewModelFactory(
            AppContainer appContainer,
            ListenVoiceUseCase listenVoiceUseCase,
            ProcessTextUseCase processTextUseCase,
            HandleDialogueUseCase handleDialogueUseCase,
            SpeakResponseUseCase speakResponseUseCase,
            TtsRepository ttsRepository,
            ResponseTextProvider responseTextProvider
    ) {
        this.appContainer = appContainer;
        this.listenVoiceUseCase = listenVoiceUseCase;
        this.processTextUseCase = processTextUseCase;
        this.handleDialogueUseCase = handleDialogueUseCase;
        this.speakResponseUseCase = speakResponseUseCase;
        this.ttsRepository = ttsRepository;
        this.responseTextProvider = responseTextProvider;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(HomeViewModel.class)) {
            // 3. Truyền thêm appContainer vào hàm tạo mới
            return (T) new HomeViewModel(
                    appContainer,
                    listenVoiceUseCase,
                    processTextUseCase,
                    handleDialogueUseCase,
                    speakResponseUseCase,
                    ttsRepository,
                    responseTextProvider
            );
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}