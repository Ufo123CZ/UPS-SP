package usp_sp.Server;

import lombok.Getter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Connection {

    private String serverAddress = "";
    private int port = 0;
    @Getter
    private String playerName = "";

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

    public Object[] makeConnection(String information) {
        try (Socket socket = new Socket(serverAddress, port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // Send a message to the server
            out.println("Client -> Server;" +  information);

            // Read the response from the server
            String response = in.readLine();
//            System.out.println(response);
            return new Object[] {true, response};

        } catch (IOException e) {
            return new Object[] {false, ""};
        }
    }

    public void connectToServer() throws IOException {
        try (Socket socket = new Socket(serverAddress, port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // Send a message to the server
            out.println("Hello from client");

            // Read the response from the server
            String response = in.readLine();
            System.out.println("Message from server: " + response);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
