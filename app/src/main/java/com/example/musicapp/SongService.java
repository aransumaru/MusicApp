package com.example.musicapp;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
                    listSong.add(currSong);
                });
                futures.add(songFuture);
            }
            CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()]));
            allOf.thenRun(() -> future.complete(listSong));
        });
        return future;
    }

    public CompletableFuture<List<Song>> search(String name) {
        CompletableFuture<List<Song>> future = new CompletableFuture<>();
        client.search(name).thenAccept(res -> {
            List<Song> finalData = new ArrayList<>();
            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(res);
                JSONObject data = jsonObject.getJSONObject("data");
                JSONArray songsArray = data.getJSONArray("songs");
                List<Song> songsList = new ArrayList<>();
                for (int i = 0; i < songsArray.length(); i++) {
                    JSONObject songObject = songsArray.getJSONObject(i);
                    Song songData = new Song();
                    songData.setTitle(songObject.get("title").toString());
                    songData.setArtist(songObject.get("artistsNames").toString());
                    songData.setPath(songObject.get("encodeId").toString());
                    songsList.add(songData);
                }

                List<CompletableFuture<Void>> futures = new ArrayList<>();
                for (Song s : songsList) {
                    CompletableFuture<Void> songFuture = client.getSong(s.getPath()).thenAccept(urlData -> {
                        Gson gson = new Gson();
                        Type mapType = new TypeToken<Map<String, Object>>() {}.getType();
                        Map<String, Object> jsonMap = gson.fromJson(urlData, mapType);

                        Map<String, String> dataMap = (Map<String, String>) jsonMap.get("data");
                        String error = jsonMap.get("err").toString();
                        if ((error).equals("0.0")) {
                            String url128 = dataMap.get("128");
                            Log.d("SONGSERVICEEEEEEEEEEEEEEE", "Song ADDED: " + url128);
                            s.setPath(url128);
                            finalData.add(s);
                        }
                    });
                    futures.add(songFuture);
                }
                CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()]));
                allOf.thenRun(() -> {
                    future.complete(finalData);
                });
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        });
        return future;
    }
}
