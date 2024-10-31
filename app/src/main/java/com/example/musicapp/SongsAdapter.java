package com.example.musicapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
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
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                Song song = data.get(position);
                ((MainActivity) view.getContext()).stopMusic();
                ((MainActivity) view.getContext()).updateUIWithSong(song);
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
