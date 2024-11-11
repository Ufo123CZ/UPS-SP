package usp_sp.GUI;

import usp_sp.Server.Connection;
import usp_sp.Server.Messages;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import static usp_sp.Utils.Const.WINDOW_HEIGHT;
import static usp_sp.Utils.Const.WINDOW_WIDTH;

public class Window extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;

    public Window() {
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        mainPanel.add(new LoginPanel(this), "Login");
        mainPanel.add(new QueuePanel(), "Queue");
        mainPanel.add(new GamePanel(), "Game");

        add(mainPanel);
        setTitle("KCD Dice Client");
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Add window listener to handle logout on close
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (Connection.getInstance().getStatus() > -1) {
                    Connection.getInstance().makeContact(Messages.LOGOUT, Connection.getInstance().getPlayerName());
                    Connection.getInstance().setStatus(-1);
                    Connection.getInstance().closeSocket();
                }
            }
        });
    }

    public void showScene(String sceneName) {
        cardLayout.show(mainPanel, sceneName);

        if (sceneName.equals("Queue")) {
            QueuePanel queuePanel = (QueuePanel) mainPanel.getComponent(1);
        }

    }
}
