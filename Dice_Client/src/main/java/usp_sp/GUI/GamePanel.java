package usp_sp.GUI;

import usp_sp.GameObjects.Board;
import usp_sp.GameObjects.Dice;

import javax.swing.*;
import java.awt.*;

import static usp_sp.Utils.Const.*;

public class GamePanel extends JPanel {

    public GamePanel() {}

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // SetUp Graphics
        prepareGraphics(g2d);

        // Board
        Board board = new Board(g2d);
        board.drawBoard();

        // Draw the Dices
         Dice dice = new Dice(g2d);
         dice.drawDice();
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
}