package usp_sp.Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Connection {

    public Connection() throws IOException {
        connectToServer();
    }

    public void connectToServer() throws IOException {
        // create a socket to connect to the server running on localhost at port number 8080
        Socket socket = new Socket("localhost", 8080);

        // Setup output stream to send data to the server
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        // Setup input stream to receive data from the server
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        // Send message to the server
        out.println("Hello from client!");

        // Receive response from the server
        String response = in.readLine();
        System.out.println("Server says: " + response);

        // Close the socket
        socket.close();
    }
}
