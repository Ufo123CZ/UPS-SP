package usp_sp.GUI;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static usp_sp.Utils.Const.*;

public class GamePanel extends JPanel {
    private final TexturePaint textureBoard, textureEdge, textureHinge, screwTexture;

    public GamePanel() {
        // Load the textures
        textureBoard = loadTexture("board.jpg");
        textureEdge = loadTexture("edge.jpg");
        textureHinge = loadTexture("hinge.jpg");
        screwTexture = loadTexture("screw.jpg");

    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Prepare the board
        prepareBoard(g2d);

        // Draw the board
        drawBoard(g2d);
    }

    //region Scale and Translate to Center
    private void prepareBoard(Graphics2D g2d) {
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

    //region Draw Board
    private void drawBoard(Graphics2D g2d) {
        // Components
        Rectangle2D.Float edge = new Rectangle2D.Float(
                -BOARD_SIZE / 1.8f, -BOARD_SIZE / 1.8f,
                (BOARD_SIZE / 1.8f) * 2f, (BOARD_SIZE / 1.8f) * 2f
        );

        Rectangle2D hingeEdge = new Rectangle2D.Float(
                -BOARD_SIZE / 2f, -BOARD_SIZE / 25f,
                BOARD_SIZE, BOARD_SIZE / 12.5f
        );

        Rectangle2D.Float board = new Rectangle2D.Float(
                -BOARD_SIZE / 2f, -BOARD_SIZE / 2f,
                BOARD_SIZE, BOARD_SIZE
        );

        Rectangle2D field = new Rectangle2D.Float(
                -BOARD_SIZE / 2f, -BOARD_SIZE / 2f,
                BOARD_SIZE, BOARD_SIZE / 2f - (BOARD_SIZE / 25f)
        );

        Line2D.Float gap = new Line2D.Float(-BOARD_SIZE / 1.8f, 0, BOARD_SIZE / 1.8f, 0);

        // Hinge
        Rectangle2D.Float hinge = new Rectangle2D.Float(
                -BOARD_SIZE / 20f, -BOARD_SIZE / 30f,
                BOARD_SIZE / 10f, BOARD_SIZE / 15f
        );

        Rectangle2D.Float hingeJoint = new Rectangle2D.Float(
                -BOARD_SIZE / 30f, -BOARD_SIZE / 150f,
                BOARD_SIZE / 40f, BOARD_SIZE / 75f
        );

        Line2D.Float hingeLine = new Line2D.Float(
                -BOARD_SIZE / 20f, 0,
                BOARD_SIZE / 10f, 0
        );

        Line2D.Float hingeJointLineL = new Line2D.Float(
                -BOARD_SIZE / 30f, -BOARD_SIZE / 150f,
                -BOARD_SIZE / 30f, BOARD_SIZE / 150f
        );

        Line2D.Float hingeJointLineR = new Line2D.Float(
                -BOARD_SIZE / 30f + BOARD_SIZE / 25f, -BOARD_SIZE / 150f,
                -BOARD_SIZE / 30f + BOARD_SIZE / 25f, BOARD_SIZE / 150f
        );

        Ellipse2D.Float screw = new Ellipse2D.Float(
                -BOARD_SIZE / 25f, -BOARD_SIZE / 40f,
                BOARD_SIZE / 75f, BOARD_SIZE / 75f
        );

        g2d.setPaint(textureEdge);
        g2d.fill(edge);
        g2d.setPaint(textureBoard);
        g2d.fill(board);
        g2d.setPaint(textureEdge);
        g2d.fill(hingeEdge);
        Color edgeColor = new Color(0, 0, 0, 255);
        g2d.setPaint(edgeColor);
        g2d.setStroke(new BasicStroke(3));
        g2d.draw(edge);
        g2d.draw(field);

        // Save the current transform
        AffineTransform center = g2d.getTransform();

        // Draw edges of fields
        g2d.scale(1, -1);
        g2d.draw(field);

        // Restore the transform
        g2d.setTransform(center);

        g2d.draw(gap);
        g2d.setPaint(textureHinge);

        g2d.setStroke(new BasicStroke(1));

        // Hinge
        for (int i = 0; i < 2; i++) {
            if (i == 1) {
                g2d.scale(-1, 1);
            }
            g2d.translate(-BOARD_SIZE / 3f, 0);

            g2d.setPaint(textureHinge);
            g2d.fill(hinge);

            g2d.setPaint(edgeColor);
            g2d.draw(hingeLine);

            AffineTransform hingeTransform = g2d.getTransform();
            for (int j = 0; j < 2; j++) {
                if (j == 1) {
                    g2d.scale(-1, 1);
                }
                g2d.setPaint(textureHinge);
                g2d.fill(hingeJoint);
                g2d.setPaint(edgeColor);
                g2d.draw(hingeJointLineL);
                g2d.draw(hingeJointLineR);
                g2d.setTransform(hingeTransform);
            }

            // Screws
            g2d.setPaint(screwTexture);
            for (int j = 0; j < 2; j++) {
                g2d.fill(screw);
                g2d.scale(-1, 1);
                g2d.fill(screw);
                g2d.scale(1, -1);
            }

            g2d.setTransform(center);
        }
    }
    //endregion

    //region Load Texture
    private TexturePaint loadTexture(String fileName) {
        try {
            BufferedImage textureImage = ImageIO.read(new File("src/main/resources/Assets/" + fileName));
            Rectangle2D rect = new Rectangle2D.Float(0, 0, textureImage.getWidth(), textureImage.getHeight());
            return new TexturePaint(textureImage, rect);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    //endregion
}