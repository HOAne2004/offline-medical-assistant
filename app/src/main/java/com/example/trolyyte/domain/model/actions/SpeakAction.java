package com.example.trolyyte.domain.model.actions;

public class SpeakAction implements IntentAction {
    private final String message;

    public SpeakAction(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}