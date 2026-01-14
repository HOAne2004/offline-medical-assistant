package com.example.trolyyte.domain.usecase;

import com.example.trolyyte.domain.dialog.DialogContext;
import com.example.trolyyte.domain.dialog.DialogueResult;
import com.example.trolyyte.domain.model.NlpResult;

public interface  HandleDialogueUseCase {
    DialogueResult execute(NlpResult nlpResult, DialogContext context);

}
