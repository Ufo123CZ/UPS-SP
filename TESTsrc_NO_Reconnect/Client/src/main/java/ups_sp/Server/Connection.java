package ups_sp.Server;

import lombok.Getter;
import lombok.Setter;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.*;

import static java.lang.Thread.sleep;
import static ups_sp.Server.Messages.*;
import static ups_sp.Utils.Const.NO_CONNECTION;
import static ups_sp.Utils.Const.DISCONNECTED_CONNECTION;

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
    private boolean listening;

    // Message
    @Getter
    private String message = "";
    private String response = "";

    private String pongMessage;

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
//                socket.setSoTimeout(SOCKET_TIMEOUT);
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                pongMessage = messageBuilder(PONG, "");
                startListening();
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

    private void startListening() {
        listening = true;
        listenerThread = new Thread(() -> {
            while (listening) {
                try {
                    String receivedMessage;
//                    boolean reconnecting = false;
//
//                    int connectionAttempts = NO_CONNECTION / 1000; // Time till when pause game
//                    int connectionAttempts2 = DISCONNECTED_CONNECTION / 1000; // Time till when close socket
//                    while (connectionAttempts2 > 0) {
//                        if (in.ready()) {
//                            receivedMessage = in.readLine();
//                            if (receivedMessage != null && !receivedMessage.isEmpty()) {
//                                message = exludePartsOfMessage(receivedMessage).toString();
//                                connectionAttempts = NO_CONNECTION / 1000;
//                                connectionAttempts2 = DISCONNECTED_CONNECTION / 1000;
//                                reconnecting = false;
//                                break;
//                            }
//                        } else {
//                            if (connectionAttempts == 0 && !reconnecting) {
//                                reconnecting = true;
//                            }
//                            connectionAttempts--;
//                            connectionAttempts2--;
//                            sleep(1000); // Wait for 1 second before trying again
//                        }
//                    }
//                    if (connectionAttempts2 == 0) {
//                        System.out.println("Connection lost.");
//                        closeSocket();
//                        if (eventListenerLogin != null) {
//                            eventListenerLogin.onMessageReceivedLogin(SERVER_ERROR);
//                        }
//                        return;
//                    }
                    int connectionAttempts = 10;
                    while (connectionAttempts > 0) {
                        sleep(10);
                        receivedMessage = in.readLine();

                        if (!receivedMessage.isEmpty()) {
                            // Check if the message is a valid message
                            if (receivedMessage.contains(BASE_OUT)) {
                                message = exludePartsOfMessage(receivedMessage).toString();
								break;
                            } else {
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

                    if (connectionAttempts == 0) {
                        System.out.println("Disconnected from server.");
                        // TODO: Handle connection loss
                        closeSocket();
                        return;
                    }

                    if (eventListenerLogin != null) {
                        eventListenerLogin.onMessageReceivedLogin(message);
                    }
                    if (eventListenerQueue != null) {
                        eventListenerQueue.onMessageReceivedQueue(message);
                    }
                    if (eventListenerGame != null) {
                        eventListenerGame.onMessageReceivedGame(message);
                    }

                    if (response.isEmpty() || response.equals("\n")) {
                        response = pongMessage;
                    }
                    out.println(response);
                    response = "";

                } catch (InterruptedException | IOException | NullPointerException e) {
                    if (!listening) {
                        System.out.println("Socket closed, stopping listener thread.");
                    } else {
                        System.out.println("Error: " + e.getMessage());
                        listening = false;
                        if (eventListenerLogin != null) {
                            eventListenerLogin.onMessageReceivedLogin(SERVER_ERROR);
                        }
                    }

                }
            }
        });
        listenerThread.start();
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

    private void stopListening() throws InterruptedException {
        listening = false;
        sleep(1000);
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
