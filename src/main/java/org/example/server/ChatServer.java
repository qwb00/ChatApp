package org.example.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.example.chat.*;

public class ChatServer implements Runnable {
    private final String chatName;
    private final ConcurrentMap<String, Connection> connections = new ConcurrentHashMap<>();
    private final ServerSocket serverSocket;

    public ChatServer(int port, String chatName) throws IOException {
        this.chatName = chatName;
        // Open the ServerSocket here to ensure it's ready before the client connects
        serverSocket = new ServerSocket(port);
        System.out.println("ChatServer '" + chatName + "' started on port " + port);
    }

    public void run() {
        try {
            while (true) {
                Socket socket = serverSocket.accept();
                new ChatHandler(socket).start();
            }
        } catch (IOException e) {
            System.out.println("Error in ChatServer '" + chatName + "': " + e.getMessage());
        } finally {
            try {
                serverSocket.close();
            } catch (IOException e) {
                System.out.println("Error closing ChatServer socket: " + e.getMessage());
            }
        }
    }

    private class ChatHandler extends Thread {
        private final Socket socket;
        private Connection connection;
        private String userName;

        public ChatHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                connection = new Connection(socket);
                performHandshake();
                sendMessageToAll(new Message(MessageType.USER_ADDED, userName + " has joined the chat.", userName));
                processMessages();
            } catch (IOException e) {
                System.out.println("Error handling client: " + e.getMessage());
            } finally {
                if (userName != null) {
                    connections.remove(userName);
                    sendMessageToAll(new Message(MessageType.USER_REMOVED, userName + " has left the chat.", userName));
                }
                connection.close();
            }
        }

        private void performHandshake() throws IOException {
            while (true) {
                connection.send(new Message(MessageType.NAME_REQUEST, "Enter your name:"));
                Message response = connection.receive();
                if (response.getType() == MessageType.USER_NAME) {
                    userName = response.getData();
                    if (userName != null && !userName.isEmpty() && !connections.containsKey(userName)) {
                        // Add connection to map
                        connections.put(userName, connection);
                        connection.send(new Message(MessageType.NAME_ACCEPTED, "Welcome to " + chatName));
                        break;
                    } else {
                        connection.send(new Message(MessageType.ERROR, "Invalid or duplicate name."));
                    }
                } else {
                    connection.send(new Message(MessageType.ERROR, "Invalid response."));
                }
            }
        }

        private void processMessages() throws IOException {
            while (true) {
                Message message = connection.receive();
                if (message == null) {
                    break;
                }
                if (message.getType() == MessageType.TEXT) {
                    // Broadcast message to all
                    Message broadcastMessage = new Message(MessageType.TEXT, message.getData(), userName, LocalDateTime.now(), chatName);
                    sendMessageToAll(broadcastMessage);
                } else if (message.getType() == MessageType.COMMAND) {
                    handleCommand(message);
                } else {
                    connection.send(new Message(MessageType.ERROR, "Invalid message type."));
                }
            }
        }

        private void handleCommand(Message message) throws IOException {
            String command = message.getData();
            if (command.startsWith("/rename ")) {
                String newName = command.substring(8).trim();
                if (!newName.isEmpty() && !connections.containsKey(newName)) {
                    connections.remove(userName);
                    connections.put(newName, connection);
                    String oldName = userName;
                    userName = newName;
                    sendMessageToAll(new Message(MessageType.NICKNAME_CHANGED, oldName + " changed nickname to " + userName));
                } else {
                    connection.send(new Message(MessageType.ERROR, "Invalid or duplicate nickname."));
                }
            } else if (command.equals("/list")) {
                String userList = String.join(", ", connections.keySet());
                connection.send(new Message(MessageType.USER_LIST, userList));
            }  else if (command.equals("/help")) {
                String helpMessage = """
                        Available commands:
                        /help           - Show this help message
                        /rename [name]  - Change your nickname
                        /list           - List users in the current chat room
                        /exit           - Exit the chat""";
                connection.send(new Message(MessageType.HELP_MESSAGE, helpMessage));
            } else {
                connection.send(new Message(MessageType.ERROR, "Unknown command.\n Type /help for a list of commands."));
            }
        }

        private void sendMessageToAll(Message message) {
            for (Connection conn : connections.values()) {
                try {
                    conn.send(message);
                } catch (IOException e) {
                    System.out.println("Error sending message to client: " + e.getMessage());
                }
            }
        }
    }
}
