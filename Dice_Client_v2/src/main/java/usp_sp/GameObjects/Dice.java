package usp_sp.GameObjects;

import lombok.Getter;
import lombok.Setter;
import usp_sp.Utils.Materials;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;

import static usp_sp.Utils.Colours.*;
import static usp_sp.Utils.Const.DICE_ROLL_ARC;
import static usp_sp.Utils.Const.DICE_SIZE;

public class Dice extends Materials {

    private final TexturePaint textureDice, textureDot;

    // Graphics2D Object
    RoundRectangle2D.Float dice;
    Ellipse2D.Float dot;
    Ellipse2D.Float diceMark;

    @Setter // Local
    Graphics2D g2d = null;
    @Getter @Setter // Server
    private String diceId = "";
    @Getter @Setter // Server
    private int diceValue = 6;
    @Getter @Setter // Server
    private boolean selected = false;
    @Getter @Setter // Local
    private boolean hover = false;
    @Getter @Setter // Server
    private boolean hold = false;

    public Dice() {
        // Load the textures
        textureDice = loadTexture("birch.jpg");
        textureDot = loadTexture("spruce.jpg");

        initGO();
    }

    private void initGO() {
        // Components
        dice = new RoundRectangle2D.Float(
                -DICE_SIZE / 2, -DICE_SIZE / 2,
                DICE_SIZE, DICE_SIZE,
                DICE_ROLL_ARC, DICE_ROLL_ARC
        );
        // Dot
        dot = new Ellipse2D.Float(
                -DICE_SIZE / 10, -DICE_SIZE / 10,
                DICE_SIZE / 5, DICE_SIZE / 5
        );
        diceMark = new Ellipse2D.Float(
                -DICE_SIZE / 1.5f, -DICE_SIZE / 1.5f,
                DICE_SIZE / 0.75f, DICE_SIZE / 0.75f
        );
    }

    public void drawDice() {
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
                drawCenterDot();
                break;
            case 2:
                drawCornerDot(old, -1, -1);
                drawCornerDot(old, 1, 1);
                break;
            case 3:
                drawCenterDot();
                drawCornerDot(old, -1, -1);
                drawCornerDot(old, 1, 1);
                break;
            case 4:
                drawCornerDot(old, -1, -1);
                drawCornerDot(old, 1, 1);
                drawCornerDot(old, -1, 1);
                drawCornerDot(old, 1, -1);
                break;
            case 5:
                drawCenterDot();
                drawCornerDot(old, -1, -1);
                drawCornerDot(old, 1, 1);
                drawCornerDot(old, -1, 1);
                drawCornerDot(old, 1, -1);
                break;
            case 6:
                drawCornerDot(old, -1, -1);
                drawCornerDot(old, 1, 1);
                drawCornerDot(old, -1, 1);
                drawCornerDot(old, 1, -1);
                drawSideDot(old, -1);
                drawSideDot(old, 1);
                break;
        }
    }

    public void markEllipse(Color[] color) {
        AffineTransform old = g2d.getTransform();
        g2d.setStroke(new BasicStroke(2));

        if (color.length == 2) {
            g2d.setPaint(color[1]);
            g2d.draw(diceMark);
            g2d.setStroke(new BasicStroke(3));
            g2d.scale(0.9f, 0.9f);
            g2d.setPaint(color[0]);
            g2d.draw(diceMark);
        } else if (color[0] == DICE_SELECTED) {
            g2d.setStroke(new BasicStroke(3));
            g2d.scale(0.9f, 0.9f);
            g2d.setPaint(color[0]);
            g2d.draw(diceMark);
        } else {
            g2d.setPaint(color[0]);
            g2d.draw(diceMark);
        }
        g2d.setStroke(new BasicStroke(1));
        g2d.setTransform(old);
    }

    //region Values draw
    private void drawCenterDot() {
        g2d.fill(dot);
    }
    private void drawCornerDot(AffineTransform old, int xs, int ys) {
        g2d.scale(xs, ys);
        g2d.translate(-DICE_SIZE / 4, -DICE_SIZE / 4);
        g2d.fill(dot);
        g2d.setTransform(old);
    }
    private void drawSideDot(AffineTransform old, int xs) {
        g2d.scale(xs, 1);
        g2d.translate(-DICE_SIZE / 4, 0);
        g2d.fill(dot);
        g2d.setTransform(old);
    }
    //endregion
}
