package com.example.trolyyte.data.receiver;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import androidx.core.app.NotificationCompat;
import com.example.trolyyte.R;
import com.example.trolyyte.data.local.AppDatabase;
import com.example.trolyyte.domain.model.Reminder;
import java.util.concurrent.Executors;

public class ReminderBroadcastReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "REMINDER_CHANNEL";

    @Override
    public void onReceive(Context context, Intent intent) {
        String id = intent.getStringExtra("REMINDER_ID");
        String title = intent.getStringExtra("TITLE");
        String typeStr = intent.getStringExtra("TYPE");

        showNotification(context, title, typeStr);

        // Update database offline
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getDatabase(context);
            Reminder reminder = db.reminderDao().getReminderById(id);
            if (reminder != null) {
                reminder.setStatus(Reminder.Status.TRIGGERED);
                db.reminderDao().update(reminder);
            }
        });
    }

    private void showNotification(Context context, String title, String typeStr) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Y Tế Nhắc Nhở", NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(channel);
        }

        int icon = typeStr.equals("MEDICINE") ? R.drawable.ic_medicine : R.drawable.ic_calendar;
        int color = typeStr.equals("MEDICINE") ? Color.parseColor("#1A73E8") : Color.parseColor("#F29900");

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(icon)
                .setContentTitle("Lịch nhắc y tế")
                .setContentText(title)
                .setColor(color)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        manager.notify(title.hashCode(), builder.build());
    }
}