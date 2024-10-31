package com.example.musicapp;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String CHAT_FRAGMENT_TAG = "CHAT_FRAGMENT";

    TextView tvTime, tvDuration, tvTitle, tvArtist;
    SeekBar seekBarTime, seekBarVolume;
    Button btnPlay, btnDownVolume, btnUpVolume, btnMain, btnListMusic;
    private ListMusicFragment listMusicFragment;
    private ChatFragment chatFragment;
    private ImageView chatBubble;
    private boolean isListMusicVisible = false;
    MediaPlayer musicPlayer;
    private Song currentSong;

    // Phương thức bindingView
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
        btnMain = findViewById(R.id.btnMain);
        btnListMusic = findViewById(R.id.btnListMusic);
        chatBubble = findViewById(R.id.chat_bubble);
    }

    // Phương thức bindingAction
    private void bindingAction() {
        btnPlay.setOnClickListener(this::onBtnPlayClick);
        btnDownVolume.setOnClickListener(this::onBtnVolumeDownClick);
        btnUpVolume.setOnClickListener(this::onBtnVolumeUpClick);
        btnMain.setOnClickListener(this::onBtnMainClick);
        btnListMusic.setOnClickListener(this::onBtnListMusicClick);
        chatBubble.setOnClickListener(this::onBtnChatBubbleClick);
    }

    private void onBtnChatBubbleClick(View view) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment chatFragment = fragmentManager.findFragmentByTag(CHAT_FRAGMENT_TAG);

        if (chatFragment != null && chatFragment.isVisible()) {
            fragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.scale_in, R.anim.scale_out) // Set animations for popping
                    .remove(chatFragment)
                    .commit();
        } else {
            // Create a new instance of ChatFragment
            Fragment newChatFragment = new ChatFragment();
            fragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.scale_in, R.anim.scale_out) // Set animations for adding
                    .replace(R.id.fragment_container, newChatFragment, CHAT_FRAGMENT_TAG)
                    .addToBackStack(null)
                    .commit();
        }
    }

    public Song getCurrentSong() {
        return currentSong; // Trả về bài hát hiện tại
    }

    // Phương thức cập nhật giao diện với bài hát
    public void updateUIWithSong(Song song) {
        if (song != null) {
            if (currentSong != null && currentSong.getPath().equals(song.getPath())) {
                Log.d("MusicPlayer", "Song is already playing: " + song.getTitle());
                return;
            }

            currentSong = song;

            tvTitle.setText(song.getTitle());
            tvArtist.setText(song.getArtist());
            setupMediaPlayer(song.getPath());
        }
    }


    // Phương thức thiết lập MediaPlayer
    private void setupMediaPlayer(String path) {
        if (musicPlayer != null) {
            stopMusic(); // Dừng nhạc nếu đang phát
        }
        musicPlayer = new MediaPlayer();
        if (path != null) {
            try {
                musicPlayer.setDataSource(path);
                musicPlayer.prepare();
                musicPlayer.setLooping(true);
                musicPlayer.start();
                btnPlay.setBackgroundResource(R.drawable.ic_button_pause);
                String duration = millisecondsToString(musicPlayer.getDuration());
                tvDuration.setText(duration);


                setupSeekBar();
                startSeekBarUpdateThread();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Unable to set data source: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }


    // Phương thức dừng nhạc
    void stopMusic() {
        if (musicPlayer != null) {
            if (musicPlayer.isPlaying()) {
                musicPlayer.stop();
                musicPlayer.release();
                musicPlayer = null;
                Log.d("MusicPlayer", "Music stopped.");
                btnPlay.setBackgroundResource(R.drawable.ic_button_play);
            }
        }
    }

    // Phương thức xử lý nút Play
    private void onBtnPlayClick(View view) {
        if (musicPlayer != null) {
            if (musicPlayer.isPlaying()) {
                musicPlayer.pause();
                btnPlay.setBackgroundResource(R.drawable.ic_button_play);
            } else {
                musicPlayer.start();
                btnPlay.setBackgroundResource(R.drawable.ic_button_pause);
            }
        }
    }

    // Phương thức xử lý nút tăng âm lượng
    private void onBtnVolumeUpClick(View view) {
        if (musicPlayer != null) {
            musicPlayer.setVolume(1.0f, 1.0f);
            seekBarVolume.setProgress(100);
        }
    }

    // Phương thức xử lý nút giảm âm lượng
    private void onBtnVolumeDownClick(View view) {
        if (musicPlayer != null) {
            musicPlayer.setVolume(0.0f, 0.0f);
            seekBarVolume.setProgress(0);
        }
    }

    // Phương thức xử lý nút Main
    private void onBtnMainClick(View view) {
        if (isListMusicVisible && listMusicFragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .hide(listMusicFragment)
                    .commit();
            isListMusicVisible = false;
        }
    }

    // Phương thức xử lý nút danh sách nhạc
    private void onBtnListMusicClick(View view) {
        if (listMusicFragment == null) {
            listMusicFragment = new ListMusicFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragment_container, listMusicFragment)
                    .commit();
        }
        getSupportFragmentManager()
                .beginTransaction()
                .show(listMusicFragment)
                .commit();
        isListMusicVisible = true;
    }

    // Phương thức thiết lập SeekBar
    private void setupSeekBar() {
        seekBarTime.setMax(musicPlayer.getDuration());
        seekBarTime.setProgress(0);
        seekBarTime.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean isFromUser) {
                if (isFromUser && musicPlayer != null) {
                    musicPlayer.seekTo(progress);
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

        seekBarVolume.setProgress(50);
        seekBarVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean isFromUser) {
                float volume = progress / 100f;
                if (musicPlayer != null) {
                    musicPlayer.setVolume(volume, volume);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }


    // Phương thức khởi động thread cập nhật SeekBar
    private void startSeekBarUpdateThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (musicPlayer != null) {
                    if (musicPlayer.isPlaying()) {
                        try {
                            final int current = musicPlayer.getCurrentPosition();
                            final int duration = musicPlayer.getDuration();
                            Log.d("MusicPlayer", "Current Position: " + current + ", Duration: " + duration);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    tvTime.setText(millisecondsToString(current));
                                    seekBarTime.setMax(duration);
                                    seekBarTime.setProgress(current);
                                }
                            });

                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        break;
                    }
                }
            }
        }).start();
    }



    // Phương thức chuyển đổi milliseconds thành String
    public String millisecondsToString(int time) {
        String elapsedTime;
        int minutes = time / 1000 / 60;
        int seconds = time / 1000 % 60;
        elapsedTime = minutes + ":";
        if (seconds < 10) {
            elapsedTime += "0";
        }
        elapsedTime += seconds;
        return elapsedTime;
    }

    // Phương thức onCreate
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
        startSeekBarUpdateThread();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (musicPlayer != null && !musicPlayer.isPlaying()) {
            musicPlayer.start(); // Bắt đầu lại nếu nhạc đã dừng
        }
        startSeekBarUpdateThread();
    }
    @Override
    protected void onPause() {
        super.onPause();

    }
    @Override
    public void onClick(View view) {

    }
}
