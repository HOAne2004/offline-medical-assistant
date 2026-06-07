package com.example.trolyyte.domain.model.actions;

import com.example.trolyyte.domain.model.NlpResult;

public class MedicationAction implements IntentAction {
    private final NlpResult result;

    public MedicationAction(NlpResult result) {
        this.result = result;
    }

    public NlpResult getResult() {
        return result;
    }
}