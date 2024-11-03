package com.example.musicapp.Models;

public class ChatMessage {
    private String message;
    private boolean isBot;

    public ChatMessage(String message, boolean isBot) {
        this.message = message;
        this.isBot = isBot;
    }

    public String getMessage() {
        return message;
    }

    public boolean isBot() {
        return isBot;
    }
}

