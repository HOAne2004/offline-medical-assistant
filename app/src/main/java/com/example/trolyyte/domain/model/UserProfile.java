package com.example.trolyyte.domain.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "user_profile")
public class UserProfile {

    @PrimaryKey
    @NonNull
    private Integer id = 1; // Luôn cố định là 1 để đảm bảo chỉ có duy nhất 1 hồ sơ trên thiết bị

    private String name;
    private int age;
    private String gender;
    private String bloodType;
    private String medicalHistory; // Tiền sử bệnh
    private String emergencyPhone;

    // Constructor mặc định cho Room
    public UserProfile() {
    }

    // Constructor đầy đủ cho Business Logic
    @Ignore
    public UserProfile(String name, int age, String gender, String bloodType, String medicalHistory, String emergencyPhone) {
        this.id = 1;
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.bloodType = bloodType;
        this.medicalHistory = medicalHistory;
        this.emergencyPhone = emergencyPhone;
    }

    @NonNull
    public Integer getId() { return id; }
    public void setId(@NonNull Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getBloodType() { return bloodType; }
    public void setBloodType(String bloodType) { this.bloodType = bloodType; }

    public String getMedicalHistory() { return medicalHistory; }
    public void setMedicalHistory(String medicalHistory) { this.medicalHistory = medicalHistory; }
    public String getEmergencyPhone() { return emergencyPhone; }
    public void setEmergencyPhone(String emergencyPhone) { this.emergencyPhone = emergencyPhone; }
}