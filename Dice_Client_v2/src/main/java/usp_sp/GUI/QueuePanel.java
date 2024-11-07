package usp_sp.GUI;

import usp_sp.Server.Connection;
import usp_sp.Server.Messeges;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

import static usp_sp.Utils.Const.ASSETS_GIF;

public class QueuePanel extends JPanel {
    private BufferedImage loadingGif;

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

        ImageIcon originalIcon = new ImageIcon( ASSETS_GIF + "loading_dice.gif");
        Image scaledImage = originalIcon.getImage().getScaledInstance(100, 100, Image.SCALE_DEFAULT);
        ImageIcon scaledIcon = new ImageIcon(scaledImage);
        JLabel loadingGif = new JLabel(scaledIcon);
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(loadingGif, gbc);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> {
            // Send on server info that someone left the Queue
//            Connection.getInstance().openSocket();
            Connection.getInstance().testConnection(Messeges.LOGOUT, Connection.getInstance().getPlayerName());
            Connection.getInstance().closeSocket();
            Window window = (Window) SwingUtilities.getWindowAncestor(this);
            window.showScene("Login");

            /* TODO: Send on server info that someone left the Queue */
        });

        /* TODO: Ask a server if other player is in queue */

        gbc.gridx = 0;
        gbc.gridy = 2;
        add(cancelButton, gbc);


    }
}