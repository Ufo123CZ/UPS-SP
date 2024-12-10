package usp_sp.GameObjects;

import lombok.Setter;
import usp_sp.Utils.Materials;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

import static usp_sp.Utils.Colours.*;
import static usp_sp.Utils.Const.*;

public class Board extends Materials {

    private final TexturePaint textureBoard, textureBoardEdge, textureHinge, screwTexture, textureEdges, textureText;
    @Setter // Local
    private Graphics2D g2d;

    // Graphics2D Object
    Rectangle2D.Float edge;
    Rectangle2D.Float hingeEdge;
    Rectangle2D.Float board;
    Rectangle2D.Float field;
    Line2D.Float gap;
    Rectangle2D.Float hinge;
    Rectangle2D.Float hingeJoint;
    Line2D.Float hingeLine;
    Line2D.Float hingeJointLineL;
    Line2D.Float hingeJointLineR;
    Ellipse2D.Float screw;

    public Board() {
        // Load the textures
        textureBoard = loadTexture("oak.jpg");
        textureBoardEdge = loadTexture("spruce.jpg");
        textureHinge = loadTexture("iron.jpg");
        screwTexture = loadTexture("steel.jpg");
        textureEdges = loadTexture("dark_iron.jpg");
        textureText = loadTexture("birch.jpg");

        initGO();
    }

    private void initGO() {
        // Components
        edge = new Rectangle2D.Float(
                -BOARD_SIZE / 1.8f, -BOARD_SIZE / 1.8f,
                (BOARD_SIZE / 1.8f) * 2f, (BOARD_SIZE / 1.8f) * 2f
        );

        hingeEdge = new Rectangle2D.Float(
                -BOARD_SIZE / 2f, -BOARD_SIZE / 25f,
                BOARD_SIZE, BOARD_SIZE / 12.5f
        );

        board = new Rectangle2D.Float(
                -BOARD_SIZE / 2f, -BOARD_SIZE / 2f,
                BOARD_SIZE, BOARD_SIZE
        );

        field = new Rectangle2D.Float(
                -BOARD_SIZE / 2f, -BOARD_SIZE / 2f,
                BOARD_SIZE, BOARD_SIZE / 2f - (BOARD_SIZE / 25f)
        );

        gap = new Line2D.Float(-BOARD_SIZE / 1.8f, 0, BOARD_SIZE / 1.8f, 0);

        // Hinge
        hinge = new Rectangle2D.Float(
                -BOARD_SIZE / 20f, -BOARD_SIZE / 30f,
                BOARD_SIZE / 10f, BOARD_SIZE / 15f
        );

        hingeJoint = new Rectangle2D.Float(
                -BOARD_SIZE / 30f, -BOARD_SIZE / 150f,
                BOARD_SIZE / 40f, BOARD_SIZE / 75f
        );

        hingeLine = new Line2D.Float(
                -BOARD_SIZE / 20f, 0,
                BOARD_SIZE / 10f, 0
        );

        hingeJointLineL = new Line2D.Float(
                -BOARD_SIZE / 30f, -BOARD_SIZE / 150f,
                -BOARD_SIZE / 30f, BOARD_SIZE / 150f
        );

        hingeJointLineR = new Line2D.Float(
                -BOARD_SIZE / 30f + BOARD_SIZE / 25f, -BOARD_SIZE / 150f,
                -BOARD_SIZE / 30f + BOARD_SIZE / 25f, BOARD_SIZE / 150f
        );

        screw = new Ellipse2D.Float(
                -BOARD_SIZE / 25f, -BOARD_SIZE / 40f,
                BOARD_SIZE / 75f, BOARD_SIZE / 75f
        );
    }

    public void drawBoard() {
        g2d.setPaint(textureBoardEdge);
        g2d.fill(edge);
        g2d.setPaint(textureBoard);
        g2d.fill(board);
        g2d.setPaint(textureBoardEdge);
        g2d.fill(hingeEdge);
        g2d.setPaint(textureEdges);
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

            g2d.setPaint(textureEdges);
            g2d.draw(hingeLine);

            AffineTransform hingeTransform = g2d.getTransform();
            for (int j = 0; j < 2; j++) {
                if (j == 1) {
                    g2d.scale(-1, 1);
                }
                g2d.setPaint(textureHinge);
                g2d.fill(hingeJoint);
                g2d.setPaint(textureEdges);
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

    public void drawBoardText() {
        AffineTransform old = g2d.getTransform();
        g2d.setFont(GAME_TEXT_FONT);
        g2d.setPaint(textureText);

        g2d.translate(-BOARD_SIZE / 2.05f, -BOARD_SIZE / 2.5f);
        g2d.drawString(GAME_TEXT_SEL, 0, -DICE_SIZE / 1.55f);
        g2d.drawString(GAME_TEXT_TH, 0, DICE_SIZE / 0.45f);
        g2d.setTransform(old);

        g2d.translate(-BOARD_SIZE / 2.05f, BOARD_SIZE / 7f);
        g2d.drawString(GAME_TEXT_SEL, 0, -DICE_SIZE / 1.55f);
        g2d.drawString(GAME_TEXT_TH, 0, DICE_SIZE / 0.45f);
        g2d.setTransform(old);
    }

    public void drawDices(List<Dice[]> diceList) {
        AffineTransform old = g2d.getTransform();
        for (int i = 0; i < 2; i++) {
            g2d.translate(-BOARD_SIZE / 1.9f, (i == 0) ? -BOARD_SIZE / 2.5f : BOARD_SIZE / 7.5f);

            // Draw the dices
            for (int j = 0; j < 6; j++) {
                g2d.translate(DICE_SIZE / 0.9f, 0);
                AffineTransform old2 = g2d.getTransform();
                // If dice is hold, translate it to the top
                g2d.translate(0, diceList.get(i)[j].isHold() ? DICE_SIZE / 2f : DICE_SIZE / 0.3f);
                diceList.get(i)[j].drawDice();
//                System.out.println(diceList.get(i)[j].getDiceId() + " " + i);
                g2d.setTransform(old2);
            }
            g2d.setTransform(old);
        }
        // Draw the dices hover, and selected
        for (int i = 0; i < 2; i++) {
            g2d.translate(-BOARD_SIZE / 1.9f, (i == 0) ? -BOARD_SIZE / 2.5f + DICE_SIZE / 0.3f : BOARD_SIZE / 7.5f + DICE_SIZE / 0.3f);
            for (int j = 0; j < 6; j++) {
                g2d.translate(DICE_SIZE / 0.9f, 0);
                if (diceList.get(i)[j].isHover() && diceList.get(i)[j].isSelected()) {
                    diceList.get(i)[j].markEllipse(new Color[] {DICE_SELECTED, DICE_HOVER});
                } else if (diceList.get(i)[j].isHover()) {
                    diceList.get(i)[j].markEllipse(new Color[] {DICE_HOVER});
                } else if (diceList.get(i)[j].isSelected()) {
                    diceList.get(i)[j].markEllipse(new Color[] {DICE_SELECTED});
                }
            }
            g2d.setTransform(old);
        }
    }

    public void drawStateText(String text) {
        g2d.setFont(GAME_TEXT_FONT);
        g2d.setPaint(textureText);
        g2d.drawString(text, PLAYER_STATS_SIZE / 2f, PLAYER_STATS_SIZE / 0.75f);
    }
}
