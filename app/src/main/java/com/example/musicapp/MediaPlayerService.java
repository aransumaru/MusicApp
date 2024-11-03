package com.example.musicapp;

import android.content.Context;
import android.media.MediaPlayer;

public class MediaPlayerService {
    private MediaPlayer mediaPlayer;
    private Context context;

    public MediaPlayerService(Context context, MediaPlayer mediaPlayer) {
        this.context = context;
        this.mediaPlayer = mediaPlayer;
    }
}
