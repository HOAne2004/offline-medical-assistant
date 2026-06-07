package com.example.trolyyte.data.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.example.trolyyte.data.receiver.ReminderBroadcastReceiver;
import com.example.trolyyte.domain.model.ReminderHistory;
import com.example.trolyyte.domain.model.ReminderTemplate;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ReminderAlarmScheduler {
    private final Context context;
    private final AlarmManager alarmManager;

    public ReminderAlarmScheduler(Context context) {
        this.context = context;
        this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    public void schedule(ReminderHistory history, ReminderTemplate template) {
        long exactTime = history.getScheduledTimeMillis();
        int remindMinutes = template.getRemindMinutes();
        long preTime = exactTime - (remindMinutes * 60 * 1000L);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());

        // 1. TẠO BÁO THỨC "NHẮC TRƯỚC" (PRE-REMINDER)
        if (remindMinutes > 0 && preTime >= System.currentTimeMillis()) {
            Intent preIntent = new Intent(context, ReminderBroadcastReceiver.class);
            preIntent.putExtra("REMINDER_ID", history.getId());
            preIntent.putExtra("TITLE", template.getTitle());
            preIntent.putExtra("TYPE", template.getType().name());
            preIntent.putExtra("INSTRUCTION", template.getInstruction() != null ? template.getInstruction() : "");
            preIntent.putExtra("ALARM_STAGE", "PRE"); // Đánh dấu là nhắc trước
            preIntent.putExtra("REMIND_MINUTES", remindMinutes);

            PendingIntent prePendingIntent = PendingIntent.getBroadcast(
                    context,
                    (history.getId() + "_PRE").hashCode(), // Sinh mã riêng biệt cho Nhắc trước
                    preIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            setExactAlarm(preTime, prePendingIntent);
            Log.d("ALARM_TEST", "[PRE] Nhắc trước cài vào lúc: " + sdf.format(new Date(preTime)));
        }

        // 2. TẠO BÁO THỨC "ĐÚNG GIỜ" (EXACT-REMINDER)
        if (exactTime >= System.currentTimeMillis()) {
            Intent exactIntent = new Intent(context, ReminderBroadcastReceiver.class);
            exactIntent.putExtra("REMINDER_ID", history.getId());
            exactIntent.putExtra("TITLE", template.getTitle());
            exactIntent.putExtra("TYPE", template.getType().name());
            exactIntent.putExtra("INSTRUCTION", template.getInstruction() != null ? template.getInstruction() : "");
            exactIntent.putExtra("ALARM_STAGE", "EXACT"); // Đánh dấu là đúng giờ

            PendingIntent exactPendingIntent = PendingIntent.getBroadcast(
                    context,
                    history.getId().hashCode(), // Sinh mã riêng biệt cho Đúng giờ
                    exactIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            setExactAlarm(exactTime, exactPendingIntent);
            Log.d("ALARM_TEST", "[EXACT] Đúng giờ cài vào lúc: " + sdf.format(new Date(exactTime)));
        }
    }

    private void setExactAlarm(long triggerTime, PendingIntent pendingIntent) {
        if (alarmManager != null) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
                    } else {
                        alarmManager.setWindow(AlarmManager.RTC_WAKEUP, triggerTime, 60000, pendingIntent);
                    }
                } else {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
                }
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
    }

    public void cancel(String historyId) {
        // Hủy báo thức EXACT
        Intent exactIntent = new Intent(context, ReminderBroadcastReceiver.class);
        PendingIntent exactPI = PendingIntent.getBroadcast(context, historyId.hashCode(), exactIntent, PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);
        if (exactPI != null && alarmManager != null) { alarmManager.cancel(exactPI); exactPI.cancel(); }

        // Hủy báo thức PRE
        Intent preIntent = new Intent(context, ReminderBroadcastReceiver.class);
        PendingIntent prePI = PendingIntent.getBroadcast(context, (historyId + "_PRE").hashCode(), preIntent, PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);
        if (prePI != null && alarmManager != null) { alarmManager.cancel(prePI); prePI.cancel(); }
    }
}