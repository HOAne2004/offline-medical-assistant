package com.example.trolyyte.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.trolyyte.domain.model.Appointment;

import java.util.List;

@Dao
public interface AppointmentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Appointment appointment);

    @Update
    void update(Appointment appointment);

    @Delete
    void delete(Appointment appointment);

    // Sắp xếp lịch khám tăng dần theo thời gian
    @Query("SELECT * FROM appointments ORDER BY timeMillis ASC")
    List<Appointment> getAllAppointments();

    @Query("SELECT * FROM appointments WHERE id = :id")
    Appointment getAppointmentById(String id);
}