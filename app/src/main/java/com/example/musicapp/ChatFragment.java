package com.example.musicapp;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ChatFragment extends Fragment {
    private EditText messageInput;
    private ImageButton sendButton;
    private RecyclerView chatRecyclerView;
    private ChatAdapter chatAdapter;
    private AiService aiService;

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

        aiService.Response(message).thenAccept(response -> {
            requireActivity().runOnUiThread(() -> {
                chatAdapter.addMessage(new ChatMessage(response, true));
                chatRecyclerView.scrollToPosition(chatAdapter.getItemCount() - 1);
                sendButton.setImageResource(R.drawable.ic_send);
                sendButton.setEnabled(true);
            });
        }).exceptionally(e -> {
            e.printStackTrace();
            requireActivity().runOnUiThread(() -> {
                chatAdapter.addMessage(new ChatMessage("Error: " + e.getMessage(), true));
                chatRecyclerView.scrollToPosition(chatAdapter.getItemCount() - 1);
                sendButton.setImageResource(R.drawable.ic_send);
                sendButton.setEnabled(true);
            });
            return null;
        });
    }
}