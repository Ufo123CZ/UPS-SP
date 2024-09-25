package usp_sp.GUI;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import static usp_sp.Utils.Colours.*;
import static usp_sp.Utils.Const.COLUMNS_NAMES;
import static usp_sp.Utils.Const.COLUMNS_WIDTH;

public class LobbyPanel extends JPanel {

    private JTable table;

    public LobbyPanel() {
        setLayout(new BorderLayout());

        List<String> lobbies = new ArrayList<>();
        /* TODO: Get lobbies from server */

        Object[][] data = new Object[lobbies.size()][5];

        for (int i = 0; i < lobbies.size(); i++) {
            String[] lobby = lobbies.get(i).split(";");
            data[i] = new Object[]{i + 1, lobby[0], lobby[1] + "/2", "Join", "Delete"};

            /*
            TODO: lobby[2] is the UID of the lobby owner and if it is not null and matches the UID of the current user, the "Delete" button should be enabled.

            if (!lobby[2].equals(null) && lobby[2].equals("UID")) {
                data[i] = new Object[]{i + 1, lobby[0], lobby[1] + "/2", "Join", "Delete"};
            } else {
                data[i] = new Object[]{i + 1, lobby[0], lobby[1] + "/2", "Join", ""};
            }
             */
        }

        DefaultTableModel model = new DefaultTableModel(data, COLUMNS_NAMES) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        DefaultTableCellRenderer centerRenderer = new CenterRenderer();
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            table.getColumnModel().getColumn(i).setResizable(false);
        }
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int column = table.columnAtPoint(e.getPoint());

                if (column == 3) { // Join column
                    System.out.println("Join button clicked at row " + row);
                    // Show Queue screen
                    Window window = (Window) SwingUtilities.getWindowAncestor(LobbyPanel.this);
                    window.showScene("Queue");

                    /* TODO: Add action for server */
                } else if (column == 4) { // Delete column /* TODO: Check if the user is the owner of the lobby */
                    System.out.println("Delete button clicked at row " + row);
                    /* TODO: Add action for server */
                }
            }
        });

        JTableHeader header = table.getTableHeader();
        header.setDefaultRenderer(new CustomHeaderRenderer());
        header.setReorderingAllowed(false);

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                adjustColumnWidths();
            }
        });

        // Panel for buttons
        JPanel buttonPanel = getButtonPanel(model);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel getButtonPanel(DefaultTableModel model) {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

        // Add a new lobby
        JButton addLobbyButton = new JButton("Add Lobby");
        addLobbyButton.addActionListener(e -> {
            String lobbyName = JOptionPane.showInputDialog("Enter lobby name:");
            if (lobbyName != null) {
                // TEMP: Add new lobby to the table
                /* TODO: Add new lobby to the server */
                model.addRow(new Object[]{model.getRowCount() + 1, lobbyName, 0 + "/2", "Join", "Delete"});
            }
        });
        buttonPanel.add(addLobbyButton);

        // Logout button
        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> {
            Window window = (Window) SwingUtilities.getWindowAncestor(LobbyPanel.this);
            window.showScene("Login");
        });
        buttonPanel.add(logoutButton);
        return buttonPanel;
    }

    //region Table style
    // Adjust column widths
    private void adjustColumnWidths() {
        int tableWidth = getWidth();

        for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth((int) (tableWidth * COLUMNS_WIDTH[i]));
        }
    }

    // Header renderer
    private static class CustomHeaderRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            c.setBackground(TABLE_HEADER);
            c.setForeground(TABLE_TEXT);
            setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, TABLE_EDGE));
            setHorizontalAlignment(JLabel.CENTER);
            return c;
        }
    }

    // Cells renderer
    private static class CenterRenderer extends DefaultTableCellRenderer {
        public CenterRenderer() {
            setHorizontalAlignment(JLabel.CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, TABLE_EDGE));
            return c;
        }
    }
    //endregion
}