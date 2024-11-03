package com.example.musicapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicapp.Models.ChatMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatFragment extends Fragment {
    private EditText messageInput;
    private ImageButton sendButton;
    private RecyclerView chatRecyclerView;
    private ChatAdapter chatAdapter;
    private AiService aiService;
    private Map<String, Object> historyChat;
    List<Map<String, Object>> contents;
    Map<String, Object> userPart;
    Map<String, Object> modelPart;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        messageInput = view.findViewById(R.id.message_input);
        sendButton = view.findViewById(R.id.send_button);
        chatRecyclerView = view.findViewById(R.id.chat_recycler_view);

        // Initialize AiService
        aiService = new AiService();

        setupRecyclerView();

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = messageInput.getText().toString().trim();
                if (!message.isEmpty()) {
                    sendMessage(message);
                    messageInput.getText().clear();
                }
            }
        });
        historyChat = new HashMap<>();
        contents = new ArrayList<>();

        return view;
    }

    private void setupRecyclerView() {
        chatAdapter = ChatAdapter.getInstance();
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        chatRecyclerView.setAdapter(chatAdapter);
        chatRecyclerView.scrollToPosition(chatAdapter.getItemCount() - 1);
    }

    private void sendMessage(String message) {
        // Add user message to the chat list
        chatAdapter.addMessage(new ChatMessage(message, false));
        chatRecyclerView.scrollToPosition(chatAdapter.getItemCount() - 1);

        // Waitt here okay?
        sendButton.setImageResource(R.drawable.ic_send_disabled);
        sendButton.setEnabled(false);

        userPart = new HashMap<>();
        userPart.put("role", "user");
        userPart.put("parts", Collections.singletonList(Collections.singletonMap("text", message)));
        contents.add(userPart);
        historyChat.put("contents", contents);
        // EM CONG CHO AI BOT NHAN TIN DUOI NAY

//        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                chatAdapter.addMessage(new ChatMessage("Bot response to: " + message, true));
//                chatRecyclerView.scrollToPosition(chatAdapter.getItemCount() - 1);
//                sendButton.setImageResource(R.drawable.ic_send);
//                sendButton.setEnabled(true);
//            }
//        }, 1000);

        // Thoi anh lai lam luon

        aiService.Response(message, historyChat).thenAccept(response -> {
            modelPart = new HashMap<>();
            modelPart.put("role", "model");
            modelPart.put("parts", Collections.singletonList(Collections.singletonMap("text", response)));
            contents.add(modelPart);
            historyChat.put("contents", contents);

            requireActivity().runOnUiThread(() -> {
                chatAdapter.addMessage(new ChatMessage(response, true));
                chatRecyclerView.scrollToPosition(chatAdapter.getItemCount() - 1);
                sendButton.setImageResource(R.drawable.ic_send);
                sendButton.setEnabled(true);
            });
        }).exceptionally(e -> {
            e.printStackTrace();
            requireActivity().runOnUiThread(() -> {
                chatAdapter.addMessage(new ChatMessage("Đã đạt đến giới hạn tin nhắn. Vui lòng thử lại sau 2 phút.", true));
                chatRecyclerView.scrollToPosition(chatAdapter.getItemCount() - 1);
                sendButton.setImageResource(R.drawable.ic_send);
                sendButton.setEnabled(true);
            });
            return null;
        });
    }
}
