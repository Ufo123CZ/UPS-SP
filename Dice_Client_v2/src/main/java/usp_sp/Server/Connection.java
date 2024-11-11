package usp_sp.Server;

import lombok.Getter;
import lombok.Setter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import static java.lang.Thread.sleep;
import static usp_sp.Server.Messages.ID;

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
    private String receivedMessage = "";


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

    public void openSocket() {
        try {
            socket = new Socket(serverAddress, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            startListening();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeSocket() {
        try {
            stopListening();
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Object[] makeContact(String action, String information) {
        // Prepare the message
        int messageLength = action.length() + information.length() + 3;
        StringBuilder messageLengthStr = new StringBuilder(String.valueOf(messageLength));
        for (int i = messageLengthStr.length(); i < 4; i++) {
            messageLengthStr.insert(0, "0");
        }
        String messageHeader = ID + ";" + messageLengthStr+ ";";
        String message = messageHeader + action + ";" + information;

        System.out.println(message);

        // Send a message to the server
        out.println(message);

        // Read the response from the server
//            String response = in.readLine();
//            System.out.println(response);

        // Wait for the response from the server send in thread
        while (receivedMessage.equals("")) {
            try {
                sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (!receivedMessage.contains("ping:")) {
            System.out.println("Received: " + receivedMessage);
        }

        return new Object[] {true, receivedMessage};
    }

    private void startListening() {
        listening = true;
        listenerThread = new Thread(() -> {
            while (listening) {
                try {
                    String message = in.readLine();
                    if (message != null && !message.contains("ping:")) {
                        receivedMessage = message;
                    }
                    // TODO: Handle ping messages - probably just respond with a pong
                } catch (IOException e) {
                    if (listening) {
                        e.printStackTrace();
                    } else {
                        System.out.println("Socket closed, stopping listener thread.");
                    }
                }
            }
        });
        listenerThread.start();
    }

    private void stopListening() {
        listening = false;
        if (listenerThread != null) {
            listenerThread.interrupt();
        }
    }
}
