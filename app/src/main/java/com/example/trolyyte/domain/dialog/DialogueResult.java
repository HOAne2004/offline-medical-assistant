package com.example.trolyyte.domain.dialog;

public class DialogueResult {
    private final DialogueAction action;
    private final DialogContext updatedContext;

    public DialogueResult(DialogueAction action, DialogContext updatedContext) {
        this.action = action;
        this.updatedContext = updatedContext;
    }

    public DialogueAction getAction() { return action; }
    public DialogContext getUpdatedContext() { return updatedContext; }
}