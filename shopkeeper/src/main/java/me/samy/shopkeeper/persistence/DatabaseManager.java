package me.samy.shopkeeper.persistence;

import me.samy.shopkeeper.ShopKeeperPlugin;

import java.sql.*;

public class DatabaseManager {
    private final ShopKeeperPlugin plugin;
    private Connection connection;

    public DatabaseManager(ShopKeeperPlugin plugin) {
        this.plugin = plugin;
    }

    public void init() {
        try {
            Class.forName("org.sqlite.JDBC");
            java.io.File dbFile = new java.io.File(plugin.getDataFolder(), "data/shopkeeper.db");
            dbFile.getParentFile().mkdirs();
            String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();
            connection = DriverManager.getConnection(url);
            try (Statement st = connection.createStatement()) {
                st.executeUpdate("CREATE TABLE IF NOT EXISTS transactions (id INTEGER PRIMARY KEY AUTOINCREMENT, player TEXT, shop TEXT, item TEXT, amount INTEGER, price REAL, time INTEGER)");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void logPurchase(String player, String shop, String item, int amount, double price) {
        if (connection == null) return;
        try (PreparedStatement ps = connection.prepareStatement("INSERT INTO transactions (player,shop,item,amount,price,time) VALUES (?, ?, ?, ?, ?, ?)")) {
            ps.setString(1, player);
            ps.setString(2, shop);
            ps.setString(3, item);
            ps.setInt(4, amount);
            ps.setDouble(5, price);
            ps.setLong(6, System.currentTimeMillis());
            ps.execute();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void shutdown() {
        if (connection == null) return;
        try { connection.close(); } catch (SQLException ignored) {}
    }
}