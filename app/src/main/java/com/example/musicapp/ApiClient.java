package com.example.musicapp;
import android.util.Log;

import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Cookie;
import okhttp3.Call;
import okhttp3.Callback;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class ApiClient {
    private String CTIME = String.valueOf(Instant.now().getEpochSecond());
    private static final String VERSION = "1.6.34";
    private static final String SECRET = "2aa2d1c561e809b267f3638c4a307aab";
    private static final String API_KEY = "88265e23d4284f25963e6eedac8fbfa3";
    private static final String BASE_URL = "https://zingmp3.vn";
    private final OkHttpClient client;

    public ApiClient() {
        // Set up OkHttp client with cookie handling
        client = new OkHttpClient.Builder()
                .cookieJar(new CookieJar() {
                    // Implement CookieJar methods to store cookies
                    @Override
                    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                        // Save cookies as needed
                    }

                    @Override
                    public List<Cookie> loadForRequest(HttpUrl url) {
                        // Load cookies as needed
                        return new ArrayList<>();
                    }
                })
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    private CompletableFuture<String> getCookie() {
        CompletableFuture<String> future = new CompletableFuture<>();
        Request request = new Request.Builder()
                .url(BASE_URL) // Use your URL here
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                future.completeExceptionally(e); // Hoàn thành với lỗi
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.header("Set-Cookie") != null) {
                    String cookie = response.header("Set-Cookie");
                    future.complete(cookie); // Hoàn thành với cookie
                } else {
                    future.completeExceptionally(new IOException("Unexpected code " + response));
                }
            }
        });
        return future;
    }

    private CompletableFuture<String> requestZingMp3(String path, Map<String, String> params) {
        return getCookie().thenCompose(cookie -> {
            CompletableFuture<String> future = new CompletableFuture<>();
            HttpUrl.Builder urlBuilder = HttpUrl.parse(BASE_URL + path).newBuilder();
            urlBuilder.addQueryParameter("ctime", CTIME);
            urlBuilder.addQueryParameter("version", VERSION);
            urlBuilder.addQueryParameter("apiKey", API_KEY);
            if (params != null) {
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    urlBuilder.addQueryParameter(entry.getKey(), entry.getValue());
                }
            }
            String url = urlBuilder.build().toString();
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Cookie", cookie)
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
                        future.complete(responseData); // Hoàn thành với dữ liệu phản hồi
                    } else {
                        future.completeExceptionally(new IOException("Unexpected code " + response));
                    }
                }
            });
            return future;
        });
    }

    public CompletableFuture<String> getInfoSong(String songId) {
        String path = "/api/v2/song/get/info";
        Map<String, String> params = new HashMap<>();
        params.put("id", songId);
        params.put("sig", hashParam(path, songId));
        return requestZingMp3(path, params).handle((res, ex) -> {
            if (ex != null) {
                throw new RuntimeException(ex);
            }
            return res;
        });
    }

    public CompletableFuture<String> getTop100() {
        String path = "/api/v2/page/get/top-100";
        Map<String, String> params = new HashMap<>();
        params.put("sig", hashParamNoId(path));
        return requestZingMp3(path, params).handle((res, ex) -> {
            if (ex != null) {
                throw new RuntimeException(ex);
            }
            return res;
        });
    }

    public CompletableFuture<String> getSong(String songId) {
        String path = "/api/v2/song/get/streaming";
        Map<String, String> params = new HashMap<>();
        params.put("id", songId);
        params.put("sig", hashParam(path, songId));
        return requestZingMp3(path, params).handle((res, ex) -> {
            if (ex != null) {
                throw new RuntimeException(ex);
            }
            return res;
        });
    }

    public CompletableFuture<String> search(String search) {
        String path = "/api/v2/search/multi";
        Map<String, String> params = new HashMap<>();
        params.put("q", search);
        params.put("sig", hashParamNoId(path));
        return requestZingMp3(path, params).handle((res, ex) -> {
            if (ex != null) {
                throw new RuntimeException(ex);
            }
            return res;
        });
    }

    public CompletableFuture<String> getList() {
        String path = "https://zingmp3.vn/album/Top-100-Bai-Hat-Nhac-Tre-Hay-Nhat-Quang-Hung-MasterD-Chau-Khai-Phong-Jack-J97-Ho-Quang-Hieu/ZWZB969E.html";
        CompletableFuture<String> future = new CompletableFuture<>();
        HttpUrl.Builder urlBuilder = HttpUrl.parse(path).newBuilder();
        String url = urlBuilder.build().toString();
        Request request = new Request.Builder()
                .url(url)
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
                    String[] parts = responseData.split("</script>");
                    String n = parts[3];
                    JSONObject jsonObject = null;
                    try {
                        jsonObject = new JSONObject(n.substring(40));
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    try {
                        JSONArray trackList = jsonObject.getJSONObject("track").getJSONArray("itemListElement");
                        future.complete(trackList.toString());
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    future.completeExceptionally(new IOException("Unexpected code " + response));
                }
            }
        });
        return future;
    }




    private String hashParamNoId(String path) {
        return getHmac512(path + getHash256(String.format("ctime=%sversion=%s", CTIME, VERSION)), SECRET);
    }

    private String hashParam(String path, String id) {
        return getHmac512(path + getHash256(String.format("ctime=%sid=%sversion=%s", CTIME, id, VERSION)), SECRET);
    }

    private String hashParamHome(String path) {
        return getHmac512(path + getHash256(String.format("count=30ctime=%spage=1version=%s", CTIME, VERSION)), SECRET);
    }

    private String hashCategoryMV (String path, String id, String type) {
        return getHmac512(path + getHash256(String.format("ctime=%sid=%stype=%sversion=%s", CTIME, id, type,VERSION)), SECRET);
    }
    private static String getHash256(String str) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(str.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error initializing SHA-256", e);
        }
    }

    private static String getHmac512(String str, String key) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac.init(secretKeySpec);
            byte[] hash = hmac.doFinal(str.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Error initializing HmacSHA512", e);
        }
    }
}

