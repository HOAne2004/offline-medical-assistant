package com.example.trolyyte.domain.dialog;

import com.example.trolyyte.domain.model.NlpResult;

public interface DialogueManager {

    DialogueResult handle(NlpResult nlpResult, DialogContext context);
}
