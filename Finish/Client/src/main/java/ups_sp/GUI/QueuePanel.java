package ups_sp.GUI;

import ups_sp.GameObjects.PlayerStats;
import ups_sp.Server.Connection;
import ups_sp.Server.Messages;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

import static ups_sp.Server.Messages.GAME_CREATED;
import static ups_sp.Utils.Const.ASSETS_GIF;

/**
 * QueuePanel class
 * <p>
 * This class is a JPanel that displays the queue information for the game.
 * It contains the loading gif and a cancel button.
 */
public class QueuePanel extends JPanel implements Connection.EventListenerQueue {

    // Reference to the GamePanel
    private GamePanel gamePanel;

    /**
     * Constructor
     * @param gamePanel The gamePanel object
     */
    public QueuePanel(GamePanel gamePanel) {
        this.setBackground(Color.WHITE);
        this.gamePanel = gamePanel;
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;

        JLabel loadingLabel = new JLabel("Waiting for opponent to connect...");
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(loadingLabel, gbc);

        ImageIcon originalIcon = new ImageIcon(Objects.requireNonNull(getClass().getResource(ASSETS_GIF + "loading_dice.gif")));
        Image scaledImage = originalIcon.getImage().getScaledInstance(100, 100, Image.SCALE_DEFAULT);
        ImageIcon scaledIcon = new ImageIcon(scaledImage);
        JLabel loadingGif = new JLabel(scaledIcon);
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(loadingGif, gbc);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> {
            Connection.getInstance().makeContact(Messages.LOGOUT, "");
            Connection.getInstance().closeSocket();
            Window window = (Window) SwingUtilities.getWindowAncestor(this);
            window.showScene("Login");
        });

        gbc.gridx = 0;
        gbc.gridy = 2;
        add(cancelButton, gbc);

        // Register this panel as an event listener
        Connection.getInstance().setEventListenerQueue(this);
    }

    /**
     * Update the game with the information from the server
     * @param message String - Information from the server (TAG and information)
     */
    @Override
    public void onMessageReceivedQueue(String message) {
        new Thread(() -> {
            // Read game information to set up the game
            if ((message.contains(GAME_CREATED) || message.contains(Messages.GAME_RECONNECTED)) && !Connection.getInstance().isReconnecting()) {
                System.out.println("Game Created with: " + message);
                // Reset Local variables
                gamePanel.setFirstMoveInRound(false);
                gamePanel.setSelectedDice(-1);
                // Set all dices to not Hover
                for (int i = 0; i < gamePanel.getDiceList().size(); i++) {
                    for (int j = 0; j < gamePanel.getDiceList().get(i).length; j++) {
                        gamePanel.getDiceList().get(i)[j].setHover(false);
                    }
                }

                gamePanel.setGameStopped(false);
                gamePanel.setGameEnd(false);
                gamePanel.setGameStoppedClient(false);
                gamePanel.setGameStoppedServer(false);
                gamePanel.setWinnerName("");
                gamePanel.upperPanel.setVisible(false);

                Connection.getInstance().setStatus(1);

                // Parse the message to get the information
                String[] parts = message.split(";");
                // parts[0] is tag GAME_CREATED
                for (int i = 1; i < parts.length - 2; i++) {
                    // Player Stats
                    PlayerStats playerStats = gamePanel.getPlayerStatsList().get(i - 1);

                    String[] playerInfo = parts[i].split("\\|");

                    // Name
                    playerStats.setName(playerInfo[0]);

                    // Scores
                    String[] playerScores = playerInfo[1].split(",");
                    playerStats.setTotalScore(Integer.parseInt(playerScores[0]));
                    playerStats.setSubtotalScore(Integer.parseInt(playerScores[1]));
                    playerStats.setThrowScore(Integer.parseInt(playerScores[2]));

                    // Dices
                    String[] dices = playerInfo[2].split("/");
                    for (int j = 0; j < dices.length; j++) {
                        String[] diceParts = dices[j].split(",");
                        gamePanel.getDiceList().get(i - 1)[j].setDiceId(diceParts[0]);
                        gamePanel.getDiceList().get(i - 1)[j].setDiceValue(Integer.parseInt(diceParts[1]));
                        gamePanel.getDiceList().get(i - 1)[j].setSelected(diceParts[2].equals("1"));
                        gamePanel.getDiceList().get(i - 1)[j].setHold(diceParts[3].equals("1"));
                    }
                }

                // Set currentPlayer
                gamePanel.setOnMove(parts[parts.length - 2]);

                //States of the game
                String gameStates = parts[parts.length - 1];
                String[] states = gameStates.split(",");

                if (gamePanel.getOnMove().equals(Connection.getInstance().getPlayerName())) {
                    for (int i = 0; i < states.length; i++) {
                        if (gamePanel.getPlayerStatsList().get(i).getName().equals(gamePanel.getOnMove())) {
                            gamePanel.setFirstMoveInRound(states[i].equals("1"));
                            break;
                        } else {
                            gamePanel.setFirstMoveInRound(false);
                        }
                    }
                }
                gamePanel.repaint();

                Window window = (Window) SwingUtilities.getWindowAncestor(this);
                window.showScene("Game");
            }

            if (message.contains(Messages.CONNECTION_RECONNECTED) && message.contains(Messages.NOGAME)) {
                System.out.println("Connection Reconnected with: " + message);
                Connection.getInstance().setStatus(0);
            }
        }).start();
    }
}