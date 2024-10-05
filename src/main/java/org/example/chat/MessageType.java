package org.example.chat;

public enum MessageType {
    REQUEST_ACTION,
    CREATE_CHAT,
    JOIN_CHAT,
    CHAT_CREATED,
    CHAT_LIST,
    PORT_REQUEST,
    CHAT_SELECTED,
    DISCONNECT,
    NAME_REQUEST,
    USER_NAME,
    NAME_ACCEPTED,
    TEXT,
    USER_ADDED,
    USER_REMOVED,
    ERROR,

    // Client commands
    COMMAND,
    NICKNAME_CHANGED,
    USER_LIST,
    HELP_MESSAGE
}
