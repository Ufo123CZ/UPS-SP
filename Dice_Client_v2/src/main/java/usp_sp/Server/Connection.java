package usp_sp.Server;

import lombok.Getter;
import lombok.Setter;
import usp_sp.GUI.Window;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import static java.lang.Thread.sleep;
import static usp_sp.Server.Messages.*;

public class Connection {

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

    // Logged in
    private boolean loggedIn = false;

    // Event listener
    @Setter
    private EventListenerQueue eventListener;
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
        try {
            socket = new Socket(serverAddress, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            pongMessage = messageBuilder(PONG, "");
            startListening();
            return true;
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
            this.status = -1;
            this.loggedIn = false;
            return false;
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
            this.loggedIn = false;
        } catch (IOException | InterruptedException ignored) {}
    }

    public String makeContact(String action, String information) {
        // Prepare the message
        response = messageBuilder(action, information);
        System.out.println("Sent: " + response);

        // Wait for the response from the server send in thread
//        while (message.isEmpty() || message.contains(PING) || message.contains(PONG)) {
//            try {
//                sleep(200);
//            } catch (InterruptedException e) {
//                return ERROR;
//            }
//        }
//        System.out.println("Received: " + message);
        return message;
    }

    public void lastContact(String action, String information) {
        // Prepare the message
        response = messageBuilder(action, information);
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
        /*
//        listenerThread = new Thread(() -> {
//            while (listening) {
//                try {
//                    // First message is login message
//                    try {
//                        if (!loggedIn) {
//                            sleep(200);
//                            if (!response.isEmpty()) {
//                                out.println(response);
//                                System.out.println("Sent: " + response);
//                                loggedIn = true;
//                            }
//                            response = "";
//                            continue;
//                        }
//                    } catch (InterruptedException ignored) {}
//                    String receivedMessage;
//                    try {
//                        int connection = 5;
//                        do {
//                            sleep(200);
//                            // Read the message from the server
//                            receivedMessage = in.readLine();
//                            if (!receivedMessage.isEmpty()) {
//                                StringBuilder messageBuilder = exludePartsOfMessage(receivedMessage);
//                                message = messageBuilder.toString();
//                                break;
//                            }
//                            connection--;
//                            if (connection == 0) {
//                                // TODO: Handle connection loss
//                                System.out.println("Disconnected from server.");
//                                closeSocket();
//                                return;
//                            }
//                        } while (message.isEmpty());
//
//                        // Notify the event listener
//                        if (eventListener != null) {
//                            eventListener.onMessageReceivedQueue(message);
//                        }
//                        if (eventListenerGame != null) {
//                            eventListenerGame.onMessageReceivedGame(message);
//                        }
//                    } catch (InterruptedException | NullPointerException e) {
//                        System.out.println("Error: Server connection lost.");
//                        // TODO: Handle connection loss
//                        status = -1;
//                        loggedIn = false;
//                        closeSocket();
//                    }
//                    // Read the message from the server
////                    System.out.println("Received: " + message);
//
//                    // Send the message to the server
//                    if (response.isEmpty() || response.equals("\n")) {
//                        response = pongMessage;
//                    }
////                    System.out.println("Sent: " + response);
//                    out.println(response);
//                    response = "";
//                    // TODO: Handle ping messages - probably just respond with a pong
//                } catch (IOException e) {
//                    if (!listening) {
//                        System.out.println("Socket closed, stopping listener thread.");
//                    }
//                }
//            }
//        });
//        listenerThread.start();
*/
        listenerThread = new Thread(() -> {
            while (listening) {
                try {
                    if (!loggedIn) {
                        sleep(10);
                        if (!response.isEmpty()) {
                            out.println(response);
                            System.out.println("Sent: " + response);
                            loggedIn = true;
                        }
                        response = "";
                        continue;
                    }

                    String receivedMessage;
                    int connectionAttempts = 5;
                    while (connectionAttempts > 0) {
                        sleep(10);
                        receivedMessage = in.readLine();
                        if (!receivedMessage.isEmpty()) {
                            message = exludePartsOfMessage(receivedMessage).toString();
                            break;
                        }
                        connectionAttempts--;
                    }

                    if (connectionAttempts == 0) {
                        System.out.println("Disconnected from server.");
                        // TODO: Handle connection loss
                        closeSocket();
                        return;
                    }

                    if (eventListener != null) {
                        eventListener.onMessageReceivedQueue(message);
                    }
                    if (eventListenerGame != null) {
                        eventListenerGame.onMessageReceivedGame(message);
                    }

                    if (response.isEmpty() || response.equals("\n")) {
                        response = pongMessage;
                    }
                    out.println(response);
                    response = "";

                } catch (InterruptedException | IOException e) {
                    if (!listening) {
                        System.out.println("Socket closed, stopping listener thread.");
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
        sleep(2000);
        if (listenerThread != null) {
            listenerThread.interrupt();
        }
    }

    public interface EventListenerQueue {
        void onMessageReceivedQueue(String message);
    }

    public interface EventListenerGame {
        void onMessageReceivedGame(String message);
    }
}
