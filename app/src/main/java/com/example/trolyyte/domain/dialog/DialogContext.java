package com.example.trolyyte.domain.dialog;

import com.example.trolyyte.domain.model.DialogState;

public class DialogContext {

    private DialogState currentState = DialogState.IDLE;

    private String medicineName;
    private String reminderTime;

    public DialogState getCurrentState() {
        return currentState;
    }

    public void setCurrentState(DialogState currentState) {
        this.currentState = currentState;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public void setMedicineName(String medicineName) {
        this.medicineName = medicineName;
    }

    public String getReminderTime() {
        return reminderTime;
    }

    public void setReminderTime(String reminderTime) {
        this.reminderTime = reminderTime;
    }

    public boolean isReminderInfoComplete() {
        return medicineName != null && reminderTime != null;
    }

    public void reset() {
        currentState = DialogState.IDLE;
        medicineName = null;
        reminderTime = null;
    }
}
