package org.criticalking.criticalDiscord;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.*;

public class SQLInstance {

    private Connection conn;
    private String url;
    private JavaPlugin plugin;
    private Table linkTable;
    private Table linkedTable;

    public SQLInstance(String url, JavaPlugin plugin) {
        this.url = url;
        this.plugin = plugin;
    }

    public Connection init() {
        try {
            conn = DriverManager.getConnection(url);
            return conn;
        } catch (SQLException e) {
            plugin.getLogger().severe("Unable to connect to MySQL server.");
            return null;
        }
    }

    public void addTable(Table table, String type) {
        try {
            PreparedStatement stmt = conn.prepareStatement(table.getInitCommand());
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Unable to add required table " + table.getTableName());
        }

        if (type.equals("LINK")) {
            linkTable = table;
        }
        if (type.equals("LINKED")) {
            linkedTable = table;
        }
    }

    /**
     * Inserts a code for the given player into the link table.
     * If an entry already exists and is less than 5 minutes old, returns false.
     * If more than 5 minutes old, deletes the old row and inserts the new one.
     */
    public boolean putCode(String code, Player player) {
        // Ensure the linkTable is available
        if (linkTable == null) {
            plugin.getLogger().severe("Link table is not registered.");
            return false;
        }

        // If the player is already registered in the linked table, do not issue a code.
        if (playerIsRegistered(player)) return false;

        String playerUUID = player.getUniqueId().toString();
        String selectSql = "SELECT creation_unix FROM " + linkTable.getTableName() + " WHERE player_uuid = ?";
        try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
            selectStmt.setString(1, playerUUID);
            ResultSet rs = selectStmt.executeQuery();
            if (rs.next()) {
                // Code already exists for this player
                String creationTimeStr = rs.getString("creation_unix");
                long creationTime = Long.parseLong(creationTimeStr);
                long currentTime = System.currentTimeMillis();
                // If less than 5 minutes (300,000 ms) have passed, do not insert a new code.
                if (currentTime - creationTime < 300000) {
                    return false;
                } else {
                    // Delete the old row since it's more than 5 minutes old.
                    String deleteSql = "DELETE FROM " + linkTable.getTableName() + " WHERE player_uuid = ?";
                    try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                        deleteStmt.setString(1, playerUUID);
                        deleteStmt.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error checking existing code for player " + playerUUID + ": " + e.getMessage());
            return false;
        }

        // Insert the new code
        String insertSql = "INSERT INTO " + linkTable.getTableName() + " (player_uuid, creation_unix, code) VALUES (?, ?, ?)";
        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
            insertStmt.setString(1, playerUUID);
            // Save the current time in milliseconds.
            insertStmt.setString(2, String.valueOf(System.currentTimeMillis()));
            insertStmt.setString(3, code);
            insertStmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to insert code into link table for player " + playerUUID + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Checks if a player is already registered in the linked table.
     */
    public boolean playerIsRegistered(Player player) {
        if (linkedTable == null) {
            plugin.getLogger().severe("Linked table is not registered.");
            return false;
        }
        String query = "SELECT * FROM " + linkedTable.getTableName() + " WHERE player_uuid = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, player.getUniqueId().toString());
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            plugin.getLogger().severe("Error checking if player is registered: " + e.getMessage());
            return false;
        }
    }

    /**
     * Force-unlinks a player by deleting their row from the linked table.
     * Returns true if a row was deleted.
     */
    public boolean forceUnlink(OfflinePlayer player) {
        if (linkedTable == null) {
            plugin.getLogger().severe("Linked table is not registered.");
            return false;
        }
        String sql = "DELETE FROM " + linkedTable.getTableName() + " WHERE player_uuid = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, player.getUniqueId().toString());
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to force unlink player " + player.getUniqueId() + ": " + e.getMessage());
            return false;
        }
    }

    public Connection getConnection() {
        try {
            if (conn.isClosed() || conn == null) {
                return init();
            } else {
                return conn;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void close() {
        try {
            conn.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Unable to close MySQL, was it already closed?");
        }
    }
}
