package com.example.musicapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ListMusicActivity extends AppCompatActivity {
    private List<Song> data;
    private RecyclerView rcv;
    private ConstraintLayout listmusic;

    private void fakeData() {
        data = new ArrayList<>();
        for (int i = 0; i < 1000; ) {
            i++;
            data.add(new Song("Title " + i, "Artist " + i));
        }
    }
    private void bindingView() {
        rcv = findViewById(R.id.rcv);
        listmusic = findViewById(R.id.listmusic);
    }

    private void bindingAction() {
        listmusic.setOnClickListener(this::onListMusicClick);
    }

    private void onListMusicClick(View view) {
        Toast.makeText(this, "ListMusic Clicked!", Toast.LENGTH_SHORT).show();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_list_music);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.listmusic), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        bindingView();
        bindingAction();
        fakeData();
        bindDataToRecyclerView();
    }
    private void bindDataToRecyclerView() {
        SongsAdapter adapter = new SongsAdapter(data);
        rcv.setAdapter(adapter);
        rcv.setLayoutManager(new LinearLayoutManager(this));
    }
}