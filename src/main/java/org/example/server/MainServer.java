package org.example.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import org.example.chat.*;

public class MainServer {
    private static final int DEFAULT_PORT = 12345;
    private static final int CHAT_START_PORT = 20000;
    private static int nextChatPort = CHAT_START_PORT;
    private static final Map<Integer, String> chatRooms = new HashMap<>();

    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        System.out.println("MainServer started on port " + port);
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new MainServerHandler(clientSocket).start();
            }
        } catch (IOException e) {
            System.out.println("Error in MainServer: " + e.getMessage());
        }
    }

    private static class MainServerHandler extends Thread {
        private final Socket socket;
        private Connection connection;

        public MainServerHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                connection = new Connection(socket);
                handleClient();
            } catch (IOException e) {
                System.out.println("Error handling client: " + e.getMessage());
            } finally {
                connection.close();
            }
        }

        private void handleClient() throws IOException {
            // Ask client to choose action
            connection.send(new Message(MessageType.REQUEST_ACTION, "Do you want to (1) Create a new chat or (2) Join an existing chat? Enter 1 or 2:"));

            Message response = connection.receive();
            if (response.getType() == MessageType.CREATE_CHAT && "1".equals(response.getData())) {
                // Client wants to create a new chat
                createNewChat();

            } else if (response.getType() == MessageType.JOIN_CHAT && "2".equals(response.getData())) {
                // Client wants to join existing chat
                joinExistingChat();

            } else {
                // Invalid response
                connection.send(new Message(MessageType.ERROR, "Invalid option. Disconnecting."));
                connection.send(new Message(MessageType.DISCONNECT));
            }
        }

        private void createNewChat() throws IOException {
            // Assign new port
            int chatPort = getNextChatPort();
            String chatName = "Chat_" + chatPort;

            try {
                // Start new ChatServer with ServerSocket initialized
                ChatServer chatServer = new ChatServer(chatPort, chatName);
                new Thread(chatServer).start();

                // Add to chatRooms
                synchronized (chatRooms) {
                    chatRooms.put(chatPort, chatName);
                }

                // Inform client and automatically connect them to the new chat
                connection.send(new Message(MessageType.CHAT_CREATED, "New chat created on port " + chatPort + ". Connecting you to the chat..."));

            } catch (IOException e) {
                System.out.println("Error starting ChatServer: " + e.getMessage());
                connection.send(new Message(MessageType.ERROR, "Unable to create chat. Please try again later."));
            }
        }

        private void joinExistingChat() throws IOException {
            // Send list of available chats
            StringBuilder chatListBuilder = new StringBuilder();
            synchronized (chatRooms) {
                if (chatRooms.isEmpty()) {
                    connection.send(new Message(MessageType.ERROR, "No available chats. Please create a new chat."));
                    createNewChat();
                    return;
                }
                chatListBuilder.append("Available chats:\n");
                for (Map.Entry<Integer, String> entry : chatRooms.entrySet()) {
                    chatListBuilder.append("Chat name: ").append(entry.getValue()).append(", Port: ").append(entry.getKey()).append("\n");
                }
            }
            connection.send(new Message(MessageType.CHAT_LIST, chatListBuilder.toString()));

            // Ask client to enter port number
            connection.send(new Message(MessageType.PORT_REQUEST, "Enter the port number of the chat you want to join:"));
            Message response = connection.receive();
            System.out.println("Received message: " + response.getType() + " with data: " + response.getData());

            if (response.getType() == MessageType.CHAT_SELECTED) {
                try {
                    int port = Integer.parseInt(response.getData());
                    synchronized (chatRooms) {
                        if (chatRooms.containsKey(port)) {
                            connection.send(new Message(MessageType.CHAT_SELECTED, "Connecting you to chat on port " + port));
                        } else {
                            connection.send(new Message(MessageType.ERROR, "Invalid port number."));
                        }
                    }
                } catch (NumberFormatException e) {
                    connection.send(new Message(MessageType.ERROR, "Invalid port number."));
                }
            } else {
                connection.send(new Message(MessageType.ERROR, "Invalid response."));
            }
        }

        private synchronized int getNextChatPort() {
            return nextChatPort++;
        }
    }
}
