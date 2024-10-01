package usp_sp.GUI;

import usp_sp.Server.Connection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginPanel extends JPanel {
    private JTextField nameField;
    private JTextField ipField;
    private JTextField portField;
    private JButton connectButton;

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
        gbc.gridx = 1;
        gbc.gridy = 0;
        add(nameField, gbc);

        JLabel ipLabel = new JLabel("IP:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(ipLabel, gbc);

        ipField = new JTextField(20);
        gbc.gridx = 1;
        gbc.gridy = 1;
        add(ipField, gbc);

        JLabel portLabel = new JLabel("Port:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        add(portLabel, gbc);

        portField = new JTextField(20);
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
                if (!portField.getText().matches("[0-9]+")) {
                    JOptionPane.showMessageDialog(window, "Port must be a number", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Connect to server
                Connection connection = Connection.getInstance();
                connection.setServerDetails(ipField.getText(), Integer.parseInt(portField.getText()));
                if (!connection.checkConnection()) {
                    JOptionPane.showMessageDialog(window, "Connection failed", "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(window, "Connection successful", "Success", JOptionPane.INFORMATION_MESSAGE);
                    window.showScene("Lobby");
                }
                // Show loading screen
                //window.showScene("Queue");

                /* TODO: Send on server info that someone joined */

                // Switch to game screen
//                window.showScene("Game");
            }
        });
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        add(connectButton, gbc);
    }
}