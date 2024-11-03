package com.example.musicapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class MediaControlReceiver extends BroadcastReceiver {
    public static final String ACTION_PLAY_PAUSE = "PLAY_PAUSE";
    public static final String ACTION_NEXT = "NEXT";
    public static final String ACTION_PREVIOUS = "PREVIOUS";
    public static final String ACTION_UPDATE_UI = "UPDATE_UI"; // Custom action to update UI in MainActivity

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null) return;

        // Handle playback actions and send UI update broadcast
        switch (action) {
            case ACTION_PLAY_PAUSE:
                // Toggle play/pause on your MediaPlayer instance
                // You might need to access MediaPlayer instance through a service or singleton
                togglePlayPause(context);
                break;
            case ACTION_NEXT:
                // Skip to the next track
                skipToNext(context);
                break;
            case ACTION_PREVIOUS:
                // Skip to the previous track
                skipToPrevious(context);
                break;
        }
    }

    private void togglePlayPause(Context context) {
        // Perform play/pause action on MediaPlayer
        // Example: mediaPlayer.pause() or mediaPlayer.start() based on current state

        // Send broadcast to MainActivity to update UI
        Intent updateUIIntent = new Intent(ACTION_UPDATE_UI);
        updateUIIntent.putExtra("action", ACTION_PLAY_PAUSE);
        LocalBroadcastManager.getInstance(context).sendBroadcast(updateUIIntent);
    }

    private void skipToNext(Context context) {
        // Perform next action on MediaPlayer

        // Send broadcast to MainActivity to update UI
        Intent updateUIIntent = new Intent(ACTION_UPDATE_UI);
        updateUIIntent.putExtra("action", ACTION_NEXT);
        LocalBroadcastManager.getInstance(context).sendBroadcast(updateUIIntent);
    }

    private void skipToPrevious(Context context) {
        // Perform previous action on MediaPlayer

        // Send broadcast to MainActivity to update UI
        Intent updateUIIntent = new Intent(ACTION_UPDATE_UI);
        updateUIIntent.putExtra("action", ACTION_PREVIOUS);
        LocalBroadcastManager.getInstance(context).sendBroadcast(updateUIIntent);
    }
}
