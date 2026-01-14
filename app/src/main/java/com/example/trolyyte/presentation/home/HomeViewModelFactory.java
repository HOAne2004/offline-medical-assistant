package com.example.trolyyte.presentation.home;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.trolyyte.domain.repository.TtsRepository;
import com.example.trolyyte.domain.usecase.HandleDialogueUseCase;
import com.example.trolyyte.domain.usecase.ListenVoiceUseCase;
import com.example.trolyyte.domain.usecase.ProcessTextUseCase;
import com.example.trolyyte.domain.usecase.SpeakResponseUseCase;
import com.example.trolyyte.presentation.common.ResponseTextProvider;

public class HomeViewModelFactory implements ViewModelProvider.Factory {

    private final ListenVoiceUseCase listenVoiceUseCase;
    private final ProcessTextUseCase processTextUseCase;
    private final HandleDialogueUseCase handleDialogueUseCase;
    private final SpeakResponseUseCase speakResponseUseCase;
    private final TtsRepository ttsRepository;
    private final ResponseTextProvider responseTextProvider;

    public HomeViewModelFactory(
            ListenVoiceUseCase listenVoiceUseCase,
            ProcessTextUseCase processTextUseCase,
            HandleDialogueUseCase handleDialogueUseCase,
            SpeakResponseUseCase speakResponseUseCase,
            TtsRepository ttsRepository,
            ResponseTextProvider responseTextProvider
    ) {
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
            //noinspection unchecked
            return (T) new HomeViewModel(
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