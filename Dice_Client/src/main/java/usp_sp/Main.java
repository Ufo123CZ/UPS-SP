package usp_sp;

import usp_sp.GUI.Window;
import usp_sp.Server.Connection;

import javax.swing.*;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        System.out.println("KCD Dice Client Launching...");
        SwingUtilities.invokeLater(() -> {
            Window window = new Window();
            window.setVisible(true);
        });
    }
}