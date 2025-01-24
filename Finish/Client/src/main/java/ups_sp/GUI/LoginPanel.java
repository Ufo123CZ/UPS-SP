package ups_sp.GUI;

import ups_sp.Server.Connection;
import ups_sp.Server.Messages;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;

import static ups_sp.Server.Messages.SERVER_ERROR;

/**
 * LoginPanel class
 * <p>
 * This class is a JPanel that displays the login information for the game.
 * It contains the fields for the user to input their name, IP and port.
 */
public class LoginPanel extends JPanel implements Connection.EventListenerLogin {
    private JTextField nameField;
    private JTextField ipField;
    private JTextField portField;
    private JButton connectButton;

    /**
     * Constructor
     *
     * @param window The window object
     */
    public LoginPanel(Window window) {
        this.setBackground(Color.WHITE);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;

        JLabel nameLabel = new JLabel("Name:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(nameLabel, gbc);

        nameField = new JTextField(13);
        //region Limit name field to 13 characters
        ((AbstractDocument) nameField.getDocument()).setDocumentFilter(new DocumentFilter() {
            private static final int MAX_CHARS = 13;

            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                if ((fb.getDocument().getLength() + string.length()) <= MAX_CHARS) {
                    super.insertString(fb, offset, string, attr);
                }
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                if ((fb.getDocument().getLength() + text.length() - length) <= MAX_CHARS) {
                    super.replace(fb, offset, length, text, attrs);
                }
            }
        });
        //endregion
        gbc.gridx = 1;
        gbc.gridy = 0;
        add(nameField, gbc);

        JLabel ipLabel = new JLabel("IP:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(ipLabel, gbc);

        ipField = new JTextField("127.0.0.1",20);
        gbc.gridx = 1;
        gbc.gridy = 1;
        add(ipField, gbc);

        JLabel portLabel = new JLabel("Port:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        add(portLabel, gbc);

        portField = new JTextField("10000", 20);
        gbc.gridx = 1;
        gbc.gridy = 2;
        add(portField, gbc);

        connectButton = new JButton("Connect");
        connectButton.addActionListener(e -> {
            // Check if all fields are filled
            if (nameField.getText().isEmpty() || ipField.getText().isEmpty() || portField.getText().isEmpty()) {
                JOptionPane.showMessageDialog(window, "Please fill all fields", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            // Check if port is a number
            if (!portField.getText().matches("\\d{1,5}") || Integer.parseInt(portField.getText()) < 0 || Integer.parseInt(portField.getText()) > 65535) {
                JOptionPane.showMessageDialog(window, "Port must be a valid number between 0 and 65535", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Check if name is valid
            // no /  or ; or \ or | or , or \n or \r
            if (nameField.getText().contains("/") || nameField.getText().contains(";") ||
                    nameField.getText().contains("\\") || nameField.getText().contains("|") ||
                    nameField.getText().contains(",") || nameField.getText().contains("\n") ||
                    nameField.getText().contains("\r")) {
                JOptionPane.showMessageDialog(window, "Name cannot contain /, ;, \\, |, ,, \\n or \\r", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Connect to server
            Connection.getInstance().setServerDetails(ipField.getText(), Integer.parseInt(portField.getText()), nameField.getText(), 0);
            if (!Connection.getInstance().openSocket()) {
                JOptionPane.showMessageDialog(window, "Cannot connect to the server.", "Error", JOptionPane.ERROR_MESSAGE);
            }

        });
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        add(connectButton, gbc);

        // Register this panel as an event listener
        Connection.getInstance().setEventListenerLogin(this);
    }

    /**
     * Update the game with the information from the server
     * @param message String - Information from the server (TAG and information)
     */
    @Override
    public void onMessageReceivedLogin(String message) {
        if (message.contains(Messages.LOGIN)) { // Check if the message is a login message
            if (message.contains(Messages.ERROR)) { // Check if the message is an error
                System.out.println("Received: " + message);
                JOptionPane.showMessageDialog(this, "Connection failed.", "Error", JOptionPane.ERROR_MESSAGE);
                Connection.getInstance().closeSocket();
                Window window = (Window) SwingUtilities.getWindowAncestor(this);
                window.showScene("Login");
            }
            if (message.contains(Messages.SUCCESS)) { // Check if the message is a success
                System.out.println("Connected to the server.");
                Connection.getInstance().makeContact(Messages.NAMESET, nameField.getText());
            }
        }
        if (message.contains(Messages.NAMESET)) { // Check if the message is a name set message
            if (message.contains(Messages.SUCCESS)) { // Check if the message is a success
                System.out.println("Received: " + message);
                Connection.getInstance().setStatus(0);
                Window window = (Window) SwingUtilities.getWindowAncestor(this);
                window.showScene("Queue");
                JOptionPane.showMessageDialog(this, "Connected to the server.", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
            if (message.contains(Messages.ERROR)) { // Check if the message is an error
                System.out.println("Received: " + message);
                JOptionPane.showMessageDialog(this, "Name already in use.", "Error", JOptionPane.ERROR_MESSAGE);
                Connection.getInstance().closeSocket();
                Window window = (Window) SwingUtilities.getWindowAncestor(this);
                window.showScene("Login");
            }
        }

            if (message.contains(SERVER_ERROR)) { // Check if the message is a server error
                JOptionPane.showMessageDialog(this, "Connection Lost", "Error", JOptionPane.ERROR_MESSAGE);
                Connection.getInstance().closeSocket();
                Window window = (Window) SwingUtilities.getWindowAncestor(this);
                window.showScene("Login");
            }

            if (message.contains(Messages.TERMINATE)) { // Check if the message is a terminate message -> FORCED DISCONNECT
                System.out.println("Received: " + message);
                Connection.getInstance().setStatus(-1);
                Connection.getInstance().closeSocket();
                JOptionPane.showMessageDialog(this, "Violation detected --> Disconnecting", "Error", JOptionPane.ERROR_MESSAGE);
                Window window = (Window) SwingUtilities.getWindowAncestor(this);
                window.showScene("Login");
            }
    }

}