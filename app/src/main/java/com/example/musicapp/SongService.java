package com.example.musicapp;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class SongService {
    private ApiClient client;
    public SongService() {
        client = new ApiClient();
    }

    private CompletableFuture<List<Song>> fetchSongsInfo() {
        CompletableFuture<List<Song>> future = new CompletableFuture<>();
        List<Song> listSong = new ArrayList<>();
        client.getList().thenAccept(data -> {
            Gson gson = new Gson();
            Type listItemType = new TypeToken<List<Map<String, Object>>>() {}.getType();
            List<Map<String, Object>> listItems = gson.fromJson(data, listItemType);

            for (Map<String, Object> item : listItems) {
                Song song = new Song();
                song.setPath(item.get("url").toString());
                Map<String, Object> innerItem = (Map<String, Object>) item.get("item");
                String itemName = (String) innerItem.get("name");
                song.setTitle(itemName);
                List<Map<String, Object>> byArtists = (List<Map<String, Object>>) innerItem.get("byArtist");
                for (Map<String, Object> artist : byArtists) {
                    String artistName = (String) artist.get("name");
                    song.setArtist(artistName);
                    break;
                }
                listSong.add(song);
            }
            future.complete(listSong);
        });
        return future;
    }

    public CompletableFuture<List<Song>> getSongs() {
        List<Song> listSong = new ArrayList<>();
        CompletableFuture<List<Song>> future = new CompletableFuture<>();
        fetchSongsInfo().thenAccept(listSongsWithoutStreamUrl -> {
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            for (int i = 0; i < listSongsWithoutStreamUrl.size(); i++) {
                Song currSong = listSongsWithoutStreamUrl.get(i);
                String currSongId = currSong.getPath().split("/")[5].substring(0, 8);
                CompletableFuture<Void> songFuture = client.getSong(currSongId).thenAccept(data -> {
                    Gson gson = new Gson();
                    Type mapType = new TypeToken<Map<String, Object>>() {}.getType();
                    Map<String, Object> jsonMap = gson.fromJson(data, mapType);
                    Map<String, String> dataMap = (Map<String, String>) jsonMap.get("data");
                    String url128 = dataMap.get("128");
                    currSong.setPath(url128);
                });
                futures.add(songFuture);
                listSong.add(currSong);
            }
            CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
            allOf.thenRun(() -> future.complete(listSong));
        });
        return future;
    }
}
