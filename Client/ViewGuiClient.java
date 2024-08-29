package Client;

import javax.swing.*;
import java.awt.*;
import java.util.Set;

public class ViewGuiClient {
    private final Client client;
    private JFrame frame = new JFrame("Chat");
    private JTextArea messages = new JTextArea(30, 20);
    private JTextArea users = new JTextArea(30, 15);
    private JPanel panel = new JPanel();
    private JTextField textField = new JTextField(40);
    private JButton buttonDisable = new JButton("Disconnect");
    private JButton buttonConnect = new JButton("Connect");

    public ViewGuiClient(Client client) {
        this.client = client;
    }

    // Method to initialize the graphical interface of the client application
    protected void initFrameClient() {
        messages.setEditable(false);
        users.setEditable(false);
        frame.add(new JScrollPane(messages), BorderLayout.CENTER);
        frame.add(new JScrollPane(users), BorderLayout.EAST);
        panel.add(textField);
        panel.add(buttonConnect);
        panel.add(buttonDisable);
        frame.add(panel, BorderLayout.SOUTH);
        frame.pack();
        frame.setLocationRelativeTo(null); // Display the window in the center of the screen on launch
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        // Event handling class for closing the application window
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                if (client.isConnect()) {
                    client.disableClient();
                }
                System.exit(0);
            }
        });

        frame.setVisible(true);

        buttonDisable.addActionListener(e -> client.disableClient());
        buttonConnect.addActionListener(e -> client.connectToServer());
        textField.addActionListener(e -> {
            client.sendMessageOnServer(textField.getText());
            textField.setText("");
        });
    }

    protected void addMessage(String text) {
        SwingUtilities.invokeLater(() -> messages.append(text));
    }

    // Method to update the list of connected user names
    protected void refreshListUsers(Set<String> listUsers) {
        SwingUtilities.invokeLater(() -> {
            if (client.isConnect()) {
                StringBuilder text = new StringBuilder("User List:\n");
                listUsers.forEach(user -> text.append(user).append("\n"));
                users.setText(text.toString());
            } else {
                users.setText(""); // Clear the user list if the client is not connected
            }
        });
    }

    // Opens a window for entering the server address
    protected String getServerAddressFromOptionPane() {
        while (true) {
            String addressServer = JOptionPane.showInputDialog(
                    frame, "Enter server address:",
                    "Server Address Entry",
                    JOptionPane.QUESTION_MESSAGE
            );
            return addressServer.trim();
        }
    }

    // Opens a window for entering the server port
    protected int getPortServerFromOptionPane() {
        while (true) {
            String port = JOptionPane.showInputDialog(
                    frame, "Enter server port:",
                    "Server Port Entry",
                    JOptionPane.QUESTION_MESSAGE
            );
            try {
                return Integer.parseInt(port.trim());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(
                        frame, "Invalid server port entered. Please try again.",
                        "Error entering server port", JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    // Opens windows for entering the user name
    protected String getNameUser() {
        return JOptionPane.showInputDialog(
                frame, "Enter your username:",
                "User Name Entry",
                JOptionPane.QUESTION_MESSAGE
        );
    }

    // Opens an error dialog window with the specified text
    protected void errorDialogWindow(String text) {
        JOptionPane.showMessageDialog(
                frame, text,
                "Error", JOptionPane.ERROR_MESSAGE
        );
    }
}
