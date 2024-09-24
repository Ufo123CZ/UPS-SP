package usp_sp.Utils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static usp_sp.Utils.Const.ASSETS_MATERIALS;

public abstract class Materials {
    public TexturePaint loadTexture(String fileName) {
        try {
            BufferedImage textureImage = ImageIO.read(new File(ASSETS_MATERIALS + fileName));
            Rectangle2D rect = new Rectangle2D.Float(0, 0, textureImage.getWidth(), textureImage.getHeight());
            return new TexturePaint(textureImage, rect);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
