package ups_sp.Server;

public class Messages {

    // Success and Error
    public static final String SUCCESS = "SUCCESS";
    public static final String ERROR = "ERROR";
    public static final String SERVER_ERROR = "SERVER ERROR";
    public static final String CONNECTION_LOST = "CONNECTION LOST";
    public static final String TERMINATE = "terminating:";

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
    public static final String NAMESET = BASE_LOGIN + "nameset:";

    // Game
    public static final String GAME_CREATED = BASE_GAME + "create:";
    public static final String GAME_RECONNECTED = BASE_GAME + "reconnect:";
    public static final String GAME_THROW_DICE = BASE_GAME + "throw:";
    public static final String GAME_SELECT_DICE = BASE_GAME + "select:";
    public static final String GAME_NEXT_TURN = BASE_GAME + "nextT:";
    public static final String GAME_END_TURN = BASE_GAME + "endT:";
    public static final String GAME_PLAYER_LEFT = BASE_GAME + "playerLeft:";
    public static final String GAME_PLAYER_JOINED = BASE_GAME + "playerJoined:";

    // Queue
    public static final String QUEUE_REJOIN = BASE_QUEUE + "rejoin:";

    // Game States
    public static final String GAME_STATE_WINNER = "winner:";





}
