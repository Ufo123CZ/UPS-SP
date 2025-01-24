package ups_sp.Server;

import lombok.Getter;
import lombok.Setter;

import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static java.lang.Thread.sleep;
import static ups_sp.Server.Messages.*;
import static ups_sp.Utils.Const.*;

/**
 * Connection class is a singleton class that handles the connection to the server.
 * It is responsible for sending and receiving messages from the server.
 * It also has a listener thread that listens for incoming messages.
 */
public class Connection extends Component {

    // Server details
    private String serverAddress = "";
    private int port = 0;
    @Getter
    private String playerName = "";
    @Getter @Setter
    private int status = -1;

    // Socket
    @Getter
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    // Reader Thread
    private Thread listenerThread;
    private AtomicBoolean listening;

    // Message
    @Getter
    private String message = "";
    private String response = "";

    private String pongMessage;

    // Reconnect
    AtomicLong lastMess;
    AtomicBoolean statusOK;

    @Getter @Setter
    boolean reconnecting = false;

    // Event listener
    @Setter
    private EventListenerLogin eventListenerLogin;
    @Setter
    private EventListenerQueue eventListenerQueue;
    @Setter
    private EventListenerGame eventListenerGame;

    // Singleton
    private static Connection instance = null;
    private Connection() {
    }
    public static Connection getInstance() {
        if (instance == null) {
            instance = new Connection();
        }
        return instance;
    }

    /**
     * Set the server details.
     * @param serverAddress - IP address of the server
     * @param port - Port number of the server
     * @param playerName - Player name
     * @param status - Status of the connection (-1 = not connected, 0 = in queue, 1 = in game)
     */
    public void setServerDetails(String serverAddress, int port, String playerName, int status) {
        this.serverAddress = serverAddress;
        this.port = port;
        this.playerName = playerName;
        this.status = status;
    }

    /**
     * Open a socket connection to the server.
     * @return - True if the connection was successful, false otherwise
     */
    public boolean openSocket() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Boolean> future = executor.submit(() -> {
            try {
                socket = new Socket(serverAddress, port);
                socket.setKeepAlive(true);
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                pongMessage = messageBuilder(PONG, "");
                response = "";
                startListening();
                sendMessages();
                return true;
            } catch (IOException e) {
                System.out.println("Error: " + e.getMessage());
                status = -1;
                return false;
            }
        });

        try {
            return future.get(DISCONNECTED_CONNECTION, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            System.out.println("Connection attempt timed out.");
            future.cancel(true);
            return false;
        } catch (InterruptedException | ExecutionException e) {
            System.out.println("Error: " + e.getMessage());
            return false;
        } finally {
            executor.shutdown();
        }
    }

    /**
     * Close the socket connection.
     */
    public void closeSocket() {
        try {
            stopListening();
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null) socket.close();
            this.playerName = "";
            this.status = -1;
        } catch (IOException | InterruptedException ignored) {}
    }

    /**
     * Send a message to the server.
     * @param action - Action to be performed
     * @param information - Information to be sent
     */
    public void makeContact(String action, String information) {
        // Prepare and send the message
        response = messageBuilder(action, information);
        System.out.println("Sent: " + response);
    }

    /**
     * Build the message what will be sent to the server.
     * @param action - Action to be performed
     * @param information - Information to be sent
     * @return - The message
     */
    private String messageBuilder(String action, String information) {
        // Prepare the message
        int messageLength;
        if (information.isEmpty()) {
            messageLength = action.length() + 3;
        } else {
            messageLength = action.length() + information.length() + 3;
        }
        StringBuilder messageLengthStr = new StringBuilder(String.valueOf(messageLength));
        for (int i = messageLengthStr.length(); i < 4; i++) {
            messageLengthStr.insert(0, "0");
        }

        String messageHeader = BASE_IN + ";" + messageLengthStr+ ";";

        if (information.isEmpty()) {
            return messageHeader + action + ";";
        }
        return messageHeader + action + ";" + information;
    }

    /**
     * Exclude the first two parts of the message (signature and length).
     * @param receivedMessage - The received message
     * @return - The message without the first two parts
     */
    private static StringBuilder exludePartsOfMessage(String receivedMessage) {
        String[] parts = receivedMessage.split(";");
        // Chain all parts except the first two
        StringBuilder messageBuilder = new StringBuilder();
        for (int i = 2; i < parts.length; i++) {
            messageBuilder.append(parts[i]);
            if (i < parts.length - 1) {
                messageBuilder.append(";");
            }
        }
        return messageBuilder;
    }

    /**
     * Start the listener thread.
     */
    private void startListening() {
        listening = new AtomicBoolean(true);
        lastMess = new AtomicLong(System.currentTimeMillis());
        statusOK = new AtomicBoolean(true);
        statusCheck();
        listenerThread = new Thread(() -> {
            while (listening.get()) {
                try {
                    sleep(100);
                    String receivedMessage;
                    /* READING RECONNECTING */
                    while (statusOK.get()) {
                        if (in.ready()) {
                            // Read the message
                            lastMess.set(System.currentTimeMillis());
                            receivedMessage = in.readLine();
                            if (receivedMessage != null && !receivedMessage.isEmpty()) {
                                // Check if the message is a valid message
                                if (receivedMessage.contains(BASE_OUT)) {
                                    message = exludePartsOfMessage(receivedMessage).toString();
                                    break;
                                } else {
                                    // Message is not valid - close the socket and send a logout message
                                    response = messageBuilder(LOGOUT, "");
                                    out.println(response);
                                    response = "";
                                    closeSocket();
                                    if (eventListenerLogin != null) {
                                        eventListenerLogin.onMessageReceivedLogin(SERVER_ERROR);
                                    }
                                    return;
                                }
                            }
                        }
                    }
                    /* BASE */
                    if (eventListenerLogin != null) {
                        eventListenerLogin.onMessageReceivedLogin(message);
                    }
                    if (eventListenerQueue != null) {
                        eventListenerQueue.onMessageReceivedQueue(message);
                    }
                    if (eventListenerGame != null) {
                        eventListenerGame.onMessageReceivedGame(message);
                    }

                    if (!response.isEmpty() && !response.equals("\n")) {
                        out.println(response);
                    }
                    response = "";

                } catch (IOException | InterruptedException | NullPointerException e) {
                    if (!listening.get()) {
                        System.out.println("Socket closed, stopping listener thread.");
                    } else {
                        System.out.println("Error: " + e.getMessage());
                        listening.set(false);
                        if (eventListenerLogin != null) {
                            eventListenerLogin.onMessageReceivedLogin(SERVER_ERROR);
                        }
                    }

                }
            }
        });
        listenerThread.start();
    }

    /**
     * Send messages to the server.
     */
    public void sendMessages() {
        new Thread(() -> {
            while(listening.get()) {
                if (!response.isEmpty()) {
                    out.println(response);
                    response = "";
                }
                try {
                    sleep(100);
                } catch (InterruptedException e) {
                    System.out.println("Error: " + e.getMessage());
                }
            }
        }).start();
    }

    /**
     * Check the status of the connection.
     */
    private void statusCheck() {
        new Thread(() -> {
            reconnecting = false;
            int counter = 0;
            long lastPing = System.currentTimeMillis();
            while (listening.get()) {
                try {
                    sleep(500);
                    long timeNow = System.currentTimeMillis();
                    if (timeNow - lastMess.get() > DISCONNECTED_CONNECTION) {   // DISCONNECTED
                        // Close socket
                        System.out.println("Connection timed out.");
                        statusOK.set(false);
                        closeSocket();
                        if (eventListenerLogin != null) {
                            eventListenerLogin.onMessageReceivedLogin(SERVER_ERROR);
                        }
                    } else if (timeNow - lastMess.get() > NO_CONNECTION && !reconnecting) { // NO CONNECTION - TRY TO RECONNECT
                        // Freeze game and try to reconnect
                        if (counter == 0) System.out.println("Connection lost.");
                        counter++;

                        if (eventListenerGame != null) {
                            eventListenerGame.onMessageReceivedGame(CONNECTION_LOST);
                            reconnecting = true;
                        }
                    } else if (timeNow - lastMess.get() <= NO_CONNECTION) { // RECONNECTED
                        counter = 0;
                    }
                    // PING
                    if ((timeNow - lastPing > PING_INTERVAL) && !reconnecting ) {
                        // Last ping message
                        lastPing = System.currentTimeMillis();
                        // Check if out is filled with pong
                        out.println(pongMessage);
                    }
                } catch (InterruptedException e) {
                    System.out.println("Error: " + e.getMessage());
                }
            }
        }).start();
    }

    /**
     * Stop the listener thread.
     * @throws InterruptedException - Exception
     */
    private void stopListening() throws InterruptedException {
        listening.set(false);
        sleep(2000);
        if (listenerThread != null) {
            listenerThread.interrupt();
        }
    }

    /**
     * Event listeners for Login
     */
    public interface EventListenerLogin {
        void onMessageReceivedLogin(String message);
    }

    /**
     * Event listeners for Queue
     */
    public interface EventListenerQueue {
        void onMessageReceivedQueue(String message);
    }

    /**
     * Event listeners for Game
     */
    public interface EventListenerGame {
        void onMessageReceivedGame(String message);
    }
}
