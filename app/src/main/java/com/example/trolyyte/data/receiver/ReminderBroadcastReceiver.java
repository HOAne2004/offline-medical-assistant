package com.example.trolyyte.data.receiver;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.trolyyte.MedicalAssistantApplication;
import com.example.trolyyte.R;
import com.example.trolyyte.data.local.AppDatabase;
import com.example.trolyyte.di.AppContainer;
import com.example.trolyyte.domain.model.ReminderHistory;
import com.example.trolyyte.domain.model.ReminderTemplate;
import com.example.trolyyte.data.utils.MedicalTtsFormatter;
import com.example.trolyyte.data.utils.VietnameseTextNormalizer;

import java.util.concurrent.Executors;

public class ReminderBroadcastReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "REMINDER_CHANNEL";

    @Override
    public void onReceive(Context context, Intent intent) {
        final PendingResult pendingResult = goAsync();

        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "MyApp:ReminderWakeLock");
        wakeLock.acquire(8 * 1000L);

        String historyId = intent.getStringExtra("REMINDER_ID");
        String title = intent.getStringExtra("TITLE");
        String typeStr = intent.getStringExtra("TYPE");
        String instruction = intent.getStringExtra("INSTRUCTION");
        String alarmStage = intent.getStringExtra("ALARM_STAGE");
        int remindMinutes = intent.getIntExtra("REMIND_MINUTES", 0);

        // QUÉT DATABASE TRƯỚC KHI QUYẾT ĐỊNH RUNG CHUÔNG
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                if (historyId == null) { pendingResult.finish(); return; }

                AppDatabase db = AppDatabase.getDatabase(context.getApplicationContext());
                ReminderHistory history = db.reminderDao().getHistoryById(historyId);

                // KIỂM TRA ĐIỀU KIỆN DỪNG: Đã uống, Đã hủy, hoặc Bỏ lỡ thì im lặng
                if (history == null || history.isCompleted() || history.isMissed()) {
                    Log.d("ALARM_TEST", "Lịch đã hoàn thành hoặc bỏ lỡ. Im lặng hủy chuông.");
                    pendingResult.finish();
                    return;
                }

                // Nếu là nhịp "Đúng giờ" (EXACT), cập nhật DB thành TRIGGERED
                if ("EXACT".equals(alarmStage) &&
                        (history.getStatus() == ReminderHistory.Status.SCHEDULED || history.getStatus() == ReminderHistory.Status.SNOOZED)) {
                    history.setStatus(ReminderHistory.Status.TRIGGERED);
                    history.setActualTriggerTimeMillis(System.currentTimeMillis());
                    db.reminderDao().updateHistory(history);
                }

                // Chuyển lại về UI Thread để phát âm thanh & hiển thị
                new Handler(Looper.getMainLooper()).post(() -> {
                    try {
                        showNotification(context, title, typeStr, alarmStage);
                        playAlertSound(context);
                        playVoiceAnnouncement(context, title, instruction, typeStr, alarmStage, remindMinutes, pendingResult);
                    } catch (Exception e) {
                        Log.e("ALARM_ERROR", "Lỗi phát giao diện/âm thanh: " + e.getMessage());
                        pendingResult.finish();
                    }
                });

            } catch (Exception e) {
                Log.e("ALARM_ERROR", "Lỗi CSDL Receiver: " + e.getMessage());
                pendingResult.finish();
            }
        });
    }

    private void playAlertSound(Context context) {
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(context, notification);
            r.play();
        } catch (Exception e) {
            Log.e("ALARM_ERROR", "Lỗi rung chuông: " + e.getMessage());
        }
    }

    private void playVoiceAnnouncement(Context context, String rawTitle, String rawInstruction, String typeStr, String alarmStage, int remindMinutes, PendingResult pendingResult) {
        AppContainer container = ((MedicalAssistantApplication) context.getApplicationContext()).appContainer;

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            try {
                String cleanTitle = VietnameseTextNormalizer.normalizeForTts(rawTitle);
                String cleanInstruction = rawInstruction != null ? VietnameseTextNormalizer.normalizeForTts(rawInstruction) : "";
                cleanTitle = MedicalTtsFormatter.formatForTts(cleanTitle);
                cleanInstruction = MedicalTtsFormatter.formatForTts(cleanInstruction);

                StringBuilder speechText = new StringBuilder();

                // LẮP RÁP KỊCH BẢN GIỌNG NÓI DỰA VÀO STAGE
                if ("PRE".equals(alarmStage)) {
                    speechText.append("Dạ thưa bác, còn ").append(remindMinutes).append(" phút nữa là đến giờ ");
                } else {
                    speechText.append("Dạ thưa bác, đã đến giờ ");
                }

                if (ReminderTemplate.Type.MEDICINE.name().equals(typeStr)) {
                    speechText.append("uống ").append(cleanTitle).append(". ");
                } else {
                    speechText.append("làm ").append(cleanTitle).append(". ");
                }

                if (!cleanInstruction.isEmpty()) {
                    speechText.append("Bác nhớ ").append(cleanInstruction).append(" nhé.");
                }

                container.ttsRepository.speak(speechText.toString());
            } catch (Exception e) {
                Log.e("ALARM_ERROR", "Lỗi đọc TTS: " + e.getMessage());
            } finally {
                new Handler(Looper.getMainLooper()).postDelayed(pendingResult::finish, 5000);
            }
        }, 1000);
    }

    private void showNotification(Context context, String title, String typeStr, String alarmStage) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String channelName = "Y Tế Nhắc Nhở";

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_HIGH);
            channel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);
            manager.createNotificationChannel(channel);
        }

        int icon = (typeStr != null && typeStr.equals(ReminderTemplate.Type.MEDICINE.name()))
                ? R.drawable.ic_capsule : R.drawable.ic_health;
        int color = (typeStr != null && typeStr.equals(ReminderTemplate.Type.MEDICINE.name()))
                ? Color.parseColor("#1A73E8") : Color.parseColor("#F29900");

        Intent fullScreenIntent = new Intent(context, com.example.trolyyte.presentation.home.HomeActivity.class);
        fullScreenIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        android.app.PendingIntent fullScreenPendingIntent = android.app.PendingIntent.getActivity(context, 0,
                fullScreenIntent, android.app.PendingIntent.FLAG_UPDATE_CURRENT | android.app.PendingIntent.FLAG_IMMUTABLE);

        // Thay đổi tiêu đề thông báo theo giai đoạn
        String notifTitle = "PRE".equals(alarmStage) ? "Sắp đến giờ y lệnh!" : "Đến giờ y lệnh!";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(icon)
                .setContentTitle(notifTitle)
                .setContentText(title)
                .setColor(color)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setAutoCancel(true)
                .setVibrate(new long[]{0, 500, 200, 500})
                .setFullScreenIntent(fullScreenPendingIntent, true);

        manager.notify(title != null ? title.hashCode() : (int) System.currentTimeMillis(), builder.build());
    }
}