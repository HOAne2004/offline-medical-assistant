package com.example.trolyyte.data.repository;

import com.example.trolyyte.data.local.dao.UserProfileDao;
import com.example.trolyyte.domain.model.UserProfile;
import com.example.trolyyte.domain.repository.UserProfileRepository;

public class UserProfileRepositoryImpl implements UserProfileRepository {

 private final UserProfileDao userProfileDao;

 // Yêu cầu Dao thay vì Context
 public UserProfileRepositoryImpl(UserProfileDao userProfileDao) {
  this.userProfileDao = userProfileDao;
 }

 @Override
 public UserProfile getProfile() {
  UserProfile profile = userProfileDao.getUserProfile();

  // Trả về dữ liệu mặc định nếu chưa có hồ sơ trong DB
  if (profile == null) {
   profile = new UserProfile();
   profile.setName("Ông/Bà"); // Giữ lại logic mặc định như code cũ của bạn
  }
  return profile;
 }

 @Override
 public void saveProfile(UserProfile profile) {
  profile.setId(1); // Đảm bảo luôn ghi đè vào id 1
  userProfileDao.insertOrUpdateProfile(profile);
 }
}