package ups_sp.Server;

import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static java.lang.Thread.sleep;
import static ups_sp.Server.Messages.*;
import static ups_sp.Utils.Const.*;

public class Connection extends Component {

    // Server details
    private String serverAddress = "";
    private int port = 0;
    @Getter
    private String playerName = "";
    @Getter @Setter
    private int status = -1;

    // Socket
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

    public void setServerDetails(String serverAddress, int port, String playerName, int status) {
        this.serverAddress = serverAddress;
        this.port = port;
        this.playerName = playerName;
        this.status = status;
    }

    public boolean openSocket() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Boolean> future = executor.submit(() -> {
            try {
                socket = new Socket(serverAddress, port);
                socket.setSoTimeout((int)(DISCONNECTED_CONNECTION));
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                pongMessage = messageBuilder(PONG, "");
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

    public void makeContact(String action, String information) {
        // Prepare and send the message
        response = messageBuilder(action, information);
        System.out.println("Sent: " + response);
    }

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

    private void startListening() {
        listening = new AtomicBoolean(true);
        lastMess = new AtomicLong(System.currentTimeMillis());
        statusOK = new AtomicBoolean(true);
        statusCheck();
        listenerThread = new Thread(() -> {
            while (listening.get()) {
                try {
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
//                        response = pongMessage;
                        out.println(response);
                    }
                    response = "";

                } catch (IOException | NullPointerException e) {
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

    private void statusCheck() {
        new Thread(() -> {
            boolean reconnecting = false;
            int counter = 0;
            while (listening.get()) {
                try {
                    sleep(500);
                    // DISCONNECTED
                    if (System.currentTimeMillis() - lastMess.get() > DISCONNECTED_CONNECTION) {
                        // Close socket
                        System.out.println("Connection timed out.");
                        statusOK.set(false);
                        closeSocket();
                        if (eventListenerLogin != null) {
                            eventListenerLogin.onMessageReceivedLogin(SERVER_ERROR);
                        }
                    }

                    // NO CONNECTION - RECONNECT
                    if (System.currentTimeMillis() - lastMess.get() > NO_CONNECTION) {
                        // Freeze game and try to reconnect
                        if (counter == 0) System.out.println("Connection lost. Trying to reconnect.");
                        counter++;

                        if (eventListenerGame != null && !reconnecting) {
                            eventListenerGame.onMessageReceivedGame(CONNECTION_LOST);
                            reconnecting = true;
                        }
                        out.println(pongMessage);
                    } else {
                        counter = 0;
                        if (eventListenerGame != null && reconnecting) {
                            eventListenerGame.onMessageReceivedGame(CONNECTION_RECONNECTED);
                            reconnecting = false;
                        }
                    }

                    // PING
                    if (System.currentTimeMillis() - lastMess.get() > PING_INTERVAL) {
                        out.println(pongMessage);
                    }
                } catch (InterruptedException e) {
                    System.out.println("Error: " + e.getMessage());
                }
            }
        }).start();
    }

    private void stopListening() throws InterruptedException {
        listening.set(false);
        sleep(2000);
        if (listenerThread != null) {
            listenerThread.interrupt();
        }
    }

    public interface EventListenerLogin {
        void onMessageReceivedLogin(String message);
    }

    public interface EventListenerQueue {
        void onMessageReceivedQueue(String message);
    }

    public interface EventListenerGame {
        void onMessageReceivedGame(String message);
    }
}
