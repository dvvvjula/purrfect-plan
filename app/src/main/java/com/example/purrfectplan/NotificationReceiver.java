package com.example.purrfectplan;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

public class NotificationReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "task_channel";

    @Override
    public void onReceive(Context context, Intent intent) {
        String title = intent.getStringExtra("task_title");
        String description = intent.getStringExtra("task_description");
        String timeStr = intent.getStringExtra("task_time");
        int taskId = intent.getIntExtra("task_id", 0);

        createNotificationChannel(context);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)  // Tymczasowa
                .setContentTitle("Meow! " + (title != null ? title : "Task"))
                .setContentText((description != null ? description : "") + " | " + (timeStr != null ? timeStr : ""))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(false)
                .setContentIntent(createPendingIntent(context, taskId));

        // BEZPIECZNY NOTIFY - usuwa błąd!
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            // Log lub ignoruj
            return;
        }
        notificationManager.notify(taskId, builder.build());
    }

    private PendingIntent createPendingIntent(Context context, int taskId) {
        Intent intent = new Intent(context, EditTaskActivity.class);
        intent.putExtra("taskId", taskId);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return PendingIntent.getActivity(context, taskId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Task Reminders",
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Purrfect task notifications 🐾");
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 300, 500});

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }
}

