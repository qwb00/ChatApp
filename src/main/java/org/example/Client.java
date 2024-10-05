package org.example;

import java.io.IOException;
import java.net.Socket;
import java.time.format.DateTimeFormatter;
import org.example.chat.*;

public class Client {
    private Connection connection;
    private String userName;

    public static void main(String[] args) {
        new Client().run();
    }

    public void run() {
        try {
            // Connect to MainServer
            connectToMainServer();
        } catch (IOException e) {
            System.out.println("Error in client: " + e.getMessage());
        }
    }

    private void connectToMainServer() throws IOException {
        String serverAddress = getServerAddress();
        int serverPort = getServerPort();

        Socket socket = new Socket(serverAddress, serverPort);
        connection = new Connection(socket);

        while (true) {
            Message message = connection.receive();
            switch (message.getType()) {
                case REQUEST_ACTION:
                    System.out.println(message.getData());
                    String action;
                    while (true) {
                        action = ConsoleHelper.readString();
                        if ("1".equals(action)) {
                            connection.send(new Message(MessageType.CREATE_CHAT, "1"));
                            break;
                        } else if ("2".equals(action)) {
                            connection.send(new Message(MessageType.JOIN_CHAT, "2"));
                            break;
                        } else {
                            System.out.println("Invalid option. Please enter 1 or 2:");
                        }
                    }
                    break;
                case CHAT_CREATED, CHAT_SELECTED:
                    System.out.println(message.getData());
                    int chatPort = extractPortFromMessage(message.getData());
                    connection.close();
                    connectToChatServer(serverAddress, chatPort);
                    return;
                case CHAT_LIST:
                    System.out.println(message.getData());
                    break;
                case PORT_REQUEST:
                    System.out.println(message.getData());
                    String portStr;
                    while (true) {
                        portStr = ConsoleHelper.readString();
                        try {
                            int port = Integer.parseInt(portStr);
                            if (port >= 0 && port <= 65535) {
                                connection.send(new Message(MessageType.CHAT_SELECTED, String.valueOf(port)));
                                break;
                            } else {
                                System.out.println("Invalid port number. Please enter a valid port number:");
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid port number. Please enter a valid port number:");
                        }
                    }
                    break;
                case ERROR:
                    System.out.println("Error: " + message.getData());
                    connection.close();
                    return;
                default:
                    System.out.println("Unexpected message type: " + message.getType());
            }
        }
    }

    private void connectToChatServer(String serverAddress, int port) {
        try {
            Socket socket = new Socket(serverAddress, port);
            connection = new Connection(socket);

            // Perform handshake
            performHandshake();

            // Start a thread to read messages from the server
            new ReaderThread().start();

            // Read messages from console and send to server
            while (true) {
                String text = ConsoleHelper.readString();
                if (text.startsWith("/")) {
                    handleCommand(text);
                } else {
                    connection.send(new Message(MessageType.TEXT, text, userName));
                }
            }
        } catch (IOException e) {
            System.out.println("Error connecting to chat server: " + e.getMessage());
        }
    }

    private void performHandshake() throws IOException {
        while (true) {
            Message message = connection.receive();
            if (message.getType() == MessageType.NAME_REQUEST) {
                System.out.println(message.getData());
                userName = ConsoleHelper.readString();
                connection.send(new Message(MessageType.USER_NAME, userName));
            } else if (message.getType() == MessageType.NAME_ACCEPTED) {
                System.out.println(message.getData());
                break;
            } else if (message.getType() == MessageType.ERROR) {
                System.out.println("Error: " + message.getData());
                connection.close();
                break;
            } else {
                System.out.println("Unexpected message type: " + message.getType());
            }
        }
    }

    private void handleCommand(String command) throws IOException {
        if (command.equalsIgnoreCase("/help")) {
            showHelp();
        } else if (command.startsWith("/rename ")) {
            String newName = command.substring(8).trim();
            if (!newName.isEmpty()) {
                connection.send(new Message(MessageType.COMMAND, "/rename " + newName, userName));
            } else {
                System.out.println("Usage: /rename [new nickname]");
            }
        } else if (command.equalsIgnoreCase("/list")) {
            connection.send(new Message(MessageType.COMMAND, "/list", userName));
        } else if (command.equalsIgnoreCase("/exit")) {
            connection.close();
            System.exit(0);
        } else {
            System.out.println("Unknown command. Type /help for a list of commands.");
        }
    }

    private void showHelp() {
        System.out.println("Available commands:");
        System.out.println("/help           - Show this help message");
        System.out.println("/rename [name]  - Change your nickname");
        System.out.println("/list           - List users in the current chat room");
        System.out.println("/exit           - Exit the chat");
    }

    private class ReaderThread extends Thread {
        public void run() {
            while (true) {
                Message message = connection.receive();
                if (message == null) {
                    break;
                }
                switch (message.getType()) {
                    case TEXT:
                        String time = message.getTimestamp().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                        System.out.println("[" + time + "] " + message.getSender() + ": " + message.getData());
                        break;
                    case USER_ADDED, USER_REMOVED, NICKNAME_CHANGED, HELP_MESSAGE:
                        System.out.println(message.getData());
                        break;
                    case USER_LIST:
                        System.out.println("Users in chat: " + message.getData());
                        break;
                    case ERROR:
                        System.out.println("Error: " + message.getData());
                        break;
                    default:
                        System.out.println("Unexpected message type: " + message.getType());
                }
            }
        }
    }

    private String getServerAddress() {
        System.out.println("Enter server address:");
        return ConsoleHelper.readString();
    }

    private int getServerPort() {
        System.out.println("Enter server port:");
        return ConsoleHelper.readInt();
    }

    private int extractPortFromMessage(String message) {
        String[] tokens = message.split(" ");
        for (int i = 0; i < tokens.length; i++) {
            if (tokens[i].equalsIgnoreCase("port") && i + 1 < tokens.length) {
                try {
                    return Integer.parseInt(tokens[i + 1].replaceAll("[^0-9]", ""));
                } catch (NumberFormatException e) {
                    return -1;
                }
            }
        }
        return -1;
    }
}
