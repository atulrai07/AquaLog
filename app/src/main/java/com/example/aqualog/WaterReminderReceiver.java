package com.example.aqualog;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;

import androidx.core.app.NotificationCompat;

import com.example.aqualog.data.WaterDatabase;

import java.util.Calendar;

public class WaterReminderReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "WATER_REMINDER_CHANNEL";
    private static final int WATER_GOAL_ML = 2000;

    @Override
    public void onReceive(Context context, Intent intent) {
        new Thread(() -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            long startOfDay = calendar.getTimeInMillis();
            int totalMl = WaterDatabase.getInstance(context)
                    .waterLogDao()
                    .getTotalForDay(startOfDay);

            if (totalMl < WATER_GOAL_ML) {
                showNotification(context, totalMl);
            }
        }).start();
    }

    private void showNotification(Context context, int totalMl) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Create channel for Android O+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Water Intake Reminders",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Reminds you to complete your daily water intake");
            channel.enableLights(true);
            channel.setLightColor(Color.BLUE);
            notificationManager.createNotificationChannel(channel);
        }

        // Intent to open AddNowActivity
        Intent intent = new Intent(context, AddNowActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.outline_asterisk_24) // Replace with your icon
                .setContentTitle("Hydration Reminder 💧")
                .setContentText("You’ve logged " + totalMl + "ml. Target is 2000ml.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }
}
