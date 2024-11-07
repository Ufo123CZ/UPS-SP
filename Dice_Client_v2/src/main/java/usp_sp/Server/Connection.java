package usp_sp.Server;

import lombok.Getter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Connection {

    // Server details
    private String serverAddress = "";
    private int port = 0;
    @Getter
    private String playerName = "";
    private int state = 0;

    // Socket
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

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

    public void setServerDetails(String serverAddress, int port, String playerName) {
        this.serverAddress = serverAddress;
        this.port = port;
        this.playerName = playerName;
    }

    public void openSocket() {
        try {
            socket = new Socket(serverAddress, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeSocket() {
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Object[] testConnection(String action, String information) {
        try {
            // Prepare the message
            // Messeage format: "<lenght>;<Action>;[<Information>]"
            int length = action.length() + information.length() + 2;
            length += String.valueOf(length).length() + 1;
            String message = length + ";" + action + ";" + information;

            // Send a message to the server
            out.println(message);

            // Read the response from the server
            String response = in.readLine();
            System.out.println("Message from server: " + response);

            return new Object[] {true, response};
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new Object[] {false, ""};
    }

//    public Object[] makeConnection() {
////        try (Socket socket = new Socket(serverAddress, port);
////             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
////             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
////
////            // Send a message to the server
////            out.println("Client -> Server;" +  information);
////
////            // Read the response from the server
////            String response = in.readLine();
//////            System.out.println(response);
////            return new Object[] {true, response};
////
////        } catch (IOException e) {
////            return new Object[] {false, ""};
////        }
//        return new Object[] {true, "Hello from client"};
//    }

//    public void connectToServer() throws IOException {
//        try (Socket socket = new Socket(serverAddress, port);
//             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
//             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
//
//            // Send a message to the server
//            out.println("Hello from client");
//
//            // Read the response from the server
//            String response = in.readLine();
//            System.out.println("Message from server: " + response);
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
}
