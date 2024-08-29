package Server;

import Connection.*;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Server {
    private ServerSocket serverSocket;
    private static ViewGuiServer gui; // Object of the view class
    private static ModelGuiServer model; // Object of the model class
    private static volatile boolean isServerStart = false; // Flag indicating the server's state: started/stopped

    // Method to start the server
    protected void startServer(int port) {
        try {
            serverSocket = new ServerSocket(port);
            isServerStart = true;
            gui.refreshDialogWindowServer("Server is running.\n");
        } catch (Exception e) {
            gui.refreshDialogWindowServer("Failed to start the server.\n");
        }
    }

    // Method to stop the server
    protected void stopServer() {
        try {
            // If the server socket has a reference and is not closed
            if (serverSocket != null && !serverSocket.isClosed()) {
                for (Map.Entry<String, Connection> user : model.getAllUsersMultiChat().entrySet()) {
                    user.getValue().close();
                }
                serverSocket.close();
                model.getAllUsersMultiChat().clear();
                gui.refreshDialogWindowServer("Server stopped.\n");
            } else gui.refreshDialogWindowServer("Server is not running - nothing to stop!\n");
        } catch (Exception e) {
            gui.refreshDialogWindowServer("Failed to stop the server.\n");
        }
    }

    // Method where the server, in an infinite loop, accepts new socket connections from clients
    protected void acceptServer() {
        while (true) {
            try {
                Socket socket = serverSocket.accept();
                new ServerThread(socket).start();
            } catch (Exception e) {
                gui.refreshDialogWindowServer("Connection with the server is lost.\n");
                break;
            }
        }
    }

    // Method to send a specified message to all clients in the map
    protected void sendMessageAllUsers(Message message) {
        for (Map.Entry<String, Connection> user : model.getAllUsersMultiChat().entrySet()) {
            try {
                user.getValue().send(message);
            } catch (Exception e) {
                gui.refreshDialogWindowServer("Error sending message to all users!\n");
            }
        }
    }

    // Entry point for the server application
    public static void main(String[] args) {
        Server server = new Server();
        gui = new ViewGuiServer(server);
        model = new ModelGuiServer();
        gui.initFrameServer();
        // The loop below waits for true from the isServerStart flag
        // (set to true when starting the server in the startServer method)
        // Then, it starts an infinite loop to accept connections from clients in the acceptServer method
        // Until the server stops or an exception occurs
        while (true) {
            if (isServerStart) {
                server.acceptServer();
                isServerStart = false;
            }
        }
    }

    // Thread class that is started when the server accepts a new socket connection from a client,
    // and a Socket object is passed to the constructor
    private class ServerThread extends Thread {
        private Socket socket;

        public ServerThread(Socket socket) {
            this.socket = socket;
        }

        // Method implementing the server's request to the client for a name and adding the name to the map
        private String requestAndAddingUser(Connection connection) {
            while (true) {
                try {
                    // Send the client a name request message
                    connection.send(new Message(MessageType.REQUEST_NAME_USER));
                    Message responseMessage = connection.receive();
                    String userName = responseMessage.getTextMessage();
                    // Receive the response with the name and check if the name is not already taken by another client
                    if (responseMessage.getTypeMessage() == MessageType.USER_NAME && userName != null && !userName.isEmpty() && !model.getAllUsersMultiChat().containsKey(userName)) {
                        // Add the name to the map
                        model.addUser(userName, connection);
                        Set<String> listUsers = new HashSet<>();
                        for (Map.Entry<String, Connection> users : model.getAllUsersMultiChat().entrySet()) {
                            listUsers.add(users.getKey());
                        }
                        // Send the client the set of names of all already connected users
                        connection.send(new Message(MessageType.NAME_ACCEPTED, listUsers));
                        // Send a message to all clients about the new user
                        sendMessageAllUsers(new Message(MessageType.USER_ADDED, userName));
                        return userName;
                    }
                    // If the name is already taken, send a message to the client that the name is in use
                    else connection.send(new Message(MessageType.NAME_USED));
                } catch (Exception e) {
                    gui.refreshDialogWindowServer("An error occurred requesting and adding a new user\n");
                }
            }
        }

        // Method implementing the exchange of messages between users
        private void messagingBetweenUsers(Connection connection, String userName) {
            while (true) {
                try {
                    Message message = connection.receive();
                    // Received a message from the client, if the message type is TEXT_MESSAGE, forward it to all users
                    if (message.getTypeMessage() == MessageType.TEXT_MESSAGE) {
                        String textMessage = String.format("%s: %s\n", userName, message.getTextMessage());
                        sendMessageAllUsers(new Message(MessageType.TEXT_MESSAGE, textMessage));
                    }
                    // If the message type is DISABLE_USER, broadcast to all users that this user left the chat,
                    // remove the user from the map, and close their connection
                    if (message.getTypeMessage() == MessageType.DISABLE_USER) {
                        sendMessageAllUsers(new Message(MessageType.REMOVED_USER, userName));
                        model.removeUser(userName);
                        connection.close();
                        gui.refreshDialogWindowServer(String.format("User with remote access %s disconnected.\n", socket.getRemoteSocketAddress()));
                        break;
                    }
                } catch (Exception e) {
                    gui.refreshDialogWindowServer(String.format("An error occurred while broadcasting a message from user %s, or the user disconnected!\n", userName));
                    break;
                }
            }
        }

        @Override
        public void run() {
            gui.refreshDialogWindowServer(String.format("New user connected with remote socket - %s.\n", socket.getRemoteSocketAddress()));
            try {
                // Get the connection using the received socket from the client and request a name, register it,
                // and start the message exchange loop between users
                Connection connection = new Connection(socket);
                String nameUser = requestAndAddingUser(connection);
                messagingBetweenUsers(connection, nameUser);
            } catch (Exception e) {
                gui.refreshDialogWindowServer("An error occurred while broadcasting a message from the user!\n");
            }
        }
    }
}
