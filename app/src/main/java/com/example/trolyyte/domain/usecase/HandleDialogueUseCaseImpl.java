// domain/usecase/HandleDialogueUseCaseImpl.java
package com.example.trolyyte.domain.usecase;

import com.example.trolyyte.domain.dialog.DialogContext;
import com.example.trolyyte.domain.dialog.DialogueManager;
import com.example.trolyyte.domain.dialog.DialogueResult;
import com.example.trolyyte.domain.model.NlpResult;

public class HandleDialogueUseCaseImpl implements HandleDialogueUseCase {

    private final DialogueManager dialogueManager;

    public HandleDialogueUseCaseImpl(DialogueManager dialogueManager) {
        this.dialogueManager = dialogueManager;
    }

    @Override
    public DialogueResult execute(NlpResult nlpResult, DialogContext context) {
        return dialogueManager.handle(nlpResult, context);
    }
}
