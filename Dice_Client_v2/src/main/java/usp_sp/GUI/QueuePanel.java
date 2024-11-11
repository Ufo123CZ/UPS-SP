package usp_sp.GUI;

import usp_sp.Server.Connection;
import usp_sp.Server.Messages;

import javax.swing.*;
import java.awt.*;
import static usp_sp.Utils.Const.ASSETS_GIF;

public class QueuePanel extends JPanel {
    public QueuePanel() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;

        JLabel loadingLabel = new JLabel("Waiting for opponent to connect...");
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(loadingLabel, gbc);

        ImageIcon originalIcon = new ImageIcon(ASSETS_GIF + "loading_dice.gif");
        Image scaledImage = originalIcon.getImage().getScaledInstance(100, 100, Image.SCALE_DEFAULT);
        ImageIcon scaledIcon = new ImageIcon(scaledImage);
        JLabel loadingGif = new JLabel(scaledIcon);
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(loadingGif, gbc);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> {
            Connection.getInstance().makeContact(Messages.LOGOUT, Connection.getInstance().getPlayerName());
            Connection.getInstance().setStatus(-1);
            Connection.getInstance().closeSocket();
            Window window = (Window) SwingUtilities.getWindowAncestor(this);
            window.showScene("Login");
        });

        gbc.gridx = 0;
        gbc.gridy = 2;
        add(cancelButton, gbc);
    }
}