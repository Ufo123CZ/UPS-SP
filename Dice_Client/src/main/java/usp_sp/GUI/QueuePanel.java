package usp_sp.GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

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

        ImageIcon originalIcon = new ImageIcon("src/main/resources/Assets/loading_dice.gif");
        Image scaledImage = originalIcon.getImage().getScaledInstance(100, 100, Image.SCALE_DEFAULT);
        ImageIcon scaledIcon = new ImageIcon(scaledImage);
        JLabel loadingGif = new JLabel(scaledIcon);
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(loadingGif, gbc);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> {
            Window window = (Window) SwingUtilities.getWindowAncestor(this);
            window.showScene("Login");

            /** TODO: Send on server info that someone left the Queue */
        });

        gbc.gridx = 0;
        gbc.gridy = 2;
        add(cancelButton, gbc);


    }
}