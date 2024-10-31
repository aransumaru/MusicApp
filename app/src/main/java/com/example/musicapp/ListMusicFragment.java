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
    private List<Song> data;
    private RecyclerView rcv;
    private ConstraintLayout listmusic;
    private Button btnSearch;
    private EditText edtSearch;
    private static final int REQUEST_READ_EXTERNAL_STORAGE_PERMISSION = 1;
    private void bindingView(View view) {
        rcv = view.findViewById(R.id.rcv);
        listmusic = view.findViewById(R.id.listmusic);
        btnSearch = view.findViewById(R.id.btnSearch);
        edtSearch = view.findViewById(R.id.edtSearch);
        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.listmusic), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

    }

    private void bindDataToRecyclerView() {
        data = new ArrayList<>();
        SongsAdapter adapter = new SongsAdapter(new ArrayList<>());
        rcv.setLayoutManager(new LinearLayoutManager(getActivity()));
        rcv.setAdapter(adapter);

        new SongService().getSongs().thenAccept(songs -> {
            getActivity().runOnUiThread(() -> {
                adapter.data.clear();
                adapter.data.addAll(songs);
                adapter.notifyDataSetChanged();
            });
        });
    }
    private void bindingAction() {
        listmusic.setOnClickListener(this::onListMusicClick);
        btnSearch.setOnClickListener(v -> onBtnSearchClick());
    }

    private void onBtnSearchClick() {
        String keyword = edtSearch.getText().toString().trim();
        searchMusic(keyword);
    }

    // hàm search music để demo
    private void searchMusic(String keyword) {
        Toast.makeText(getActivity(), "Tìm kiếm: " + keyword, Toast.LENGTH_SHORT).show();
        // search với keyword là data
    }

    private void onListMusicClick(View view) {
        Toast.makeText(getActivity(), "ListMusic Clicked!", Toast.LENGTH_SHORT).show();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_music, container, false);
        bindingView(view);
        bindingAction();

        bindDataToRecyclerView();
        //requestPermission();
        return view;
    }

}