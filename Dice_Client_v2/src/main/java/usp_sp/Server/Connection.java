package usp_sp.Server;

import lombok.Getter;
import lombok.Setter;

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
    private String message = "";
    private String response = "";

    private String pongMessage;

    // Logged in
    private boolean loggedIn = false;


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
            this.status = -1;
            this.loggedIn = false;
        } catch (IOException | InterruptedException ignored) {}
    }

    public String makeContact(String action, String information) {
        /*
//        int messageLength = action.length() + information.length() + 3;
//        StringBuilder messageLengthStr = new StringBuilder(String.valueOf(messageLength));
//        for (int i = messageLengthStr.length(); i < 4; i++) {
//            messageLengthStr.insert(0, "0");
//        }
//
//        String messageHeader = BASE_IN + ";" + messageLengthStr+ ";";
//        response = messageHeader + action + ";" + information;
//        System.out.println(response);

//        System.out.println(message);
//
//        // Send a message to the server
//        out.println(message);

        // Read the response from the server
//            String response = in.readLine();
//            System.out.println(response);
*/
        // Prepare the message
        response = messageBuilder(action, information);

        // Wait for the response from the server send in thread
        while (message.isEmpty()) {
            try {
                sleep(500);
            } catch (InterruptedException e) {
                return ERROR;
            }
        }

        return message;
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
                    // First message is login message
                    try {
                        if (!loggedIn) {
                            sleep(500);
                            if (!response.isEmpty()) {
                                out.println(response);
                                System.out.println("Sent: " + response);
                                loggedIn = true;
                            }
                            response = "";
                            continue;
                        }
                    } catch (InterruptedException ignored) {}
                    String receivedMessage;
                    try {
                        int connection = 5;
                        do {
                            sleep(500);
                            receivedMessage = in.readLine();
                            if (!receivedMessage.isEmpty()) {
                                message = receivedMessage;
                                connection = 5;
                            }
                            connection--;
                            if (connection == 0) {
                                status = -1;
                                loggedIn = false;
                                // TODO: Handle connection loss
                                closeSocket();
                                return;
                            }
                        } while (message.isEmpty());
                    } catch (InterruptedException | NullPointerException e) {
                        System.out.println("Error: Server connection lost.");
                        // TODO: Handle connection loss
                        status = -1;
                        loggedIn = false;
                        closeSocket();
                        return;
                    }
                    // Read the message from the server
                    System.out.println("Received: " + message);

                    // Send the message to the server
                    if (response.isEmpty() || response.equals("\n")) {
                        response = pongMessage;
                    }
                    System.out.println("Sent: " + response);
                    out.println(response);
                    response = "";
                    // TODO: Handle ping messages - probably just respond with a pong
                } catch (IOException e) {
                    if (!listening) {
                        System.out.println("Socket closed, stopping listener thread.");
                    }
                }
            }
        });
        listenerThread.start();
    }

    private void stopListening() throws InterruptedException {
        listening = false;
        sleep(2000);
        if (listenerThread != null) {
            listenerThread.interrupt();
        }
    }
}
