package com.example.trolyyte.data.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import com.example.trolyyte.data.receiver.ReminderBroadcastReceiver;
import com.example.trolyyte.domain.model.Reminder;

public class ReminderAlarmScheduler {
    private final Context context;
    private final AlarmManager alarmManager;

    public ReminderAlarmScheduler(Context context) {
        this.context = context;
        this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    public void schedule(Reminder reminder) {
        Intent intent = new Intent(context, ReminderBroadcastReceiver.class);
        intent.putExtra("REMINDER_ID", reminder.getId());
        intent.putExtra("TITLE", reminder.getTitle());
        intent.putExtra("TYPE", reminder.getType().name());

        long triggerTime = reminder.getTriggerAtMillis() - (reminder.getRemindMinutes() * 60 * 1000);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                reminder.getId().hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (alarmManager != null) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
            );
        }
    }

    public void cancel(String reminderId) {
        Intent intent = new Intent(context, ReminderBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                reminderId.hashCode(),
                intent,
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
        );

        if (pendingIntent != null && alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }
}