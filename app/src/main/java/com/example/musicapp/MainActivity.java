package com.example.musicapp;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    TextView tvTime, tvDuration;
    SeekBar seekBarTime;
    Button btnPlay;
    MediaPlayer musicPLayer;

    private void bindingView(){
        tvTime = findViewById(R.id.tvTime);
        tvDuration = findViewById(R.id.tvDuration);
        seekBarTime = findViewById(R.id.seekBarTime);
        btnPlay = findViewById(R.id.btnPlay);
    }

    private void bindingAction(){
        btnPlay.setOnClickListener(this);
    }

    private void setupSeekBar(){
        seekBarTime.setMax(musicPLayer.getDuration());
        seekBarTime.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean isFromUser) {
                if(isFromUser){
                    musicPLayer.seekTo(progress);
                    seekBar.setProgress(progress);
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }
    private void setupMediaPlayer() {
        musicPLayer = MediaPlayer.create(this, R.raw.test);
        musicPLayer.setLooping(true);
        musicPLayer.seekTo(0);
        //musicPLayer.start();
        String duration = millisecondsToString(musicPLayer.getDuration());
        tvDuration.setText(duration);
    }
    private void startProgressUpdater(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (musicPLayer != null){
                    if(musicPLayer.isPlaying()){
                        try {
                            final double current = musicPLayer.getCurrentPosition();
//                            double duration = musicPLayer.getDuration();
//                            final double position =(100.0/duration) * current;
                            final String elapsedTime = millisecondsToString((int) current);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    tvTime.setText(elapsedTime);
                                    seekBarTime.setProgress((int)current);
                                }
                            });


                            Thread.sleep(1000);

                        }catch (InterruptedException e){

                        }
                    }
                }
            }
        }).start();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        bindingView();
        bindingAction();
        setupSeekBar();
        setupMediaPlayer();
        startProgressUpdater();
    }

    public String millisecondsToString(int time){
        String elapsedTime = "";
        int minutes = time / 1000 / 60;
        int seconds = time / 1000 % 60;
        elapsedTime = minutes+":";
        if(seconds < 10){
            elapsedTime +="0";
        }
        elapsedTime += seconds;
        return elapsedTime;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btnPlay) {
            togglePlayPause();
        }
    }
    private void togglePlayPause() {
        if (musicPLayer.isPlaying()) {
            musicPLayer.pause();
            btnPlay.setBackgroundResource(R.drawable.ic_button_play);
        } else {
            musicPLayer.start();
            btnPlay.setBackgroundResource(R.drawable.ic_button_pause);
        }
    }
}