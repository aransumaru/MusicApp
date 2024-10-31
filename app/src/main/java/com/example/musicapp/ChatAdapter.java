package com.example.musicapp;// ChatAdapter.java
import android.annotation.SuppressLint;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
    private static ChatAdapter instance;
    private final List<ChatMessage> messages = new ArrayList<>();

    private ChatAdapter() {

    }

    public static ChatAdapter getInstance() {
        if (instance == null) {
            instance = new ChatAdapter();
        }
        return instance;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_message, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        holder.bind(messages.get(position));
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void addMessage(ChatMessage message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    public class ChatViewHolder extends RecyclerView.ViewHolder {
        private TextView messageText;
        private LinearLayout messageLayout;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.message_text);
            messageLayout = (LinearLayout) itemView;
        }

        public void bind(ChatMessage message) {
            messageText.setText(message.getMessage());
            if (message.isBot()) {
                messageLayout.setGravity(Gravity.START);
            } else {
                messageLayout.setGravity(Gravity.END);
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateMessages(List<ChatMessage> newMessages) {
        messages.clear();
        messages.addAll(newMessages);
        notifyDataSetChanged();
    }
}

