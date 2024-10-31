package com.example.musicapp;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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
    private static final int REQUEST_READ_EXTERNAL_STORAGE_PERMISSION = 1;

    private void bindingView() {
        rcv = findViewById(R.id.rcv);
        listmusic = findViewById(R.id.listmusic);
    }

    private void bindingAction() {
        listmusic.setOnClickListener(this::onListMusicClick);
    }

    private void onListMusicClick(View view) {

    }
    private void requestPermission(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                new AlertDialog.Builder(this)
                        .setTitle("Permission Needed")
                        .setMessage("This app needs the READ EXTERNAL STORAGE permission to read music files. Please grant the permission.")
                        .setPositiveButton("OK", (dialog, which) -> {
                            // Re-request the permission
                            ActivityCompat.requestPermissions(ListMusicActivity.this,
                                    new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                                    REQUEST_READ_EXTERNAL_STORAGE_PERMISSION);
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> {
                            dialog.dismiss();
                        })
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_READ_EXTERNAL_STORAGE_PERMISSION);
            }
        } else {
            //readMusicFiles();
        }


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
        data = new ArrayList<>();
        bindingView();
        bindingAction();
        //requestPermission();
        bindDataToRecyclerView();
    }
    private void bindDataToRecyclerView() {
        data = new ArrayList<>();
        SongsAdapter adapter = new SongsAdapter(new ArrayList<>());
        rcv.setLayoutManager(new LinearLayoutManager(this));
        rcv.setAdapter(adapter);

        new SongService().getSongs().thenAccept(songs -> {
            runOnUiThread(() -> {
                adapter.data.clear();
                adapter.data.addAll(songs);
                adapter.notifyDataSetChanged();
            });
        });
    }
//    private void bindDataToRecyclerView() {
//        SongsAdapter adapter = new SongsAdapter(data);
//        rcv.setAdapter(adapter);
//        rcv.setLayoutManager(new LinearLayoutManager(this));
//        adapter.notifyDataSetChanged();
//    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_READ_EXTERNAL_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            } else {
                Toast.makeText(this, "Permission denied to read music files", Toast.LENGTH_SHORT).show();
            }
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.option_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getMenuInflater().inflate(R.menu.option_menu, menu);
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.opt_listmusic) {
            Toast.makeText(this, "opt_listmusic_contextmenu", Toast.LENGTH_SHORT).show();
        } else if (item.getItemId() == R.id.opt_main) {
            Toast.makeText(this, "opt_main_contextmenu", Toast.LENGTH_SHORT).show();
        }
        return super.onContextItemSelected(item);
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.opt_listmusic) {
            Toast.makeText(this, "opt_listmusic", Toast.LENGTH_SHORT).show();
            Intent listmusicIntent = new Intent(this, ListMusicActivity.class);
            startActivity(listmusicIntent);
            return true;
        } else if (item.getItemId() == R.id.opt_main) {
            Toast.makeText(this, "opt_main", Toast.LENGTH_SHORT).show();
            Intent mainIntent = new Intent(this, MainActivity.class);
            startActivity(mainIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}