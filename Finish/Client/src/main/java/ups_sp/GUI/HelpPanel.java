package ups_sp.GUI;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

import static ups_sp.Utils.Const.ASSETS_ADDS;

/**
 * HelpPanel class
 * <p>
 * This class is a JPanel that displays the help information for the gamePanel.
 * It contains the rules and combinations of the gamePanel.
 */
public class HelpPanel extends JPanel {

    /**
     * Constructor
     */
    public HelpPanel() {
        this.setBackground(Color.WHITE);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;

        // Return to gamePanel button
        JButton returnButton = new JButton("Return to Game");
        returnButton.addActionListener(e -> {
            Window window = (Window) SwingUtilities.getWindowAncestor(this);
            window.showScene("Game");
        });

        ImageIcon origIcon, scaledIcon;
        Image origImage, scaledImage;


        // Help Combinations image
        origIcon = new ImageIcon(Objects.requireNonNull(getClass().getResource(ASSETS_ADDS + "Combinations.png")));
        origImage = origIcon.getImage();
        // Scale the image
        int width = origImage.getWidth(null);
        int height = origImage.getHeight(null);
        int scale = 2;
        int[] newSize = {width * scale, height * scale};
        scaledImage = origImage.getScaledInstance(newSize[0], newSize[1], Image.SCALE_FAST);
        scaledIcon = new ImageIcon(scaledImage);
        // Add the image to a JLabel
        JLabel combLabel = new JLabel(scaledIcon);

        // Help Rules
        origIcon = new ImageIcon(Objects.requireNonNull(getClass().getResource(ASSETS_ADDS + "Rules.png")));
        origImage = origIcon.getImage();
        // Scale the image
        float scaleR = (float) (origImage.getWidth(null) / origImage.getHeight(null));
        scaledImage = origImage.getScaledInstance((int)(origImage.getWidth(null) * (scaleR * 0.8f)), newSize[1], Image.SCALE_AREA_AVERAGING);
        scaledIcon = new ImageIcon(scaledImage);
        JLabel rulesLabel = new JLabel(scaledIcon);

        // Help Rules
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(rulesLabel, gbc);

        // Help Return to gamePanel button
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        add(returnButton, gbc);

        // Help Combinations
        gbc.gridx = 1;
        gbc.gridy = 0;
        add(combLabel, gbc);
    }
}
