package usp_sp.GUI;

import javax.swing.*;
import java.awt.*;

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
    }

    public void showScene(String sceneName) {
        cardLayout.show(mainPanel, sceneName);
    }
}
