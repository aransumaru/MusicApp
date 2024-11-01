package com.example.musicapp;

import android.app.Application;

import androidx.core.app.NotificationManagerCompat;

public class MyApplication extends Application {
    @Override
    public void onTerminate() {
        super.onTerminate();
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.cancel(1); // Hủy thông báo có ID 1
    }
}

