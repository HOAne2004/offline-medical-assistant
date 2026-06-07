package com.example.trolyyte.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.trolyyte.domain.model.UserProfile;

@Dao
public interface UserProfileDao {

    // Nếu đã tồn tại id = 1, ghi đè toàn bộ thông tin mới lên
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdateProfile(UserProfile profile);

    // Lấy bản ghi hồ sơ duy nhất
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    UserProfile getUserProfile();
}