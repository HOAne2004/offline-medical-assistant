package com.example.trolyyte.presentation.profile;

public class ProfileUiState {
    private final String name;
    private final String emergencyPhone;
    private final String medicalHistory;
    private final boolean isLoading;
    private final boolean isSaved;
    private final String errorMessage;

    public ProfileUiState(String name, String emergencyPhone, String medicalHistory, boolean isLoading, boolean isSaved, String errorMessage) {
        this.name = name;
        this.emergencyPhone = emergencyPhone;
        this.medicalHistory = medicalHistory;
        this.isLoading = isLoading;
        this.isSaved = isSaved;
        this.errorMessage = errorMessage;
    }

    public static ProfileUiState idle() {
        return new ProfileUiState("", "", "", false, false, null);
    }

    public String getName() { return name; }
    public String getEmergencyPhone() { return emergencyPhone; }
    public String getMedicalHistory() { return medicalHistory; }
    public boolean isLoading() { return isLoading; }
    public boolean isSaved() { return isSaved; }
    public String getErrorMessage() { return errorMessage; }

    public ProfileUiState copyWith(String name, String emergencyPhone, String medicalHistory, boolean isLoading, boolean isSaved, String errorMessage) {
        return new ProfileUiState(
                name != null ? name : this.name,
                emergencyPhone != null ? emergencyPhone : this.emergencyPhone,
                medicalHistory != null ? medicalHistory : this.medicalHistory,
                isLoading,
                isSaved,
                errorMessage
        );
    }
}
