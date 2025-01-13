package ups_sp.GUI;

import ups_sp.Server.Connection;
import ups_sp.Server.Messages;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import static ups_sp.Utils.Const.WINDOW_HEIGHT;
import static ups_sp.Utils.Const.WINDOW_WIDTH;

public class Window extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;

    public Window() {
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        GamePanel gamePanel = new GamePanel();
        QueuePanel queuePanel = new QueuePanel(gamePanel);

        mainPanel.add(new LoginPanel(this), "Login");
        mainPanel.add(queuePanel, "Queue");
        mainPanel.add(gamePanel, "Game");
        mainPanel.add(new HelpPanel(), "Help");

        add(mainPanel);
        setTitle("KCD Dice Client");
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        setLocationRelativeTo(null);

        // Add window listener to handle logout on close
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
            Connection.getInstance().makeContact(Messages.LOGOUT, "");
            Connection.getInstance().closeSocket();
            // Close the window
            System.exit(0);
            }
        });
    }

    public void showScene(String sceneName) {
        cardLayout.show(mainPanel, sceneName);
    }
}
