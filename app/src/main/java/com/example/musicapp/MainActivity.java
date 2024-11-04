package com.example.musicapp;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.musicapp.Models.Song;
import com.example.musicapp.NotificationService.MediaControlReceiver;
import com.example.musicapp.NotificationService.MediaNotificationHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ListMusicFragment.OnSongsDataPass {

    private static final String CHAT_FRAGMENT_TAG = "CHAT_FRAGMENT";
    private static final String CHANNEL_ID = "music_channel";
    private static final String LIST_MUSIC_FRAGMENT_TAG = "LIST_MUSIC_FRAGMENT";

    TextView tvTime, tvDuration, tvTitle, tvArtist;
    SeekBar seekBarTime, seekBarVolume;
    Button btnPlay, btnDownVolume, btnUpVolume, btnNextSong, btnPrevSong, btnLoop, btnRandom;
    public MediaPlayer mediaPlayer;
    private Song currentSong;
    ConstraintLayout mainLayout;
    ProgressBar progressBar;
    private boolean isLooping = false;
    private boolean isRandomOn = false;
    //tạo state list song và lưu danh sách từ fragment vào state
    private List<Song> songList = new ArrayList<>();
    private int currentSongIndex = -1;
    private MediaNotificationHelper notificationHelper;
    private MediaSessionCompat mediaSession;
    SharedPreferences prefs;

    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment chatFragment = fragmentManager.findFragmentByTag(CHAT_FRAGMENT_TAG);
        Fragment listMusicFragment = fragmentManager.findFragmentByTag(LIST_MUSIC_FRAGMENT_TAG);

        if ((chatFragment != null && chatFragment.isVisible()) || (listMusicFragment != null && listMusicFragment.isVisible())) {
            onConstraintLayoutMainClick(null);
        } else {
            super.onBackPressed(); // Thoát ứng dụng
        }
    }
    public void setCurrentSongIndex(int index) {
        currentSongIndex = index;
    }

    @Override
    public void onSongsDataPass(List<Song> songs) {
        songList.clear();
        songList.addAll(songs);
    }

    public void SetIsLoading(Boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            findViewById(R.id.overlay).setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);
            findViewById(R.id.overlay).setVisibility(View.GONE);
        }

    }

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
        mainLayout = findViewById(R.id.main);
        btnNextSong = findViewById(R.id.btnNextSong);
        btnPrevSong = findViewById(R.id.btnPrevSong);
        btnLoop = findViewById(R.id.btnLoop);
        btnRandom= findViewById(R.id.btnRandom);
        progressBar = findViewById(R.id.progressBar);
    }

    // Phương thức bindingAction
    private void bindingAction() {
        btnPlay.setOnClickListener(this::onBtnPlayClick);
        btnDownVolume.setOnClickListener(this::onBtnVolumeDownClick);
        btnUpVolume.setOnClickListener(this::onBtnVolumeUpClick);
        mainLayout.setOnClickListener(this::onConstraintLayoutMainClick);
        btnNextSong.setOnClickListener(this::onBtnNextSongClick);
        btnPrevSong.setOnClickListener(this::onBtnPrevSongClick);
        btnLoop.setOnClickListener(this::onBtnLoop);
        btnRandom.setOnClickListener(this::onBtnRandom);
    }

    private int getRandomIndex(int min, int max) {
        return (int) (Math.random() * (max - min + 1)) + min;
    }
    private void onBtnRandom(View view) {
        isRandomOn = !isRandomOn;
        if (isRandomOn) {
            btnRandom.setBackgroundResource(R.drawable.ic_button_random_enabled);
        } else {
            btnRandom.setBackgroundResource(R.drawable.ic_button_random);
        }
    }

    private void onBtnLoop(View view) {
        isLooping = !isLooping;
        if (isLooping) {
            mediaPlayer.setLooping(true);
            btnLoop.setBackgroundResource(R.drawable.ic_button_loop_enabled);
        } else {
            mediaPlayer.setLooping(false);
            btnLoop.setBackgroundResource(R.drawable.ic_button_loop);
        }
    }

    private void onBtnPrevSongClick(View view) {
        if (currentSongIndex == -1) return;
        if (currentSongIndex == 0) return;
        currentSongIndex--;
        Song song;
        try {
            song = songList.get(currentSongIndex);
        } catch (ArrayIndexOutOfBoundsException ex) {
            song = songList.get(0);
        }
//        stopCurrentMusic();
        updateUIWithSong(song);
    }

    private void onBtnNextSongClick(View view) {
        if (currentSongIndex == -1) return;
        if (currentSongIndex == songList.size() - 1) return;
        currentSongIndex++;
        Song song;
        try {
            song = songList.get(currentSongIndex);
        } catch (ArrayIndexOutOfBoundsException ex) {
            song = songList.get(0);
        }
//        stopCurrentMusic();
        updateUIWithSong(song);
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
        if (mediaPlayer != null) {
            stopCurrentMusic(); // Xóa media player / hết nhạc nếu có
        }
        mediaPlayer = new MediaPlayer();
        mediaSession = new MediaSessionCompat(this, "MediaSessionTag");
        notificationHelper = new MediaNotificationHelper(this, mediaSession, mediaPlayer);
        if (path != null) {
            try {
                mediaPlayer.setDataSource(path);
                mediaPlayer.prepare();
                mediaPlayer.setLooping(isLooping);
                notificationHelper.showMediaNotification(tvTitle.getText().toString(), tvArtist.getText().toString());
                mediaPlayer.start();
                btnPlay.setBackgroundResource(R.drawable.ic_button_pause);
                String duration = millisecondsToString(mediaPlayer.getDuration());
                tvDuration.setText(duration);
                mediaPlayer.setOnCompletionListener(mp -> {
                    if (isRandomOn) {
                        updateUIWithSong(songList.get(getRandomIndex(0, songList.size())));
                    } else {
                        onBtnNextSongClick(null);
                    }
                });
                setupSeekBar();
                startSeekBarUpdateThread();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Unable to set data source: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    void stopCurrentMusic() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
                Log.d("MusicPlayer", "Music stopped.");
                btnPlay.setBackgroundResource(R.drawable.ic_button_play);
            }
        }
    }

    // Phương thức xử lý nút Play
    public void onBtnPlayClick(View view) {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                startSeekBarUpdateThread();
                notificationHelper.showMediaNotification(tvTitle.getText().toString(), tvArtist.getText().toString());
                btnPlay.setBackgroundResource(R.drawable.ic_button_play);
            } else {
                mediaPlayer.start();
                startSeekBarUpdateThread();
                notificationHelper.showMediaNotification(tvTitle.getText().toString(), tvArtist.getText().toString());
                btnPlay.setBackgroundResource(R.drawable.ic_button_pause);
            }
        } else {
            Log.d("MusicPlayer", "MediaPlayer is null.");
            if (currentSong.getPath() == null) {
                onBtnListMusicClick();
            } else {
                setupMediaPlayer(currentSong.getPath());
            }
        }
    }
  
    // Phương thức xử lý nút tăng âm lượng
    private void onBtnVolumeUpClick(View view) {
        if (mediaPlayer != null) {
            mediaPlayer.setVolume(1.0f, 1.0f);
            seekBarVolume.setProgress(100);
        }
    }

    // Phương thức xử lý nút giảm âm lượng
    private void onBtnVolumeDownClick(View view) {
        if (mediaPlayer != null) {
            mediaPlayer.setVolume(0.0f, 0.0f);
            seekBarVolume.setProgress(0);
        }
    }
    private void FetchSongList() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        ListMusicFragment listMusicFragment = (ListMusicFragment) fragmentManager.findFragmentByTag(LIST_MUSIC_FRAGMENT_TAG);
        if (listMusicFragment == null) {
            listMusicFragment = new ListMusicFragment();
            fragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left)
                    .add(R.id.fragment_container, listMusicFragment, LIST_MUSIC_FRAGMENT_TAG)
                    .hide(listMusicFragment)
                    .commit();
        } else if (!listMusicFragment.isVisible()) {
            fragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left)
                    .hide(listMusicFragment)
                    .commit();
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

    // Phương thức thiết lập SeekBar
    private void setupSeekBar() {
        seekBarTime.setMax(mediaPlayer.getDuration());
        seekBarTime.setProgress(0);
        seekBarTime.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean isFromUser) {
                if (isFromUser && mediaPlayer != null) {
                    mediaPlayer.seekTo(progress);
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
                if (mediaPlayer != null) {
                    mediaPlayer.setVolume(volume, volume);
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
                while (mediaPlayer != null) {
                    if (mediaPlayer.isPlaying()) {
                        try {
                            final int current = mediaPlayer.getCurrentPosition();
                            final int duration = mediaPlayer.getDuration();
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

        // Register receiver for UI updates
        LocalBroadcastManager.getInstance(this).registerReceiver(uiUpdateReceiver,
                new IntentFilter(MediaControlReceiver.ACTION_UPDATE_UI));


        bindingView();
        bindingAction();
        startSeekBarUpdateThread();
        restoreLastSong();
        FetchSongList();
    }

    void restoreLastSong() {
        prefs = getSharedPreferences("MusicApp", MODE_PRIVATE);
        String title = prefs.getString("currentSongTitle", null);
        String artist = prefs.getString("currentSongArtist", null);
        int progress = prefs.getInt("currentSongProgress", 0);
        currentSongIndex = prefs.getInt("currSongIndex", 0);
        String path = prefs.getString("currentSongPath", null); // Lấy đường dẫn bài hát

        if (title != null && path != null) {
            // Tạo đối tượng Song từ dữ liệu đã lưu
            Song song = new Song(title, artist, path);
//            updateUIWithSong(song);
            tvTitle.setText(song.getTitle());
            tvArtist.setText(song.getArtist());
            currentSong = song;
//            mediaPlayer.seekTo(progress); // Khôi phục vị trí
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Quyền đã được cấp, có thể gửi thông báo
            } else {
                Toast.makeText(this, "Permission to post notifications denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startSeekBarUpdateThread();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Hủy thông báo nếu ứng dụng bị đóng
//        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
//        notificationManager.cancel(1); // Hủy thông báo có ID 1
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            mediaSession.release();
        }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(uiUpdateReceiver);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.option_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.opt_search);
        MenuItem chatItem = menu.findItem(R.id.opt_chat);
        searchItem.setOnMenuItemClickListener(item -> {
            onBtnListMusicClick();
            return true;
        });
        chatItem.setOnMenuItemClickListener(item -> {
            onBtnChatBubbleClick();
            return true;
        });

        return super.onCreateOptionsMenu(menu);
    }


    // Phương thức xử lý nút danh sách nhạc
    private void onBtnListMusicClick() {
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

    private void onBtnChatBubbleClick() {
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

    private final BroadcastReceiver uiUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getStringExtra("action");
            if (action == null) return;

            // Update UI based on action
            switch (action) {
                case MediaControlReceiver.ACTION_PLAY_PAUSE:
                    onBtnPlayClick(null);
                    break;
                case MediaControlReceiver.ACTION_NEXT:
                    onBtnNextSongClick(null);
                    break;
                case MediaControlReceiver.ACTION_PREVIOUS:
                    onBtnPrevSongClick(null);
                    break;
            }
        }
    };
}
