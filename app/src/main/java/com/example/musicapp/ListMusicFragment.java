package com.example.musicapp;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.musicapp.Models.Song;

import java.util.ArrayList;
import java.util.List;


public class ListMusicFragment extends Fragment {
    private RecyclerView rcv;
    private SearchView  btnSearch;

    //interface callback để chuyển state list song qua MainActitity (xử lý chuyển bài bằng núi next và prev)
    private OnSongsDataPass dataPasser;
    public interface OnSongsDataPass {
        void onSongsDataPass(List<Song> songs);
    }
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnSongsDataPass) {
            dataPasser = (OnSongsDataPass) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnSongsDataPass");
        }
    }

    private void bindingView(View view) {
        rcv = view.findViewById(R.id.rcv);
        btnSearch = view.findViewById(R.id.btnSearch);
    }

    private void bindingAction() {
        btnSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                fetchDataToRecyclerView(query.trim());
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    private void fetchDataToRecyclerView(String title) {
        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.SetIsLoading(true);
        SongsAdapter adapter = new SongsAdapter(new ArrayList<>());
        rcv.setLayoutManager(new LinearLayoutManager(getActivity()));
        rcv.setAdapter(adapter);
        SongService _songService = new SongService();
        if (title.trim() == "") {
            _songService.getSongs().thenAccept(songs -> {
                getActivity().runOnUiThread(() -> {
                    dataPasser.onSongsDataPass(songs);
                    adapter.data.clear();
                    adapter.data.addAll(songs);
                    adapter.notifyDataSetChanged();
                    mainActivity.SetIsLoading(false);
                });
            });
        } else {
            _songService.search(title.trim()).thenAccept(songs -> {
                getActivity().runOnUiThread(() -> {
                    dataPasser.onSongsDataPass(songs);
                    adapter.data.clear();
                    if (songs.isEmpty()) {
                        Toast.makeText(mainActivity, "Không tìm thấy!", Toast.LENGTH_SHORT).show();
                    }
                    adapter.data.addAll(songs);
                    adapter.notifyDataSetChanged();
                    mainActivity.SetIsLoading(false);
                });
            });
        }
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_music, container, false);
        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.listmusic), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        bindingView(view);
        bindingAction();
        fetchDataToRecyclerView("");
        return view;
    }

}