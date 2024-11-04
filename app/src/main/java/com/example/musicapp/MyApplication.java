package com.example.musicapp;

import android.app.Activity;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;

public class MyApplication extends Application {
    public static final String CHANNEL_ID = "CHANNEL_MUNIQUE";

    @Override
    public void onCreate() {
        super.onCreate();
        // Khởi tạo các thành phần cần thiết cho ứng dụng của bạn ở đây
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "MUnique channel",
                    NotificationManager.IMPORTANCE_NONE
            );
            channel.setSound(null, null);
            channel.enableVibration(false);

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
            Log.d("MyApplication", "Notification channel created");
        }
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.cancel(1); // Hủy thông báo có ID 1
    }
}

