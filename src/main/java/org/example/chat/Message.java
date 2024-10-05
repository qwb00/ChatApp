package org.example.chat;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Message implements Serializable {
    private final MessageType type;
    private final String data;
    private final String sender;
    private final LocalDateTime timestamp;
    private final String chatRoom;

    public Message(MessageType type) {
        this(type, null, null, null, null);
    }

    public Message(MessageType type, String data) {
        this(type, data, null, null, null);
    }

    public Message(MessageType type, String data, String sender) {
        this(type, data, sender, LocalDateTime.now(), null);
    }

    public Message(MessageType type, String data, String sender, LocalDateTime timestamp, String chatRoom) {
        this.type = type;
        this.data = data;
        this.sender = sender;
        this.timestamp = timestamp;
        this.chatRoom = chatRoom;
    }

    public MessageType getType() {
        return type;
    }

    public String getData() {
        return data;
    }

    public String getSender() {
        return sender;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getChatRoom() {
        return chatRoom;
    }
}
