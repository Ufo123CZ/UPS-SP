package usp_sp.Server;

public class Messeges {

    // Base
    private static final String base = "dc:";
    private static final String lobby = "lobby:";

    // Login
    public static final String LOGIN = base + "login:";
    public static final String LOGOUT = base + "logout:";
    // Lobby
    public static final String CREATE_LOBBY = base + lobby + "create:";
    public static final String DELETE_LOBBY = base + lobby + "delete:";
    public static final String REQUEST_LOBBY = base + lobby + "request:";
    public static final String JOIN_LOBBY = base + lobby + "join:";
    public static final String LEAVE_LOBBY = base + lobby + "leave:";




}
