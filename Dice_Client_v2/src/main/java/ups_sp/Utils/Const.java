package ups_sp.Utils;

import java.awt.*;

public class Const {
    // Assets Paths
    public static final String ASSETS_GIF = "/Assets/Gifs/";
    public static final String ASSETS_MATERIALS = "/Assets/Materials/";
    public static final String ASSETS_ADDS = "/Assets/Adds/";

    // Window
    public static final int WINDOW_WIDTH = 800;
    public static final int WINDOW_HEIGHT = 600;

    // Game
    public static final float BOARD_SIZE = 500.0f;
    public static final float DICE_SIZE = 40.0f;
    public static final float DICE_ROLL_ARC = 20;
    public static final float PLAYER_STATS_SIZE = 100.0f;

    // Text Styles
    public static final Font GAME_TEXT_FONT  = new Font("Georgia", Font.BOLD, 25);
    public static final Font GAME_TEXT_FONT_SMALL  = new Font("Georgia", Font.BOLD, 10);

    // Text content
    public static final String GAME_TEXT_TH = "Hozené kostky";
    public static final String GAME_TEXT_SEL = "Kostky v mezisoučtu";


    // Timeouts
    public static final long DISCONNECTED_CONNECTION = 30000; // 10 seconds
    public static final long NO_CONNECTION = 3000; // 3 seconds


}
