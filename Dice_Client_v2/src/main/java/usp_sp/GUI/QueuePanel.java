package usp_sp.GUI;

import lombok.Getter;
import lombok.Setter;
import usp_sp.Server.Connection;
import usp_sp.Server.Messeges;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import static usp_sp.Utils.Const.ASSETS_GIF;

public class QueuePanel extends JPanel {
    private BufferedImage loadingGif;
    @Getter
    private Timer timer;

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
            Connection.getInstance().makeContact(Messeges.LOGOUT, Connection.getInstance().getPlayerName());
            Connection.getInstance().setStatus(-1);
            Connection.getInstance().closeSocket();
            Window window = (Window) SwingUtilities.getWindowAncestor(this);
            window.showScene("Login");

            if (timer != null) {
                timer.stop();
            }
        });

        gbc.gridx = 0;
        gbc.gridy = 2;
        add(cancelButton, gbc);

        timer = new Timer(5000, this::actionPerformed);
        timer.setInitialDelay(5000);
//        timer.start();

//        timer = new Timer(5000, e -> {
//            System.out.println("Checking queue status...");
//            Object[] result = Connection.getInstance().makeContact(Messeges.QUEUE_STATUS, Connection.getInstance().getPlayerName());
//            if (result != null) {
//                if (result[0].equals("1")) {
//                    Window window = (Window) SwingUtilities.getWindowAncestor(this);
//                    window.showScene("Game");
//                    timer.stop();
//                }
//            }
//        });
    }

    public void actionPerformed(ActionEvent e) {
        System.out.println("Checking queue status...");
        Object[] result = Connection.getInstance().makeContact(Messeges.QUEUE_STATUS, "");
        if (result != null) {
            if (result[0].equals("1")) {
                Window window = (Window) SwingUtilities.getWindowAncestor(this);
                window.showScene("Game");
                timer.stop();
            }
        }
    }
}