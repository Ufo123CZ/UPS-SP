package usp_sp.GameObjects;

import lombok.Getter;
import lombok.Setter;
import usp_sp.Utils.Materials;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

import static usp_sp.Utils.Colours.PLAYER_STATS_DIVIDER;
import static usp_sp.Utils.Const.*;

public class PlayerStats extends Materials {

    @Setter // Local
    Graphics2D g2d;

    @Getter @Setter // Server
    public String name = "";
    @Getter @Setter // Server
    public int totalScore = 0;
    @Getter @Setter // Server
    public int subtotalScore = 0;
    @Getter @Setter // Server
    public int throwScore = 0;

    TexturePaint textureStatsBG, textureEdge;

    public PlayerStats() {
        textureStatsBG = loadTexture("paper.jpg");
        textureEdge = loadTexture("dark_iron.jpg");
    }

    public void drawStats() {
        // Components
        Rectangle2D.Float statsBG = new Rectangle2D.Float(
                0, 0,
                PLAYER_STATS_SIZE / 0.6f, PLAYER_STATS_SIZE / 1.1f
        );

        Line2D.Float divider = new Line2D.Float(0, 0, PLAYER_STATS_SIZE / 0.64f, 0);

        // Draw the stats
        AffineTransform old = g2d.getTransform();
        g2d.setPaint(textureStatsBG);
        g2d.fill(statsBG);
        g2d.setPaint(textureEdge);
        g2d.setStroke(new BasicStroke(3));
        g2d.draw(statsBG);
        g2d.setStroke(new BasicStroke(1));

        // Name
        g2d.translate(PLAYER_STATS_SIZE / 10f, PLAYER_STATS_SIZE / 5f);
        g2d.setFont(GAME_TEXT_FONT_SMALL);
        g2d.drawString(name, 0, 0);
        drawDivider(divider);
        g2d.translate(0, PLAYER_STATS_SIZE / 5f);
        g2d.drawString("Celkové skóre: " + totalScore + "/4000", 0, 0);
        drawDivider(divider);
        g2d.translate(0, PLAYER_STATS_SIZE / 5f);
        g2d.drawString("Mezisoučet: " + subtotalScore, 0, 0);
        drawDivider(divider);
        g2d.translate(0, PLAYER_STATS_SIZE / 5f);
        g2d.drawString("Hozené skóre: " + throwScore, 0, 0);

        g2d.setTransform(old);
    }

    private void drawDivider(Line2D.Float divider) {
        AffineTransform old2 = g2d.getTransform();
        g2d.translate(-PLAYER_STATS_SIZE / 20f, PLAYER_STATS_SIZE / 10f);
        g2d.setPaint(PLAYER_STATS_DIVIDER);
        g2d.draw(divider);
        g2d.setTransform(old2);
    }

}
