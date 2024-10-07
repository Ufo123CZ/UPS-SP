package usp_sp.GUI;

import usp_sp.Server.Connection;
import usp_sp.Server.Messeges;

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
    private static DefaultTableModel model;

    public LobbyPanel() {
        setLayout(new BorderLayout());

        List<String[]> lobbies = new ArrayList<>();

        createTable(lobbies, new Object[0][5]);
        updateTableData(lobbies);
    }

    //region Buttons
    private JPanel getButtonPanel(DefaultTableModel model) {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

        // Add a new lobby
        JButton addLobbyButton = getjButton();
        buttonPanel.add(addLobbyButton);

        // Logout button
        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> {
            Connection connection = Connection.getInstance();
            connection.makeConnection(Messeges.LOGOUT + connection.getPlayerName());

            Window window = (Window) SwingUtilities.getWindowAncestor(LobbyPanel.this);
            window.showScene("Login");
        });
        buttonPanel.add(logoutButton);

        // Refresh button
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> {
            List<String[]> lobbies = getDataForTable(Messeges.FETCH_LOBBY);
            updateTableData(lobbies);
        });
        buttonPanel.add(refreshButton);

        return buttonPanel;
    }

    private JButton getjButton() {
        JButton addLobbyButton = new JButton("Add Lobby");
        addLobbyButton.addActionListener(e -> {
            String lobbyName = JOptionPane.showInputDialog("Enter lobby name:");
            if (lobbyName != null) {
                List<String[]> lobbies = getDataForTable(Messeges.CREATE_LOBBY + Connection.getInstance().getPlayerName() + ":" + lobbyName);
                updateTableData(lobbies);
            }
        });
        return addLobbyButton;
    }
    //endregion

    //region Table Creation/Update
    protected static void updateTableData(List<String[]> lobbies) {
        // Clear existing data
        model.setRowCount(0);

        for (int i = 0; i < lobbies.size(); i++) {
            String[] lobby = lobbies.get(i);
            if (lobby[2].equals(Connection.getInstance().getPlayerName())) {
                model.addRow(new Object[]{i + 1, lobby[0], lobby[1] + "/2", "Join", "Delete"});
            } else {
                model.addRow(new Object[]{i + 1, lobby[0], lobby[1] + "/2", "Join", ""});
            }
        }
    }

    protected void createTable(List<String[]> lobbies, Object[][] data) {
        model = new DefaultTableModel(data, COLUMNS_NAMES) {
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
    //endregion

    //region Get data for table
    protected static List<String[]> getDataForTable(String request) {

        Object[] received = Connection.getInstance().makeConnection(request);

        List<String[]> lobbies = new ArrayList<>();

        if ((boolean) received[0]) {
            String information = (String) received[1];
            String[] lobbiesData = information.split(";");

            for (String lobbyData : lobbiesData) {
                String[] lobbyDetails = lobbyData.split(":");
                lobbies.add(lobbyDetails);
            }
        } else {
            System.out.println("Failed to retrieve lobbies.");
        }
        return lobbies;
    }
    //endregion

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