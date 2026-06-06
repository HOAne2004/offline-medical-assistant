package com.example.trolyyte.data.local;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.trolyyte.data.local.dao.AppointmentDao;
import com.example.trolyyte.data.local.dao.ReminderDao;
import com.example.trolyyte.data.local.dao.UserProfileDao;
import com.example.trolyyte.domain.model.AppTypeConverters;
import com.example.trolyyte.domain.model.Appointment; // Bổ sung import
import com.example.trolyyte.domain.model.ReminderHistory;
import com.example.trolyyte.domain.model.ReminderTemplate;
import com.example.trolyyte.domain.model.UserProfile; // Bổ sung import

@Database(entities = {ReminderTemplate.class, ReminderHistory.class, UserProfile.class, Appointment.class}, version = 3, exportSchema = false)
@TypeConverters({AppTypeConverters.class})
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase INSTANCE;

    public abstract ReminderDao reminderDao();
    public abstract UserProfileDao userProfileDao();
    public abstract AppointmentDao appointmentDao();

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "tro_ly_y_te_db")
                            .allowMainThreadQueries()
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}