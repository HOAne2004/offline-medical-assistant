package com.example.trolyyte.data.local;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.trolyyte.data.local.dao.ReminderDao;
import com.example.trolyyte.domain.model.Reminder;
import com.example.trolyyte.domain.model.ReminderTypeConverters;

@Database(entities = {Reminder.class}, version = 1, exportSchema = false)
@TypeConverters({ReminderTypeConverters.class})
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase INSTANCE;

    public abstract ReminderDao reminderDao();

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "tro_ly_y_te_db")
                            .allowMainThreadQueries() // Lưu ý: Chỉ dùng cho mục đích NCKH/Demo nhanh, Production nên dùng Thread riêng
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
