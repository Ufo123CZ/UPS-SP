package usp_sp.Server;

public class Messages {

    // Error
    public static final String ERROR = "ERROR";

    // Base
    static final String BASE_IN = "dc";
    static final String BASE_OUT = "ds";
    private static final String BASE_LOGIN = "log:";
    private static final String BASE_QUEUE = "queue:";
    private static final String BASE_GAME = "game:";

    // Ping
    public static final String PING = "ping:";
    public static final String PONG = "pong:";

    // Login
    public static final String LOGIN = BASE_LOGIN + "in:";
    public static final String LOGOUT = BASE_LOGIN + "out:";

    // Game
    public static final String GAME_CREATED = BASE_GAME + "create:";
    public static final String GAME_THROW_DICE = BASE_GAME + "throw:";





}
