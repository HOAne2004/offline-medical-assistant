package com.example.trolyyte.domain.usecase;

import com.example.trolyyte.domain.model.NlpResult;

public interface ProcessTextUseCase {

    interface Callback {
        void onSuccess(NlpResult result);
        void onFailure(String message);
    }

    void execute(String inputText, Callback callback);
}