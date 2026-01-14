package com.example.trolyyte.domain.usecase;

import com.example.trolyyte.domain.dialog.DialogueResult;
import com.example.trolyyte.domain.model.SpeakResult;

public interface  SpeakResponseUseCase {
    SpeakResult execute(DialogueResult dialogueResult);
}
