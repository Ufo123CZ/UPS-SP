package ups_sp.GUI;

import lombok.Getter;
import lombok.Setter;
import ups_sp.GameObjects.Board;
import ups_sp.GameObjects.Dice;
import ups_sp.GameObjects.PlayerStats;
import ups_sp.Server.Connection;
import ups_sp.Server.Messages;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.geom.AffineTransform;
import java.util.List;

import static java.lang.Thread.sleep;
import static ups_sp.Server.Messages.*;
import static ups_sp.Utils.Const.*;

/**
 * GamePanel class
 * <p>
 * This class is a JPanel that displays the game board and the player stats.
 * It contains the game logic and the controls for the game.
 */
public class GamePanel extends JPanel implements Connection.EventListenerGame {

    // Additional Buttons
    JButton endButtonBottom, endButtonUpper;
    @Setter
    JPanel statusPanel, upperPanel;
    JLabel statusLabel, upperLabel;

    // Globals
    private final Board board;
    @Getter
    private List<Dice[]> diceList;
    private final PlayerStats playerStatsP1;
    private final PlayerStats playerStatsP2;
    @Getter
    private List<PlayerStats> playerStatsList;

    // Player
    @Setter @Getter
    private String onMove = "P1";
    @Setter
    private boolean firstMoveInRound = false;
    @Setter
    private int selectedDice = -1;

    // Game End
    @Setter
    private boolean gameEnd = false;
    @Setter
    String winnerName = "";

    // Game Interrupt
    @Setter
    boolean gameStopped = false;
    @Setter
    boolean gameStoppedServer = false;
    @Setter
    boolean gameStoppedClient = false;

    /**
     * Constructor
     */
    public GamePanel() {
        this.setBackground(Color.WHITE);
        board = new Board();

        Dice[] dicesP1 = new Dice[6];
        Dice[] dicesP2 = new Dice[6];
        for (int i = 0; i < 6; i++) {
            dicesP1[i] = new Dice();
            dicesP2[i] = new Dice();
        }
        diceList = List.of(dicesP1, dicesP2);

        playerStatsP1 = new PlayerStats();
        playerStatsP2 = new PlayerStats();
        playerStatsList = List.of(playerStatsP1, playerStatsP2);

        // Register this panel as an event listener
        Connection.getInstance().setEventListenerGame(this);

        // Create a panel to hold the board and player stats
        JPanel centerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                GamePanel.this.paintComponent(g);
            }
        };

        // Set the layout of the center panel
        this.setLayout(new BorderLayout());
        // Add the center panel to the main panel
        this.add(centerPanel, BorderLayout.CENTER);
        // Init the bars
        initUpperBar();
        initLowerBar();
        // Add the bars to the main panel
        this.add(upperPanel, BorderLayout.NORTH);
        this.add(statusPanel, BorderLayout.SOUTH);

        controls();
    }

    //region Init Bars
    /**
     * Initialize the lower bar for the status of the game and possible moves
     */
    private void initLowerBar() {
        // Create the lower bar panel with BorderLayout
        statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBackground(Color.LIGHT_GRAY);
        statusPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        statusPanel.setPreferredSize(new Dimension(0, 20));

        // On the left side is the status of the game
        statusLabel = new JLabel("Status: ");
        statusLabel.setFont(new Font("Arial", Font.BOLD, 16));
        statusPanel.add(statusLabel, BorderLayout.WEST);

        // On the right side is the end game button
        endButtonBottom = new JButton("Endgame Options");
        endButtonBottom.setVisible(false);
        endButtonBottom.addActionListener(e -> showGameOverDialog(""));
        statusPanel.add(endButtonBottom, BorderLayout.EAST);
    }

    /**
     * Initialize the upper bar for the status of the game and special states (wins or disconnects)
     */
    private void initUpperBar() {
        // Create the upper bar panel with BorderLayout
        upperPanel = new JPanel(new BorderLayout());
        upperPanel.setBackground(Color.LIGHT_GRAY);
        upperPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        upperPanel.setPreferredSize(new Dimension(0, 20));

        // On the left side is the status of the game
        upperLabel = new JLabel("Player is disconnected!");
        upperLabel.setFont(new Font("Arial", Font.BOLD, 16));
        upperPanel.add(upperLabel, BorderLayout.WEST);

        // On the right side is the end game button
        endButtonUpper = new JButton("Disconnect Options");
        endButtonUpper.setVisible(false);
        endButtonUpper.addActionListener(e -> showGameStatusDialog(""));
        upperPanel.add(endButtonUpper, BorderLayout.EAST);
    }
    //endregion

    /**
     * Paint the components of the game
     * @param g Graphics
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Original Transform
        AffineTransform originalTransform = g2d.getTransform();

        // SetUp Graphics
        prepareGraphics(g2d);

        // Board
        board.setG2d(g2d);
        board.drawBoard();
        board.drawBoardText();

        // Set the Graphics for the Dices
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 2; j++) {
                diceList.get(j)[i].setG2d(g2d);
            }
        }

        // Draw the Dices
        board.drawDices(diceList);

        // Player Stats Graphics
        playerStatsP1.setG2d(g2d);
        playerStatsP2.setG2d(g2d);
        for (int i = 0; i < 2; i++) {
            AffineTransform old = g2d.getTransform();
            if (i == 0) {
                g2d.translate(BOARD_SIZE / 2f - PLAYER_STATS_SIZE / 0.6f, -BOARD_SIZE / 2f);
            } else {
                g2d.translate(BOARD_SIZE / 2f - PLAYER_STATS_SIZE / 0.6f, BOARD_SIZE / 25f);
            }
            // Set the status Bar
            if (Connection.getInstance().getPlayerName().equals(onMove)) {
                if (!firstMoveInRound) {
                    statusLabel.setText("Status: Press SPACE to throw dices");
                } else {
                    statusLabel.setText("Status: Use A and D to select dice, E to select, F to next throw, Q to end turn");
                }
            } else {
                statusLabel.setText("Status: " + onMove + " is playing");
            }
            playerStatsList.get(i).drawStats(onMove);
            g2d.setTransform(old);
        }

        g2d.setTransform(originalTransform);
    }

    //region Scale and Translate to Center
    /**
     * Prepare the Graphics for the panel
     * Moves draw point on center and scale the panel
     * @param g2d Graphics2D
     */
    private void prepareGraphics(Graphics2D g2d) {
        // Anti-aliasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Get the size of the panel
        float width = getWidth();
        float height = getHeight();

        // Translate the origin to the center of the panel
        g2d.translate(width / 2, height / 2);

        // Scale based on the size of the window
        double scaleX = width / WINDOW_WIDTH;
        double scaleY = height / WINDOW_HEIGHT;
        double scale = Math.min(scaleX, scaleY);
        g2d.scale(scale, scale);
    }
    //endregion

    //region Controls
    /**
     * Set the controls for the game
     * A - Move Left
     * D - Move Right
     * E - Select Dice
     * F - Next Throw
     * Q - End Turn
     * SPACE - Throw Dices
     * T - Help
     */
    private void controls() {
        setFocusable(true);
        requestFocusInWindow();

        InputMap inputMap = getInputMap(WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getActionMap();

        inputMap.put(KeyStroke.getKeyStroke("A"), "MoveLeft");
        inputMap.put(KeyStroke.getKeyStroke("D"), "MoveRight");
        inputMap.put(KeyStroke.getKeyStroke("E"), "SelectDice");
        inputMap.put(KeyStroke.getKeyStroke("F"), "NextThrow");
        inputMap.put(KeyStroke.getKeyStroke("Q"), "EndTurn");
        inputMap.put(KeyStroke.getKeyStroke("SPACE"), "ThrowDices");
        inputMap.put(KeyStroke.getKeyStroke("T"), "Help");

        actionMap.put("MoveLeft", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (firstMoveInRound && !gameEnd && !gameStoppedServer && !gameStoppedClient && !gameStopped) {
                    if (Connection.getInstance().getPlayerName().equals(onMove)) {
                        int who = whoIsPlaying();
                        int leftest = getTheLeftest();
                        if (selectedDice == -1 || selectedDice == leftest) {
                            if (selectedDice != -1) {
                                diceList.get(who)[selectedDice].setHover(false);
                            }
                            selectedDice = leftest;
                            diceList.get(who)[selectedDice].setHover(true);
                        } else {
                            diceList.get(who)[selectedDice].setHover(false);
                            do {
                                selectedDice--;
                            } while (selectedDice >= leftest && diceList.get(who)[selectedDice].isHold());
                            diceList.get(who)[selectedDice].setHover(true);
                        }
                        repaint();
                    }
                }
            }
        });

        actionMap.put("MoveRight", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (firstMoveInRound && !gameEnd && !gameStoppedServer && !gameStoppedClient && !gameStopped) {
                    if (Connection.getInstance().getPlayerName().equals(onMove)) {
                        int who = whoIsPlaying();
                        int rightest = getTheRightest();
                        if (selectedDice == -1 || selectedDice == rightest) {
                            if (selectedDice != -1) {
                                diceList.get(who)[selectedDice].setHover(false);
                            }
                            selectedDice = rightest;
                            diceList.get(who)[selectedDice].setHover(true);
                        } else {
                            diceList.get(who)[selectedDice].setHover(false);
                            do {
                                selectedDice++;
                            } while (selectedDice <= rightest && diceList.get(who)[selectedDice].isHold());
                            diceList.get(who)[selectedDice].setHover(true);
                        }
                        repaint();
                    }
                }
            }
        });

        actionMap.put("SelectDice", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (firstMoveInRound && !gameEnd && !gameStoppedServer && !gameStoppedClient && !gameStopped) {
                    if (Connection.getInstance().getPlayerName().equals(onMove)) {
                        int who = whoIsPlaying();
                        if (selectedDice != -1 && diceList.get(who)[selectedDice].isSelected()) {
                            Connection.getInstance().makeContact(Messages.GAME_SELECT_DICE, diceList.get(who)[selectedDice].getDiceId());
                        } else if (selectedDice != -1 && !diceList.get(who)[selectedDice].isSelected()) {
                            Connection.getInstance().makeContact(Messages.GAME_SELECT_DICE, diceList.get(who)[selectedDice].getDiceId());
                        }
                        repaint();
                    }
                }
            }
        });

        actionMap.put("NextThrow", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (firstMoveInRound && isAtleastOneDiceSelected() && throwScoreIsntZero() && !gameEnd && !gameStoppedServer && !gameStoppedClient && !gameStopped) {
                    if (Connection.getInstance().getPlayerName().equals(onMove)) {
                        Connection.getInstance().makeContact(Messages.GAME_NEXT_TURN, "");
                        disableHover();
                    }
                }
            }
        });

        actionMap.put("EndTurn", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (firstMoveInRound && isAtleastOneDiceSelected() && throwScoreIsntZero() && !gameEnd && !gameStoppedServer && !gameStoppedClient && !gameStopped) {
                    if (Connection.getInstance().getPlayerName().equals(onMove)) {
                        Connection.getInstance().makeContact(Messages.GAME_END_TURN, "");
                        disableHover();
                    }
                }
            }
        });

        actionMap.put("ThrowDices", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!firstMoveInRound && !gameEnd && !gameStoppedServer && !gameStoppedClient && !gameStopped) {
                    if (Connection.getInstance().getPlayerName().equals(onMove)) {
                        Connection.getInstance().makeContact(Messages.GAME_THROW_DICE, "");
                        firstMoveInRound = true;
                    }
                }
            }
        });

        actionMap.put("Help", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Window window = (Window) SwingUtilities.getWindowAncestor(GamePanel.this);
                window.showScene("Help");
            }
        });
    }
    //endregion

    //region Controls Functions
    /**
     * Get the player who is playing
     * @return int - 0 for P1, 1 for P2
     */
    private int whoIsPlaying() {
        int who = -1;
        if (onMove.equals(playerStatsList.get(0).name)) {
            who = 0;
        } else if (onMove.equals(playerStatsList.get(1).name)) {
            who = 1;
        }
        return who;
    }

    /**
     * Get the leftest selectable dice
     * @return int - index of the leftest dice
     */
    private int getTheLeftest() {
        int leftest = 0;
        for (int i = 0; i < 6; i++) {
            if (!diceList.get(whoIsPlaying())[i].isHold()) {
                leftest = i;
                break;
            }
        }
        return leftest;
    }

    /**
     * Get the rightest selectable dice
     * @return int - index of the rightest dice
     */
    private int getTheRightest() {
        int rightest = 5;
        for (int i = 5; i >= 0; i--) {
            if (!diceList.get(whoIsPlaying())[i].isHold()) {
                rightest = i;
                break;
            }
        }
        return rightest;
    }

    /**
     * Check if at least one dice is selected
     * @return boolean - true if at least one dice is selected
     */
    private boolean isAtleastOneDiceSelected() {
        for (int i = 0; i < 6; i++) {
            if (diceList.get(whoIsPlaying())[i].isSelected()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if the throw score isn't zero
     * @return boolean - true if the throw score isn't zero
     */
    private boolean throwScoreIsntZero() {
        for (int i = 0; i < 2; i++) {
            if (playerStatsList.get(i).getThrowScore() != 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Disable the hover on the dices
     */
    private void disableHover() {
        selectedDice = -1;
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 2; j++) {
                diceList.get(j)[i].setHover(false);
            }
        }
    }

    //endregion

    //region Update Game
    /**
     * Update the game with the information from the server
     * @param information String - Information from the server
     */
    public void updateGame(String information) {
        // information TAG;PLAYER;SCORE_T,SCORE_ST,SCORE_TH;DICE1_id,DICE1_val, ...|DICE2|...|DICE6|;SWITCH_PLAYER;
        // Parse the message to get the information
        String[] parts = information.split(";");
        // parts[0] is tag GAME_<UPDATE>
        // parts[1] is the player who changed
        for (int who = 0; who < playerStatsList.size(); who++) {
            if (playerStatsList.get(who).name.equals(parts[1])) {
                statusLabel.setText("Status: " + parts[1] + " is playing");
                // Player Score Update
                if (!parts[2].equals(" ")) {
                    String[] playerInfo = parts[2].split(",");
                    playerStatsList.get(who).setTotalScore(Integer.parseInt(playerInfo[0]));
                    playerStatsList.get(who).setSubtotalScore(Integer.parseInt(playerInfo[1]));
                    playerStatsList.get(who).setThrowScore(Integer.parseInt(playerInfo[2]));
                }

                // Dice Update
                if (!parts[3].equals(" ")) {
                    String[] dices = parts[3].split("\\|");
                    for (String s : dices) {
                        String[] dice = s.split(",");
                        // Find dice to update
                        for (int j = 0; j < diceList.get(who).length; j++) {
                            if (diceList.get(who)[j].getDiceId().equals(dice[0])) {
                                diceList.get(who)[j].setDiceValue(Integer.parseInt(dice[1]));
                                diceList.get(who)[j].setSelected(dice[2].equals("1"));
                                diceList.get(who)[j].setHold(dice[3].equals("1"));
                                break;
                            }
                        }
                    }
                }

                // Throw boolean
                String[] throwsB = parts[4].split(",");
                firstMoveInRound = throwsB[who].equals("1");

                // Switch Player
                if (!parts[5].equals(" ")) {
                    onMove = parts[5];
                    statusLabel.setText("Status: " + parts[5] + " is playing");
                }

                // Unique Information
                if (!parts[6].equals(" ")) {
                    if (parts[6].contains(GAME_STATE_WINNER)) {
                        String[] winner = parts[6].split(":");
                        winnerName = winner[1];
                    }
                }
            }
        }

        // Check if the game is ended
        if (!winnerName.isEmpty()) {
            gameEnd = true;
            statusLabel.setText("Status: " + winnerName + " is the winner!");
            endButtonBottom.setVisible(true);
            gameEnd();
        }

        // Repaint the panel
        repaint();
    }

    /**
     * Update the game when the player reconnects
     * @param message String - Information from the server
     */
    private void updateGameOnReconnect(String message) {
        new Thread(() -> {
            gameStopped = false;
            gameEnd = false;
            gameStoppedClient = false;
            gameStoppedServer = false;
            winnerName = "";
            upperPanel.setVisible(false);

            Connection.getInstance().setStatus(1);

            // Parse the message to get the information
            String[] parts = message.split(";");
            // parts[0] is tag GAME_CREATED
            for (int i = 1; i < parts.length - 3; i++) {
                // Player Stats
                PlayerStats playerStats = playerStatsList.get(i - 1);

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
                    diceList.get(i - 1)[j].setDiceId(diceParts[0]);
                    diceList.get(i - 1)[j].setDiceValue(Integer.parseInt(diceParts[1]));
                    diceList.get(i - 1)[j].setSelected(diceParts[2].equals("1"));
                    diceList.get(i - 1)[j].setHold(diceParts[3].equals("1"));
                }
            }

            // Set currentPlayer
            onMove = parts[parts.length - 3];

            //States of the game
            String gameStates = parts[parts.length - 2];
            String[] states = gameStates.split(",");

            if (onMove.equals(Connection.getInstance().getPlayerName())) {
                for (int i = 0; i < states.length; i++) {
                    if (playerStatsList.get(i).getName().equals(onMove)) {
                        firstMoveInRound = states[i].equals("1");
                        break;
                    } else {
                        firstMoveInRound = false;
                    }
                }
            }

            // Game is Stopped
            String gameIsStopped = parts[parts.length - 1];
            if (gameIsStopped.equals("1")) {
                gameStopped = true;
                upperPanel.setVisible(true);
                upperLabel.setText("Player is disconnected!");
            } else {
                gameStopped = false;
                upperPanel.setVisible(false);
                upperLabel.setText("");
            }

            // Repaint the panel
            repaint();

            // Switch to the game scene
            Window window = (Window) SwingUtilities.getWindowAncestor(this);
            window.showScene("Game");
        }).start();
    }
    //endregion

    //region Game Status
    /**
     * Show the game over dialog
     * @param winner String - Name of the winner
     */
    private void showGameOverDialog(String winner) {
        if (winnerName.isEmpty()) {
            return;
        }
        statusLabel.setText("Status: " + winnerName + " is the winner!");
        endButtonBottom.setVisible(false);
        String message = "YOU LOSE!";
        if (winner.equals(Connection.getInstance().getPlayerName())) {
            message = "YOU WON!";
        }
        Object[] options = {"Return to Login Screen", "Queue for another game"};
        int option = JOptionPane.showOptionDialog(
                this,
                message + "\nDo you want to return to login or queue for another game?",
                "Game Over",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );
        optionPanel(option, endButtonBottom);
    }

    /**
     * Show the game status dialog
     * @param disconnected String - Name of the disconnected player
     */
    private void showGameStatusDialog(String disconnected) {
        // get name of the disconnected player
        statusLabel.setText("Player " + disconnected + " is disconnected!");
        endButtonUpper.setVisible(false);
        Object[] options = {"Return to Login Screen", "Queue for another game"};
        int option = JOptionPane.showOptionDialog(
                this,
                 statusLabel.getText() + "\nDo you want to return to login or queue for another game?",
                "Game Over",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );
        optionPanel(option, endButtonUpper);
    }

    /**
     * Handle the options from the game status dialog
     * @param option int - Option selected
     * @param endButtonUpper JButton - Button to show the dialog
     */
    private void optionPanel(int option, JButton endButtonUpper) {
        if (option == 0) {
            new Thread(() -> {
                try {
                    Connection.getInstance().makeContact(Messages.LOGOUT, "");
                    sleep(500);
                    Connection.getInstance().closeSocket();
                    Window window = (Window) SwingUtilities.getWindowAncestor(this);
                    window.showScene("Login");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        } else if (option == 1) {
            Connection.getInstance().makeContact(Messages.QUEUE_REJOIN, "");
        } else {
            endButtonUpper.setVisible(true);
        }
    }

    /**
     * Show the game end dialog
     */
    private void gameEnd() {
        SwingUtilities.invokeLater(() -> {
            endButtonBottom.setVisible(false);
            showGameOverDialog(winnerName);
        });
    }
    //endregion

    /**
     * Update the game with the information from the server
     * @param message String - Information from the server (TAG and information)
     */
    @Override
    public void onMessageReceivedGame(String message) {
        new Thread(() -> {
            if (message.contains(GAME_PLAYER_LEFT) && !Connection.getInstance().isReconnecting()) { // Player Left
                System.out.println("Update Game with message: " + message);
                upperPanel.setVisible(true);
                gameStoppedServer = true;
                String disconected = message.split(";")[1];
                showGameStatusDialog(disconected);
            }
            if (message.contains(GAME_PLAYER_JOINED) && !Connection.getInstance().isReconnecting()) { // Player Joined
                System.out.println("Update Game with message: " + message);
                upperPanel.setVisible(false);
                gameStoppedServer = false;
                gameStopped = false;
            }
            if ((   // Game Update
                    message.contains(GAME_THROW_DICE) ||
                    message.contains(GAME_SELECT_DICE) ||
                    message.contains(GAME_NEXT_TURN) ||
                    message.contains(GAME_END_TURN))
                    && !Connection.getInstance().isReconnecting()) {
                System.out.println("Update Game with message: " + message);
                updateGame(message);
            }
            if (message.contains(QUEUE_REJOIN) && !Connection.getInstance().isReconnecting()) { // Rejoin Queue
                System.out.println("Update Game with message: " + message);
                Connection.getInstance().setStatus(0);
                Window window = (Window) SwingUtilities.getWindowAncestor(this);
                window.showScene("Queue");
            }
            if (message.contains(GAME_PLAYER_DISCONNECTED) && !Connection.getInstance().isReconnecting()) { // Player Disconnected
                System.out.println("Update Game with message: " + message);
                gameStopped = true;
                upperPanel.setVisible(true);
                String disconected = message.split(";")[1];
                upperLabel.setText("Player: " + disconected + " is disconnected!");
            }

            if (message.contains(CONNECTION_LOST)) { // Connection Lost - Stop Game
                gameStoppedClient = true;
                new Thread(() -> JOptionPane.showMessageDialog(this, "Connection Lost. Reconnecting...", "Error", JOptionPane.ERROR_MESSAGE)).start();
                upperPanel.setVisible(true);
                upperLabel.setText("Connection Lost");
            }
            if (message.contains(CONNECTION_RECONNECT) && !message.contains(NOGAME)) { // Reconnect - Sync Game
                updateGameOnReconnect(message);
                gameStoppedClient = false;
                System.out.println("Game synchronized with message: " + message);
            }
            if (message.contains(CONNECTION_ALIVE) && Connection.getInstance().isReconnecting()) { // Connection Alive - Resume Game
                System.out.println("Connection is alive with message: " + message);
                Connection.getInstance().makeContact(CONNECTION_RECONNECT, "");
                Connection.getInstance().setReconnecting(false);
                gameStoppedClient = false;
                upperPanel.setVisible(false);
                upperLabel.setText("");
                new Thread(() -> JOptionPane.showMessageDialog(this, "Connection Retrieved", "Error", JOptionPane.INFORMATION_MESSAGE)).start();
            }
        }).start();
    }
}