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

        // Draw the Dices
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
        inputMap.put(KeyStroke.getKeyStroke("E"), "HoldDice");
        inputMap.put(KeyStroke.getKeyStroke("F"), "NextThrow");
        inputMap.put(KeyStroke.getKeyStroke("Q"), "EndTurn");
        inputMap.put(KeyStroke.getKeyStroke("T"), "Help");
        inputMap.put(KeyStroke.getKeyStroke("SPACE"), "ThrowDices");

        actionMap.put("MoveLeft", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int leftest = getTheLeftest();
                if (selectedDice == -1 || selectedDice == leftest) {
                    if (selectedDice != -1) {
                        diceList.get(whoIsPlaying())[selectedDice].setHover(false);
                    }
                    selectedDice = leftest;
                    diceList.get(whoIsPlaying())[selectedDice].setHover(true);
                } else {
                    diceList.get(whoIsPlaying())[selectedDice].setHover(false);
                    do {
                        selectedDice--;
                    } while (selectedDice >= leftest && diceList.get(whoIsPlaying())[selectedDice].isSelected());
                    diceList.get(whoIsPlaying())[selectedDice].setHover(true);
                }
                repaint();
            }
        });

        actionMap.put("MoveRight", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int rightest = getTheRightest();
                if (selectedDice == -1 || selectedDice == rightest) {
                    if (selectedDice != -1) {
                        diceList.get(whoIsPlaying())[selectedDice].setHover(false);
                    }
                    selectedDice = rightest;
                    diceList.get(whoIsPlaying())[selectedDice].setHover(true);
                } else {
                    diceList.get(whoIsPlaying())[selectedDice].setHover(false);
                    do {
                        selectedDice++;
                    } while (selectedDice <= rightest && diceList.get(whoIsPlaying())[selectedDice].isSelected());
                    diceList.get(whoIsPlaying())[selectedDice].setHover(true);
                }
                repaint();
            }
        });

        actionMap.put("HoldDice", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int whoIsPlaying = whoIsPlaying();
                if (selectedDice != -1 && diceList.get(whoIsPlaying)[selectedDice].isHold()) {
                    diceList.get(whoIsPlaying)[selectedDice].setHold(false);
                } else if (selectedDice != -1 && !diceList.get(whoIsPlaying)[selectedDice].isHold()) {
                    diceList.get(whoIsPlaying)[selectedDice].setHold(true);
                }

                /* TODO: Send holded dices to server and check if they give player some score */
                repaint();
            }
        });

        actionMap.put("NextThrow", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Pressed F");
                /* TODO: Send all holded dices to server after checking that the holded ones have value */
            }
        });

        actionMap.put("EndTurn", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Pressed Q");
                /* TODO: Send subtotal score to server and switch players */
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
                String result = "";
                if (Connection.getInstance().getPlayerName().equals(onMove)) {
                    result = Connection.getInstance().makeContact(Messages.GAME_THROW_DICE, "");
                }
                if (!result.isEmpty()) updateGame(result);
            }
        });
    }
    //endregion

    //region Controls Functions
    private int whoIsPlaying() {
        return onMove.equals("P1") ? 0 : 1;
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
        // Parse the message to get the information
        String[] parts = information.split(";");
        // parts[0] is tag GAME_<UPDATE>
        // parts[1] is the player who changed
        int who = 0;
        for (PlayerStats playerStats : playerStatsList) {
            if (playerStats.name.equals(parts[1])) {
                for (int i = 2; i < parts.length - 1; i++) {
                    String[] dice = parts[i].split(",");
                    // Find dice to update
                    for (int j = 0; j < 6; j++) {
                        if (diceList.get(who)[j].getDiceId().equals(dice[0])) {
                            diceList.get(who)[j].setDiceValue(Integer.parseInt(dice[1]));
                            diceList.get(who)[j].setSelected(dice[2].equals("1"));
                            diceList.get(who)[j].setHold(dice[3].equals("1"));
                            break;
                        }
                    }
                }
            }
            who++;
        }
        repaint();
    }
    //endregion

    @Override
    public void onMessageReceivedGame(String message) {
        new Thread(() -> {
            try {
                sleep(500);
                if (message.contains(Messages.GAME_THROW_DICE)) {
                    System.out.println("Update Game with message: " + message);
                    updateGame(message);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }
}