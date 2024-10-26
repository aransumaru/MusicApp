package com.example.musicapp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class SongsAdapter extends RecyclerView.Adapter<SongsAdapter.ViewHolder> {
    private List<Song> data;

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
        Song s = data.get(position);
        holder.setData(s);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTitle;
        private TextView tvArtist;

        private void bindingView() {
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvArtist = itemView.findViewById(R.id.tvArtist);
        }

        private void bindingAction() {
            tvTitle.setOnClickListener(this::onTvTitleClick);
            tvArtist.setOnClickListener(this::onTvArtistClick);
            itemView.setOnClickListener(this::onItemViewClick);
        }

        private void onItemViewClick(View view) {
            Toast.makeText(view.getContext(), tvTitle.getText() + " - - - " + tvArtist.getText(), Toast.LENGTH_SHORT).show();
            Context context = view.getContext();
            Intent intent = new Intent(context, MainActivity.class);
            Song song = data.get(getAdapterPosition());
            intent.putExtra("song", song); // Truyền Song object
            context.startActivity(intent);
        }

        private void onTvTitleClick(View view) {
            Toast.makeText(view.getContext(), tvArtist.getText(), Toast.LENGTH_SHORT).show();
        }

        private void onTvArtistClick(View view) {
            Toast.makeText(view.getContext(), tvArtist.getText(), Toast.LENGTH_SHORT).show();
        }

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            bindingView();
            bindingAction();
        }
        public void setData(Song s) {
            tvTitle.setText(s.getTitle());
            tvArtist.setText(s.getArtist());
        }
    }
}
