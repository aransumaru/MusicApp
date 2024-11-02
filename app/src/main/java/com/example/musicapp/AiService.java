package com.example.musicapp;


import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import okhttp3.*;


public class AiService {
    private final String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent?key=AIzaSyD6xhKtuyqoqhpL7PBcXvJNt2Xyh5cuX3U";

    public CompletableFuture<String> Response(String msg, Map<String, Object> historyChat) {
        CompletableFuture<String> future = new CompletableFuture();
        OkHttpClient client = new OkHttpClient();
        Gson gson = new Gson();
        String jsonString = gson.toJson(historyChat);
        Log.d("AIIIIIIIIIIIIIIIIIIIIIIIIIIIII", jsonString);

        // Tạo RequestBody
        RequestBody body = RequestBody.create(
                MediaType.parse("application/json; charset=utf-8"),
                jsonString
                );
        // Tạo request
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                future.completeExceptionally(e); // Hoàn thành với lỗi
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    // Tạo một thể hiện của JsonParser
                    JsonParser parser = new JsonParser();

                    // Phân tích JSON
                    JsonObject jsonObject = parser.parse(responseData).getAsJsonObject();

                    // Lấy ra candidates
                    JsonArray candidates = jsonObject.getAsJsonArray("candidates");
                    JsonObject candidate = candidates.get(0).getAsJsonObject();

                    // Lấy ra content và parts
                    JsonObject content = candidate.getAsJsonObject("content");
                    JsonArray parts = content.getAsJsonArray("parts");

                    // Lấy ra text
                    String text = parts.get(0).getAsJsonObject().get("text").getAsString();
                    future.complete(text); // Hoàn thành với dữ liệu phản hồi
                } else {
                    future.completeExceptionally(new IOException("Unexpected code " + response));
                }
            }
        });

        return future;
    }

}
