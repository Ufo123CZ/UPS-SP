package usp_sp.GUI;

import lombok.Getter;
import lombok.Setter;
import usp_sp.GameObjects.Board;
import usp_sp.GameObjects.Dice;
import usp_sp.GameObjects.PlayerStats;
import usp_sp.Server.Connection;
import usp_sp.Server.Messages;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.geom.AffineTransform;
import java.util.List;

import static java.lang.Thread.sleep;
import static usp_sp.Utils.Const.*;

public class GamePanel extends JPanel implements Connection.EventListenerGame {

    // Globals
    private Board board;

    private Dice[] dicesP1;
    private Dice[] dicesP2;
    @Getter
    private List<Dice[]> diceList;

    private PlayerStats playerStatsP1;
    private PlayerStats playerStatsP2;
    @Getter
    private List<PlayerStats> playerStatsList;

    // Player
    @Setter
    private String onMove = "P1";
    private boolean firstMoveInRound = false;
    private int selectedDice = -1;

    public GamePanel() {
        board = new Board();

        dicesP1 = new Dice[6];
        dicesP2 = new Dice[6];
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

        controls();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

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
            playerStatsList.get(i).drawStats(onMove);
            g2d.setTransform(old);
        }
    }

    //region Scale and Translate to Center
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
        inputMap.put(KeyStroke.getKeyStroke("T"), "Help");
        inputMap.put(KeyStroke.getKeyStroke("SPACE"), "ThrowDices");

        actionMap.put("MoveLeft", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (firstMoveInRound) {
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
                if (firstMoveInRound) {
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
                if (firstMoveInRound) {
                    if (Connection.getInstance().getPlayerName().equals(onMove)) {
                        int who = whoIsPlaying();
                        if (selectedDice != -1 && diceList.get(who)[selectedDice].isSelected()) {
                            diceList.get(who)[selectedDice].setSelected(false);
                        } else if (selectedDice != -1 && !diceList.get(who)[selectedDice].isSelected()) {
                            diceList.get(who)[selectedDice].setSelected(true);
                        }
                        repaint();
                    }
                }
            }
        });

        actionMap.put("NextThrow", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Pressed F");
            }
        });

        actionMap.put("EndTurn", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (firstMoveInRound) {
                    if (Connection.getInstance().getPlayerName().equals(onMove)) {
                        String result = Connection.getInstance().makeContact(Messages.GAME_END_TURN, "");
                        if (!result.isEmpty()) updateGame(result);
                    }
                }
            }
        });

        actionMap.put("Help", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        });

        actionMap.put("ThrowDices", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!firstMoveInRound) {
                    if (Connection.getInstance().getPlayerName().equals(onMove)) {
                        String result = Connection.getInstance().makeContact(Messages.GAME_THROW_DICE, "");
                        if (!result.isEmpty()) {
                            updateGame(result);
                            firstMoveInRound = true;
                        }
                    }
                }
            }
        });
    }
    //endregion

    //region Controls Functions
    private int whoIsPlaying() {
        int who = -1;
        if (onMove.equals(playerStatsList.get(0).name)) {
            who = 0;
        } else if (onMove.equals(playerStatsList.get(1).name)) {
            who = 1;
        }
        return who;
    }

    private int getTheLeftest() {
        int leftest = 0;
        for (int i = 0; i < 6; i++) {
            if (!diceList.get(whoIsPlaying())[i].isSelected()) {
                leftest = i;
                break;
            }
        }
        return leftest;
    }

    private int getTheRightest() {
        int rightest = 5;
        for (int i = 5; i >= 0; i--) {
            if (!diceList.get(whoIsPlaying())[i].isSelected()) {
                rightest = i;
                break;
            }
        }
        return rightest;
    }

    void updateGame(String information) {

        // information TAG;PLAYER;SCORE_T,SCORE_ST,SCORE_TH;DICE1_id,DICE1_val, ...|DICE2|...|DICE6|;SWITCH_PLAYER;

        // Parse the message to get the information
        String[] parts = information.split(";");
        // parts[0] is tag GAME_<UPDATE>
        // parts[1] is the player who changed
        for (int who = 0; who < playerStatsList.size(); who++) {
            if (playerStatsList.get(who).name.equals(parts[1])) {
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

                // Switch Player
                if (!parts[4].equals(" ")) {
                    onMove = parts[4];
                    firstMoveInRound = false;
                }

                // Reset selected dice
                selectedDice = -1;
            }
        }
        repaint();
    }
    //endregion

    @Override
    public void onMessageReceivedGame(String message) {
        new Thread(() -> {
            if (message.contains(Messages.GAME_THROW_DICE)) {
                System.out.println("Update Game with message: " + message);
                updateGame(message);
            }

            if (message.contains(Messages.GAME_END_TURN)) {
                System.out.println("Update Game with message: " + message);
                updateGame(message);
            }
        }).start();
    }
}