package usp_sp.GUI;

import usp_sp.Server.Connection;
import usp_sp.Server.Messeges;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginPanel extends JPanel {
    private JTextField nameField;
    private JTextField ipField;
    private JTextField portField;
    private JButton connectButton;
    private LobbyPanel lobbyPanel;

    public LoginPanel(Window window) {
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

        ipField = new JTextField("localhost",20);
        gbc.gridx = 1;
        gbc.gridy = 1;
        add(ipField, gbc);

        JLabel portLabel = new JLabel("Port:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        add(portLabel, gbc);

        portField = new JTextField("8080", 20);
        gbc.gridx = 1;
        gbc.gridy = 2;
        add(portField, gbc);

        connectButton = new JButton("Connect");
        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Check if all fields are filled
                if (nameField.getText().isEmpty() || ipField.getText().isEmpty() || portField.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(window, "Please fill all fields", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                // Check if port is a number
                if (!portField.getText().matches("[0-9]+") || Integer.parseInt(portField.getText()) < 0 || Integer.parseInt(portField.getText()) > 65535) {
                    JOptionPane.showMessageDialog(window, "Port must be a valid number", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Connect to server
                Connection connection = Connection.getInstance();
                connection.setServerDetails(ipField.getText(), Integer.parseInt(portField.getText()), nameField.getText());
                Object[] response = connection.makeConnection(Messeges.LOGIN + connection.getPlayerName());
                if (response[0].equals(false)) {
                    JOptionPane.showMessageDialog(window, "Connection failed", "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(window, "Connection successful", "Success", JOptionPane.INFORMATION_MESSAGE);
                    window.showScene("Lobby");
                    LobbyPanel.updateTableData(LobbyPanel.getDataForTable(Messeges.FETCH_LOBBY));
                }
            }
        });
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        add(connectButton, gbc);
    }
}