package usp_sp.GameObjects;

import usp_sp.Utils.Materials;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;

import static usp_sp.Utils.Colours.DICE_EDGES;
import static usp_sp.Utils.Const.DICE_ROLL_ARC;
import static usp_sp.Utils.Const.DICE_SIZE;

public class Dice extends Materials {

    private final TexturePaint textureDice, textureDot;

    Graphics2D g2d;

    private String diceId;
    private int diceValue = 6;


    public Dice(Graphics2D g2d) {
        // Load the textures
        textureDice = loadTexture("birch.jpg");
        textureDot = loadTexture("spruce.jpg");

        // Graphics2D
        this.g2d = g2d;
    }

    public void drawDice() {
        // Components
        RoundRectangle2D.Float dice = new RoundRectangle2D.Float(
                -DICE_SIZE / 2, -DICE_SIZE / 2,
                DICE_SIZE, DICE_SIZE,
                DICE_ROLL_ARC, DICE_ROLL_ARC
        );

        // Dot
        Ellipse2D.Float dot = new Ellipse2D.Float(
                -DICE_SIZE / 10, -DICE_SIZE / 10,
                DICE_SIZE / 5, DICE_SIZE / 5
        );

        // Draw the dice
        g2d.setPaint(textureDice);
        g2d.fill(dice);
        g2d.setPaint(DICE_EDGES);
        g2d.draw(dice);

        // Draw the dots
        g2d.setPaint(textureDot);
        AffineTransform old = g2d.getTransform();
        switch (diceValue) {
            case 1:
                drawCenterDot(dot, old);
                break;
            case 2:
                drawCornerDot(dot, old, -1, -1);
                drawCornerDot(dot, old, 1, 1);
                break;
            case 3:
                drawCenterDot(dot, old);
                drawCornerDot(dot, old, -1, -1);
                drawCornerDot(dot, old, 1, 1);
                break;
            case 4:
                drawCornerDot(dot, old, -1, -1);
                drawCornerDot(dot, old, 1, 1);
                drawCornerDot(dot, old, -1, 1);
                drawCornerDot(dot, old, 1, -1);
                break;
            case 5:
                drawCenterDot(dot, old);
                drawCornerDot(dot, old, -1, -1);
                drawCornerDot(dot, old, 1, 1);
                drawCornerDot(dot, old, -1, 1);
                drawCornerDot(dot, old, 1, -1);
                break;
            case 6:
                drawCornerDot(dot, old, -1, -1);
                drawCornerDot(dot, old, 1, 1);
                drawCornerDot(dot, old, -1, 1);
                drawCornerDot(dot, old, 1, -1);
                drawSideDot(dot, old, -1, 1);
                drawSideDot(dot, old, 1, 1);
                break;
        }
    }

    //region Values draw
    private void drawCenterDot(Ellipse2D.Float dot, AffineTransform old) {
        g2d.fill(dot);
    }
    private void drawCornerDot(Ellipse2D.Float dot, AffineTransform old, int xs, int ys) {
        g2d.scale(xs, ys);
        g2d.translate(-DICE_SIZE / 4, -DICE_SIZE / 4);
        g2d.fill(dot);
        g2d.setTransform(old);
    }
    private void drawSideDot(Ellipse2D.Float dot, AffineTransform old, int xs, int ys) {
        g2d.scale(xs, ys);
        g2d.translate(-DICE_SIZE / 4, 0);
        g2d.fill(dot);
        g2d.setTransform(old);
    }
    //endregion
}
