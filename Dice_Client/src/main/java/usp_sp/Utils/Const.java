package usp_sp.Utils;

import lombok.Getter;
import lombok.ToString;

public class Const {
    // Assets Paths
    public static final String ASSETS_GIF = "src/main/resources/Assets/Gifs/";
    public static final String ASSETS_MATERIALS = "src/main/resources/Assets/Materials/";

    // Window
    public static final int WINDOW_WIDTH = 800;
    public static final int WINDOW_HEIGHT = 600;

    // Game
    public static final float BOARD_SIZE = 500.0f;
    public static final float DICE_SIZE = 40.0f;
    public static final float DICE_ROLL_ARC = 20;

    // Lobby
    public static final String[] COLUMNS_NAMES = {"No.", "Jmeno", "Pocet Hráčů", "Join", "Delete"};
    public static final double[] COLUMNS_WIDTH = {0.05, 0.45, 0.1, 0.2, 0.2}; // percentage of the table width

}
