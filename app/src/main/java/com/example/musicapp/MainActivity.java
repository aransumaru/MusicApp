package com.example.musicapp;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    TextView tvTime, tvDuration, tvTitle, tvArtist;
    SeekBar seekBarTime, seekBarVolume;
    Button btnPlay, btnDownVolume, btnUpVolume;
    MediaPlayer musicPLayer;
    private void bindingView() {
        tvTime = findViewById(R.id.tvTime);
        tvDuration = findViewById(R.id.tvDuration);
        tvTitle = findViewById(R.id.tvTitle);
        tvArtist = findViewById(R.id.tvArtist);
        seekBarTime = findViewById(R.id.seekBarTime);
        seekBarVolume = findViewById(R.id.seekBarVolume);
        btnPlay = findViewById(R.id.btnPlay);
        btnDownVolume = findViewById(R.id.btnDownVolume);
        btnUpVolume = findViewById(R.id.btnUpVolume);
        IntentView();
    }
    private void IntentView(){
        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        String artist = intent.getStringExtra("artist");

        if (title != null) {
            tvTitle.setText(title);
        }
        if (artist != null) {
            tvArtist.setText(artist);
        }
    }
    private void bindingAction(){
        btnPlay.setOnClickListener(this::onBtnPlayClick);
        btnDownVolume.setOnClickListener(this::onBtnVolumeDownClick);
        btnUpVolume.setOnClickListener(this::onBtnVolumeUpClick);
    }

    private void onBtnVolumeUpClick(View view) {
        musicPLayer.setVolume(1.0f, 1.0f);
        seekBarVolume.setProgress(100);
    }

    private void onBtnVolumeDownClick(View view) {
        musicPLayer.setVolume(0.0f, 0.0f);
        seekBarVolume.setProgress(0);
    }

    private void onBtnPlayClick(View view) {
        if(view.getId()==R.id.btnPlay){
            if(musicPLayer.isPlaying()){
                //is playing
                musicPLayer.pause();
                btnPlay.setBackgroundResource(R.drawable.ic_button_play);
            }else{
                //on pause
                musicPLayer.start();
                btnPlay.setBackgroundResource(R.drawable.ic_button_pause);
            }
        }
    }

    private void setupMediaPlayer() {
        musicPLayer = new MediaPlayer();
        String path = getIntent().getStringExtra("path");

        if (path != null) {
            try {
                musicPLayer.setDataSource(path);
                musicPLayer.prepare(); // Prepare the player
                musicPLayer.setLooping(true);
                musicPLayer.seekTo(0);

                // Bắt đầu phát bài hát
                //musicPLayer.start();

                String duration = millisecondsToString(musicPLayer.getDuration());
                tvDuration.setText(duration);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Unable to set data source: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            //Toast.makeText(this, "Path is null", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupSeekBar() {
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
        musicPLayer.setVolume(0.5f, 0.5f);
        seekBarVolume.setProgress(50);
        seekBarVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean isFromUser) {
                float volume = progress / 100f;
                musicPLayer.setVolume(volume, volume);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }
    private void startSeekBarUpdateThread() {
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

        Song song = (Song) getIntent().getSerializableExtra("song");
        if (song != null) {
            tvTitle.setText(song.getTitle());
            tvArtist.setText(song.getArtist());
        }
        bindingView();
        setupMediaPlayer();
        bindingAction();
        setupSeekBar();
        startSeekBarUpdateThread();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.option_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getMenuInflater().inflate(R.menu.option_menu, menu);
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.opt_listmusic) {
            Toast.makeText(this, "opt_listmusic_contextmenu", Toast.LENGTH_SHORT).show();
        } else if (item.getItemId() == R.id.opt_main) {
            Toast.makeText(this, "opt_main_contextmenu", Toast.LENGTH_SHORT).show();
        }
        return super.onContextItemSelected(item);
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.opt_listmusic) {
            //Toast.makeText(this, "opt_listmusic", Toast.LENGTH_SHORT).show();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ListMusicFragment())
                    .addToBackStack(null)
                    .commit();
            //Toast.makeText(this, "on open fragment", Toast.LENGTH_SHORT).show();

            return true;
        } else if (item.getItemId() == R.id.opt_main) {
            //Toast.makeText(this, "opt_main", Toast.LENGTH_SHORT).show();
            Intent mainIntent = new Intent(this, MainActivity.class);
            startActivity(mainIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onClick(View view) {
        if(view.getId()==R.id.btnPlay){
            if(musicPLayer.isPlaying()){
                //is playing
                musicPLayer.pause();
                btnPlay.setBackgroundResource(R.drawable.ic_button_play);
            }else{
                //on pause
                musicPLayer.start();
                btnPlay.setBackgroundResource(R.drawable.ic_button_pause);
            }
        }
    }
}