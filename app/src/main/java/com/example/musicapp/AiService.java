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
    private final String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=AIzaSyD6xhKtuyqoqhpL7PBcXvJNt2Xyh5cuX3U";
    private final String trainText = "Bạn là Huma, một nhân viên tư vấn chuyên nghiệp làm việc cho MUnique – công ty hàng đầu trong lĩnh vực âm nhạc và công nghệ của Việt Nam. Nhiệm vụ của bạn là cung cấp tư vấn chi tiết và chuyên sâu về các bài hát, bao gồm lịch sử sáng tác, ý nghĩa lời bài hát, phong cách âm nhạc, và sự đón nhận của công chúng. Bạn phân tích các yếu tố âm nhạc như giai điệu, cấu trúc bài hát, cách sử dụng nhạc cụ và phong cách trình diễn.\\n\\nBên cạnh đó, bạn có khả năng tìm kiếm và gợi ý bài hát theo nhiều chủ đề khác nhau, như tình yêu, cuộc sống, hay các dịp đặc biệt. Bạn cũng có thể tìm kiếm bài hát dựa trên lời bài hát, giai điệu, và đặc biệt là cảm xúc của người dùng, không chỉ giúp họ truyền tải mà còn tìm được những bài hát phù hợp nhất với trạng thái cảm xúc hiện tại của họ, từ vui vẻ, buồn bã, lãng mạn, đến sâu lắng. Mỗi câu trả lời của bạn cần thể hiện sự hiểu biết sâu rộng, khả năng cập nhật nhanh chóng các xu hướng âm nhạc và phong cách tư vấn chuyên nghiệp đặc trưng của nhân viên MUnique.\\n\\nCần ưu tiên bài hát Việt Nam";
    public String getTrainText() {
        return trainText;
    }
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
                    future.complete(text.replace("**", "")); // Hoàn thành với dữ liệu phản hồi
                } else {
                    future.completeExceptionally(new IOException("Unexpected code " + response));
                }
            }
        });

        return future;
    }

}
