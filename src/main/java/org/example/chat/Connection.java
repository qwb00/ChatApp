package org.example.chat;

import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Connection implements Closeable {
    final private Socket socket;
    final private ObjectOutputStream out;
    final private ObjectInputStream in;

    public Connection(Socket socket) {
        this.socket = socket;
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void send(Message message) throws IOException {
        synchronized (out) {
            out.writeObject(message);
        }
    }

    public Message receive() {
        Message message = null;
        try {
            synchronized (in) {
                message = (Message) in.readObject();
            }
        } catch (IOException | ClassNotFoundException e) {
            ConsoleHelper.writeMessage("An error occurred while receiving a message.");
        }
        return message;
    }

    @Override
    public void close() {
        try {
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            ConsoleHelper.writeMessage("An error occurred while closing the connection.");
        }
    }
}
