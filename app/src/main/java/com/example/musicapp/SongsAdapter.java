package com.example.musicapp;

import android.content.SharedPreferences;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicapp.Models.Song;

import java.util.List;

public class SongsAdapter extends RecyclerView.Adapter<SongsAdapter.ViewHolder> {
    List<Song> data;

    public SongsAdapter(List<Song> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public SongsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_song, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongsAdapter.ViewHolder holder, int position) {
        holder.setData(data.get(position));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvArtist;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvArtist = itemView.findViewById(R.id.tvArtist);
            tvTitle.setOnClickListener(this::onTvTitleClick);
            tvArtist.setOnClickListener(this::onTvArtistClick);
            itemView.setOnClickListener(this::onItemViewClick);
        }


        private void onItemViewClick(View view) {
            MainActivity mainActivity = (MainActivity) view.getContext();

            mainActivity.onConstraintLayoutMainClick(null);
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                Song song = data.get(position);

                Song currentSong = mainActivity.getCurrentSong();

                if (currentSong == null || !currentSong.getPath().equals(song.getPath())) {
                    mainActivity.stopCurrentMusic();
                    mainActivity.updateUIWithSong(song);
                    //update current song index
                    mainActivity.setCurrentSongIndex(position);
                    // Lưu thông tin bài hát vào SharedPreferences
                    SharedPreferences prefs = mainActivity.getSharedPreferences("MusicApp", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("currentSongTitle", song.getTitle());
                    editor.putString("currentSongArtist", song.getArtist());
                    editor.putInt("currentSongProgress", 0); // Đặt tiến trình về 0 khi chọn bài mới
                    editor.putString("currentSongPath", song.getPath()); // Lưu đường dẫn bài hát
                    editor.apply();

                    String message = "Đã chọn bài hát " + song.getTitle();
                    Toast.makeText(mainActivity, message, Toast.LENGTH_SHORT).show();
                } else {
                    String message = song.getTitle() + " đang được phát.";
                    Toast.makeText(mainActivity, message, Toast.LENGTH_SHORT).show();
                }

            }
        }

        private void onTvTitleClick(View view) {
            onItemViewClick(view);
        }

        private void onTvArtistClick(View view) {
            onItemViewClick(view);
        }

        public void setData(Song s) {
            tvTitle.setText(s.getTitle());
            tvArtist.setText(s.getArtist());
        }
    }
}
