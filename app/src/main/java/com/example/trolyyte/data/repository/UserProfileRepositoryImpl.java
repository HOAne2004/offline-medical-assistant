package com.example.trolyyte.data.repository;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.trolyyte.domain.repository.UserProfileRepository;

public class UserProfileRepositoryImpl implements UserProfileRepository {

 // Tên của file lưu trữ ngầm trong Android
 private static final String PREF_NAME = "MedicalAssistantProfile";

 // Các Key để lưu dữ liệu
 private static final String KEY_NAME = "user_name";
 private static final String KEY_PHONE = "emergency_phone";
 private static final String KEY_HISTORY = "medical_history";

 private final SharedPreferences sharedPreferences;

 // Constructor yêu cầu Context để mở SharedPreferences
 public UserProfileRepositoryImpl(Context context) {
  this.sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
 }

 @Override
 public void saveProfile(String name, String emergencyPhone, String medicalHistory) {
  // Mở editor và lưu dữ liệu, dùng apply() để lưu bất đồng bộ (tránh giật lag UI)
  sharedPreferences.edit()
          .putString(KEY_NAME, name)
          .putString(KEY_PHONE, emergencyPhone)
          .putString(KEY_HISTORY, medicalHistory)
          .apply();
 }

 @Override
 public String getUserName() {
  // Trả về "Ông/Bà" làm mặc định nếu người dùng chưa nhập tên
  return sharedPreferences.getString(KEY_NAME, "Ông/Bà");
 }

 @Override
 public String getEmergencyPhone() {
  // Trả về chuỗi rỗng nếu chưa cài số khẩn cấp
  return sharedPreferences.getString(KEY_PHONE, "");
 }

 @Override
 public String getMedicalHistory() {
  return sharedPreferences.getString(KEY_HISTORY, "");
 }
}