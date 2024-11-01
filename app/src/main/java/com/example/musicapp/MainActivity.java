package com.example.musicapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
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
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final String CHAT_FRAGMENT_TAG = "CHAT_FRAGMENT";
    private static final String CHANNEL_ID = "music_channel";
    private static final String LIST_MUSIC_FRAGMENT_TAG = "LIST_MUSIC_FRAGMENT";

    TextView tvTime, tvDuration, tvTitle, tvArtist;
    SeekBar seekBarTime, seekBarVolume;
    Button btnPlay, btnDownVolume, btnUpVolume, btnListMusic;
    private ImageView chatBubble;
    MediaPlayer musicPlayer;
    private Song currentSong;
    ConstraintLayout mainLayout;

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
        btnListMusic = findViewById(R.id.btnListMusic);
        chatBubble = findViewById(R.id.chat_bubble);
        mainLayout = findViewById(R.id.main);
    }

    // Phương thức bindingAction
    private void bindingAction() {
        btnPlay.setOnClickListener(this::onBtnPlayClick);
        btnDownVolume.setOnClickListener(this::onBtnVolumeDownClick);
        btnUpVolume.setOnClickListener(this::onBtnVolumeUpClick);
        btnListMusic.setOnClickListener(this::onBtnListMusicClick);
        chatBubble.setOnClickListener(this::onBtnChatBubbleClick);
        mainLayout.setOnClickListener(this::onConstraintLayoutMainClick);
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
                sendNotification(tvTitle.getText().toString(), tvArtist.getText().toString(), "Playing");
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
                startSeekBarUpdateThread();
                sendNotification(tvTitle.getText().toString(), tvArtist.getText().toString(), "Paused");
                btnPlay.setBackgroundResource(R.drawable.ic_button_play);
            } else {
                musicPlayer.start();
                startSeekBarUpdateThread();
                sendNotification(tvTitle.getText().toString(), tvArtist.getText().toString(), "Playing");
                btnPlay.setBackgroundResource(R.drawable.ic_button_pause);
            }
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Music Player Channel", // Channel name
                    NotificationManager.IMPORTANCE_LOW // Importance level
            );
            serviceChannel.setSound(null, null);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    void sendNotification(String title, String artist, String status) {
        // Lưu thông tin bài hát vào SharedPreferences
        SharedPreferences prefs = getSharedPreferences("MusicApp", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("currentSongTitle", title);
        editor.putString("currentSongArtist", artist);
        editor.putInt("currentSongProgress", musicPlayer.getCurrentPosition());
        editor.putString("currentSongPath", currentSong.getPath()); // Lưu đường dẫn bài hát
        editor.apply();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_music_player)
                .setContentTitle(title)
                .setContentText("Artist: " + artist + "\nStatus: " + status)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setSound(null)
                .setAutoCancel(false)
                .addAction(
                        musicPlayer.isPlaying() ? R.drawable.ic_button_pause : R.drawable.ic_button_play,
                        musicPlayer.isPlaying() ? "Pause" : "Play",
                        getPendingIntentForNotificationAction("TOGGLE_PLAY_PAUSE")
                );

        // Tạo Intent để mở MainActivity khi nhấn vào thông báo
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);

        // Hiển thị thông báo
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationManager.notify(1, builder.build());
    }

    // Phương thức để tạo PendingIntent cho các hành động trong thông báo
    private PendingIntent getPendingIntentForNotificationAction(String action) {
        Intent intent = new Intent(this, NotificationReceiver.class);
        intent.setAction(action);
        return PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("ACTION_TOGGLE_PLAY_PAUSE".equals(intent.getAction())) {
                onBtnPlayClick(null); // Gọi phương thức để phát/tạm dừng nhạc
            }
        }
    };


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
    private void onConstraintLayoutMainClick(View view) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment chatFragment = fragmentManager.findFragmentByTag(CHAT_FRAGMENT_TAG);
        Fragment listMusicFragment = fragmentManager.findFragmentByTag(LIST_MUSIC_FRAGMENT_TAG);

        if (listMusicFragment != null && listMusicFragment.isVisible()) {
            fragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.slide_out_left, R.anim.slide_out_right)
                    .hide(listMusicFragment)
                    .commit();
        }
        if (chatFragment != null && chatFragment.isVisible()) {
            fragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.slide_out_left, R.anim.slide_out_right) // Set animations for popping
                    .hide(chatFragment)
                    .commit();
        }
    }

    // Phương thức xử lý nút danh sách nhạc
    private void onBtnListMusicClick(View view) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment listMusicFragment = fragmentManager.findFragmentByTag(LIST_MUSIC_FRAGMENT_TAG);
        Fragment chatFragment = fragmentManager.findFragmentByTag(CHAT_FRAGMENT_TAG);

        if (chatFragment != null && chatFragment.isVisible()) {
            fragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.slide_out_left, R.anim.slide_out_right) // Set animations for popping
                    .hide(chatFragment)
                    .commit();
        }

        if (listMusicFragment == null) {
            listMusicFragment = new ListMusicFragment();
            fragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left)
                    .add(R.id.fragment_container, listMusicFragment, LIST_MUSIC_FRAGMENT_TAG)
                    .commit();
        } else if (!listMusicFragment.isVisible()) {
            fragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left)
                    .show(listMusicFragment)
                    .commit();
        }
    }

    private void onBtnChatBubbleClick(View view) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment chatFragment = fragmentManager.findFragmentByTag(CHAT_FRAGMENT_TAG);
        Fragment listMusicFragment = fragmentManager.findFragmentByTag(LIST_MUSIC_FRAGMENT_TAG);

        if (listMusicFragment != null && listMusicFragment.isVisible()) {
            fragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.slide_out_left, R.anim.slide_out_right)
                    .hide(listMusicFragment)
                    .commit();
        }

        if (chatFragment == null) {
            // Create a new instance of ChatFragment
            Fragment newChatFragment = new ChatFragment();
            fragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_left, R.anim.slide_in_right) // Set animations for adding
                    .add(R.id.fragment_container, newChatFragment, CHAT_FRAGMENT_TAG)
                    .commit();
        } else if (!chatFragment.isVisible()) {
            fragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_left, R.anim.slide_in_right)
                    .show(chatFragment)
                    .commit();
        }

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
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
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
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter("ACTION_TOGGLE_PLAY_PAUSE"));
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        createNotificationChannel();

        bindingView();
        bindingAction();
        startSeekBarUpdateThread();

        // Khôi phục trạng thái bài hát
        SharedPreferences prefs = getSharedPreferences("MusicApp", MODE_PRIVATE);
        String title = prefs.getString("currentSongTitle", null);
        String artist = prefs.getString("currentSongArtist", null);
        int progress = prefs.getInt("currentSongProgress", 0);
        String path = prefs.getString("currentSongPath", null); // Lấy đường dẫn bài hát

        if (title != null && path != null) {
            // Tạo đối tượng Song từ dữ liệu đã lưu
            Song song = new Song(title, artist, path);
            updateUIWithSong(song);
            musicPlayer.seekTo(progress); // Khôi phục vị trí
            musicPlayer.start(); // Bắt đầu phát từ vị trí đã lưu
        }
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
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }
}
