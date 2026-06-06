package com.example.trolyyte.domain.model.actions;

import com.example.trolyyte.domain.model.NlpResult;

public class AppointmentAction implements IntentAction {
    private final NlpResult result;

    public AppointmentAction(NlpResult result) {
        this.result = result;
    }

    public NlpResult getResult() {
        return result;
    }
}