package com.example.trolyyte.presentation.nlu;

import com.example.trolyyte.domain.model.*;
import com.example.trolyyte.domain.model.actions.AppointmentAction;
import com.example.trolyyte.domain.model.actions.EmergencyAction;
import com.example.trolyyte.domain.model.actions.IntentAction;
import com.example.trolyyte.domain.model.actions.MedicationAction;
import com.example.trolyyte.domain.model.actions.SpeakAction;

public class IntentRouter {
    public IntentAction route(NlpResult result) {
        switch (result.getIntent()) {
            case SET_OR_UPDATE_MEDICATION:
            case CANCEL_MEDICATION:
            case CHECK_MEDICATION:
                return new MedicationAction(result);

            case SET_OR_UPDATE_APPOINTMENT:
            case CANCEL_APPOINTMENT:
            case CHECK_APPOINTMENT:
                return new AppointmentAction(result);

            case REQUEST_EMERGENCY:
                return new EmergencyAction();

            case ASK_DATE_TIME:
            case ASK_HELP:
                return new SpeakAction("Dạ, cháu đang hỗ trợ bác đây ạ.");

            default:
                return new SpeakAction("Bác ơi, cháu chưa hiểu ý bác. Bác nói lại giúp cháu nhé.");
        }
    }
}