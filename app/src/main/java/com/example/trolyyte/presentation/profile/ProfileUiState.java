package com.example.trolyyte.presentation.profile;

public class ProfileUiState {
    private final String fullName;
    private final int age;
    private final String gender;
    private final String medicalHistory;
    private final String currentMedications;
    private final String reminderMessage;

    public ProfileUiState(String fullName, int age, String gender,
                          String medicalHistory, String currentMedications,
                          String reminderMessage) {
        this.fullName = fullName;
        this.age = age;
        this.gender = gender;
        this.medicalHistory = medicalHistory;
        this.currentMedications = currentMedications;
        this.reminderMessage = reminderMessage;
    }

    // Getters
    public String getFullName() { return fullName; }
    public int getAge() { return age; }
    public String getGender() { return gender; }
    public String getMedicalHistory() { return medicalHistory; }
    public String getCurrentMedications() { return currentMedications; }
    public String getReminderMessage() { return reminderMessage; }

}
