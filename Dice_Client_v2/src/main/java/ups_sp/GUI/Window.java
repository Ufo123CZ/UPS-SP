package ups_sp.GUI;

import ups_sp.Server.Connection;
import ups_sp.Server.Messages;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import static ups_sp.Utils.Const.WINDOW_HEIGHT;
import static ups_sp.Utils.Const.WINDOW_WIDTH;

/**
 * Window class
 * <p>
 * This class is a JFrame that contains the main panels of the game.
 * It contains the login, queue, game and help panels.
 */
public class Window extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;

    /**
     * Constructor
     */
    public Window() {
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        GamePanel gamePanel = new GamePanel();
        QueuePanel queuePanel = new QueuePanel(gamePanel);

        // Add the panels to the main panel
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
                if (Connection.getInstance().getSocket() != null) {
                    Connection.getInstance().makeContact(Messages.LOGOUT, "");
                    Connection.getInstance().closeSocket();
                }
                // Close the window
                System.exit(0);
                }
        });
    }

    /**
     * Show a scene
     * @param sceneName The name of the scene to show
     */
    public void showScene(String sceneName) {
        cardLayout.show(mainPanel, sceneName);
    }
}
