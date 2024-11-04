package com.example.musicapp.NotificationService;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.support.v4.media.session.MediaSessionCompat;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.media.app.NotificationCompat.MediaStyle;

import com.example.musicapp.MainActivity;
import com.example.musicapp.MyApplication;
import com.example.musicapp.R;

public class MediaNotificationHelper {

    private static final int NOTIFICATION_ID = 1;

    private final Context context;
    private final MediaSessionCompat mediaSession;
    private final MediaPlayer mediaPlayer;

    public MediaNotificationHelper(Context context, MediaSessionCompat mediaSession, MediaPlayer mediaPlayer) {
        this.context = context;
        this.mediaSession = mediaSession;
        this.mediaPlayer = mediaPlayer;
    }

    public void showMediaNotification(String title, String artist) {

        // Create pending intents for media controls
        PendingIntent playPauseIntent = createMediaControlPendingIntent("PLAY_PAUSE");
        PendingIntent nextIntent = createMediaControlPendingIntent("NEXT");
        PendingIntent previousIntent = createMediaControlPendingIntent("PREVIOUS");

        // Build the media notification
        NotificationCompat.Builder notification = new NotificationCompat.Builder(context, MyApplication.CHANNEL_ID)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.img_notification))
                .setSmallIcon(R.drawable.ic_small_music) // Small icon for the notification
                .setContentTitle(title)
                .setContentText("Artist: " + artist)
                .setSound(null)
                .addAction(R.drawable.ic_back, "Previous", previousIntent) // Previous action
                .addAction(mediaPlayer.isPlaying() ? R.drawable.ic_pause : R.drawable.ic_play,
                        mediaPlayer.isPlaying() ? "Pause" : "Play", playPauseIntent) // Play/Pause action
                .addAction(R.drawable.ic_next, "Next", nextIntent) // Next action
                .setStyle(new MediaStyle()
                        .setMediaSession(mediaSession.getSessionToken()) // Link MediaSession
                        .setShowActionsInCompactView(0, 1, 2)) // Show Previous, Play/Pause, and Next in compact view
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        notification.setContentIntent(pendingIntent);

        // Show the notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) context, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 100);
        }
        notificationManager.notify(NOTIFICATION_ID, notification.build());
    }

    private PendingIntent createMediaControlPendingIntent(String action) {
        Intent intent = new Intent(context, MediaControlReceiver.class);
        intent.setAction(action);
        return PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE // Add FLAG_IMMUTABLE here
        );}
}
