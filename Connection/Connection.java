package Connection;

import java.io.*;
import java.net.Socket;

public class Connection implements Closeable {
    private final Socket socket;
    private final ObjectOutputStream out;
    private final ObjectInputStream in;

    public Connection(Socket socket) throws IOException {
        this.socket = socket;
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.in = new ObjectInputStream(socket.getInputStream());
    }

    // Method to send a message over the socket connection
    public void send(Message message) throws IOException {
        synchronized (this.out) {
            out.writeObject(message);
            out.flush(); // Ensure all data is sent immediately
        }
    }

    // Method to receive a message over the socket connection
    public Message receive() throws IOException, ClassNotFoundException {
        synchronized (this.in) {
            Message message = (Message) in.readObject();
            return message;
        }
    }

    // Method to close the reading, writing, and socket streams
    @Override
    public void close() throws IOException {
        try {
            in.close();
        } finally {
            try {
                out.close();
            } finally {
                socket.close();
            }
        }
    }
}
