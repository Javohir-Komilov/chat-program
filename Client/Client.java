package Client;

import Connection.*;

import java.io.IOException;
import java.net.Socket;

public class Client {
    private Connection connection;
    private static ModelGuiClient model;
    private static ViewGuiClient gui;
    private volatile boolean isConnect = false; // Flag indicating the client's connection state to the server

    private static final String SERVICE_MESSAGE_CONNECTED = "Service message: You have connected to the server.\n";
    private static final String SERVICE_MESSAGE_NAME_ACCEPTED = "Service message: Your name has been accepted!\n";
    private static final String ERROR_MESSAGE_ALREADY_CONNECTED = "You are already connected.";
    private static final String ERROR_MESSAGE_CONNECTING = "An error occurred! Perhaps you entered the wrong server address or port. Please try again.";
    private static final String ERROR_MESSAGE_NAME_IN_USE = "This name is already in use. Please enter another name.";
    private static final String ERROR_MESSAGE_NAME_REGISTRATION = "An error occurred during name registration. Please try reconnecting.";
    private static final String ERROR_MESSAGE_CLOSING_CONNECTION = "Error closing the connection";
    private static final String ERROR_MESSAGE_SENDING_MESSAGE = "Error sending message";
    private static final String ERROR_MESSAGE_RECEIVING_MESSAGE = "Error receiving message from the server.";
    private static final String SERVICE_MESSAGE_DISCONNECT_ERROR = "Service message: An error occurred while disconnecting.";

    public boolean isConnect() {
        return isConnect;
    }

    public void setConnect(boolean connect) {
        isConnect = connect;
    }

    // Entry point for the client application
    public static void main(String[] args) {
        Client client = new Client();
        model = new ModelGuiClient();
        gui = new ViewGuiClient(client);
        gui.initFrameClient();
        while (true) {
            if (client.isConnect()) {
                client.nameUserRegistration();
                client.receiveMessageFromServer();
                client.setConnect(false);
            }
        }
    }

    // Method to connect the client to the server
    protected void connectToServer() {
        // If the client is not connected to the server...
        if (!isConnect) {
            // Display input windows for server address and port
            String addressServer = gui.getServerAddressFromOptionPane();
            if (addressServer == null) {
                // User clicked "Cancel" in the server address input window
                return;
            }
            Integer port = gui.getPortServerFromOptionPane();
            try {
                // Create a socket and connection object
                Socket socket = new Socket(addressServer, port);
                connection = new Connection(socket);
                isConnect = true;
                gui.addMessage(SERVICE_MESSAGE_CONNECTED);
            } catch (Exception e) {
                gui.errorDialogWindow(ERROR_MESSAGE_CONNECTING);
            }
        } else {
            gui.errorDialogWindow(ERROR_MESSAGE_ALREADY_CONNECTED);
        }
    }

    // Method implementing user name registration on the client side
    protected void nameUserRegistration() {
        while (true) {
            try {
                Message message = connection.receive();
                // Received a message from the server; if it's a user name request, display input window for the name and send it to the server
                if (message.getTypeMessage() == MessageType.REQUEST_NAME_USER) {
                    String nameUser = gui.getNameUser();
                    connection.send(new Message(MessageType.USER_NAME, nameUser));
                }
                // If the message is "name already in use," display an error dialog, prompt for another name, and send it to the server
                if (message.getTypeMessage() == MessageType.NAME_USED) {
                    gui.errorDialogWindow(ERROR_MESSAGE_NAME_IN_USE);
                    String nameUser = gui.getNameUser();
                    connection.send(new Message(MessageType.USER_NAME, nameUser));
                }
                // If the name is accepted, get the set of all connected users and exit the loop
                if (message.getTypeMessage() == MessageType.NAME_ACCEPTED) {
                    gui.addMessage(SERVICE_MESSAGE_NAME_ACCEPTED);
                    model.setUsers(message.getListUsers());
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
                gui.errorDialogWindow(ERROR_MESSAGE_NAME_REGISTRATION);
                try {
                    connection.close();
                    isConnect = false;
                    break;
                } catch (IOException ex) {
                    gui.errorDialogWindow(ERROR_MESSAGE_CLOSING_CONNECTION);
                }
            }
        }
    }

    // Method for sending a message to other users on the server
    protected void sendMessageOnServer(String text) {
        try {
            connection.send(new Message(MessageType.TEXT_MESSAGE, text));
        } catch (Exception e) {
            gui.errorDialogWindow(ERROR_MESSAGE_SENDING_MESSAGE);
        }
    }

    // Method for receiving messages from other clients on the server
    protected void receiveMessageFromServer() {
        while (isConnect) {
            try {
                Message message = connection.receive();
                // If the message type is TEXT_MESSAGE, add the message text to the chat window
                if (message.getTypeMessage() == MessageType.TEXT_MESSAGE) {
                    gui.addMessage(message.getTextMessage());
                }
                // If the message is of type USER_ADDED, add a message to the chat window about a new user
                if (message.getTypeMessage() == MessageType.USER_ADDED) {
                    model.addUser(message.getTextMessage());
                    gui.refreshListUsers(model.getUsers());
                    gui.addMessage(String.format("Service message: User %s has joined the chat.\n", message.getTextMessage()));
                }
                // Similarly for disconnecting other users
                if (message.getTypeMessage() == MessageType.REMOVED_USER) {
                    model.removeUser(message.getTextMessage());
                    gui.refreshListUsers(model.getUsers());
                    gui.addMessage(String.format("Service message: User %s has left the chat.\n", message.getTextMessage()));
                }
            } catch (Exception e) {
                gui.errorDialogWindow(ERROR_MESSAGE_RECEIVING_MESSAGE);
                setConnect(false);
                gui.refreshListUsers(model.getUsers());
                break;
            }
        }
    }

    // Method implementing the disconnection of our client from the chat
    protected void disableClient() {
        try {
            if (isConnect) {
                connection.send(new Message(MessageType.DISABLE_USER));
                model.getUsers().clear();
                isConnect = false;
                gui.refreshListUsers(model.getUsers());
            } else gui.errorDialogWindow("You are already disconnected.");
        } catch (Exception e) {
            gui.errorDialogWindow(SERVICE_MESSAGE_DISCONNECT_ERROR);
        }
    }
}
