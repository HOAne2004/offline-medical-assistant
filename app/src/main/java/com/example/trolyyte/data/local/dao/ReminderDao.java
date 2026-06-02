package com.example.trolyyte.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.trolyyte.domain.model.Reminder;

import java.util.List;

@Dao
public interface ReminderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Reminder reminder);

    @Update
    void update(Reminder reminder);

    @Delete
    void delete(Reminder reminder);

    @Query("SELECT * FROM reminders ORDER BY triggerAtMillis ASC")
    List<Reminder> getAllReminders();

    @Query("SELECT * FROM reminders WHERE id = :id")
    Reminder getReminderById(String id);
}
