package ups_sp;

import ups_sp.GUI.Window;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        System.out.println("KCD Dice Client Launching...");
        SwingUtilities.invokeLater(() -> {
            Window window = new Window();
            window.setVisible(true);
        });
    }
}