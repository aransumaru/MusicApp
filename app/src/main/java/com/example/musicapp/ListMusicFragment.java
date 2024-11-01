package com.example.musicapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import java.util.ArrayList;
import java.util.List;


public class ListMusicFragment extends Fragment {
    private RecyclerView rcv;
    private SearchView  btnSearch;

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
        SongsAdapter adapter = new SongsAdapter(new ArrayList<>());
        rcv.setLayoutManager(new LinearLayoutManager(getActivity()));
        rcv.setAdapter(adapter);
        SongService _songService = new SongService();
        if (title.trim() == "") {
            _songService.getSongs().thenAccept(songs -> {
                getActivity().runOnUiThread(() -> {
                    adapter.data.clear();
                    adapter.data.addAll(songs);
                    adapter.notifyDataSetChanged();
                });
            });
        } else {
            _songService.search(title.trim()).thenAccept(songs -> {
                getActivity().runOnUiThread(() -> {
                    adapter.data.clear();
                    adapter.data.addAll(songs);
                    adapter.notifyDataSetChanged();
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