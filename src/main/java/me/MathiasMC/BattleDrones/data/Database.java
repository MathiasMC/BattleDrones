package me.MathiasMC.BattleDrones.data;

import me.MathiasMC.BattleDrones.BattleDrones;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.sql.*;
import java.util.regex.Pattern;

public class Database {

    private final BattleDrones plugin;
    private Connection connection;

    private final boolean mysql;
    private final String host, port, database, username, password;

    private static final Pattern VALID_TABLE_NAME = Pattern.compile("^[a-zA-Z0-9_]+$");

    public Database(final BattleDrones plugin) {
        this.plugin = plugin;

        this.mysql = plugin.getFileUtils().config.getBoolean("mysql.use");
        this.host = plugin.getFileUtils().config.getString("mysql.host");
        this.port = plugin.getFileUtils().config.getString("mysql.port");
        this.database = plugin.getFileUtils().config.getString("mysql.database");
        this.username = plugin.getFileUtils().config.getString("mysql.username");
        this.password = plugin.getFileUtils().config.getString("mysql.password");


        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    if (connection != null && !connection.isClosed()) {
                        try (Statement selectSql = connection.createStatement()) {
                            selectSql.execute("SELECT 1");
                        }
                    } else {
                        connection = connect();
                        if (connection != null) {
                            createPlayersTable();
                        }
                    }
                } catch (SQLException e) {
                    plugin.getTextUtils().exception(e.getStackTrace(), e.getMessage());
                }
            }
        }.runTaskTimerAsynchronously(plugin, 60 * 20, 60 * 20);
    }

    private Connection connect() {
        try {
            if (mysql) {
                plugin.getTextUtils().info("Database ( Connected ) ( MySQL )");
                return DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&autoReconnect=true&serverTimezone=UTC&characterEncoding=utf8", username, password);
            } else {
                plugin.getTextUtils().info("Database ( Connected ) ( SQLite )");
                return DriverManager.getConnection("jdbc:sqlite:" + new File(plugin.getDataFolder(), "data.db"));
            }
        } catch (SQLException e) {
            plugin.getTextUtils().exception(e.getStackTrace(), e.getMessage());
            return null;
        }
    }

    public void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                plugin.getTextUtils().exception(e.getStackTrace(), e.getMessage());
            }
        }
    }

    public boolean checkConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = connect();
                if (connection != null) {
                    createPlayersTable();
                }
            }
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            plugin.getTextUtils().exception(e.getStackTrace(), e.getMessage());
            return false;
        }
    }

    public void createPlayersTable() {
        if (!checkConnection()) return;
        String tableSql = "CREATE TABLE IF NOT EXISTS `players` (" +
                "`uuid` char(36) PRIMARY KEY, " +
                "`active` VARCHAR(255), " +
                "`coins` BIGINT, " +
                "`group` VARCHAR(255)" +
                ");";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(tableSql);
        } catch (SQLException e) {
            plugin.getTextUtils().exception(e.getStackTrace(), e.getMessage());
        }
    }

    public void createDroneTable(String drone) {
        if (!checkConnection() || !VALID_TABLE_NAME.matcher(drone).matches()) return;
        String tableSql = "CREATE TABLE IF NOT EXISTS `" + drone + "` (" +
                "`uuid` char(36) PRIMARY KEY, " +
                "`unlocked` TINYINT(1), " +
                "`level` SMALLINT, " +
                "`ammo` SMALLINT, " +
                "`monsters` TINYINT(1), " +
                "`animals` TINYINT(1), " +
                "`players` TINYINT(1), " +
                "`exclude` LONGTEXT, " +
                "`health` SMALLINT, " +
                "`left` SMALLINT" +
                ");";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(tableSql);
        } catch (SQLException e) {
            plugin.getTextUtils().exception(e.getStackTrace(), e.getMessage());
        }
    }

    public void insertPlayer(String uuid) {
        if (!checkConnection()) return;
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                String selectSql = "SELECT 1 FROM players WHERE uuid = ?";
                String insertSql = "INSERT INTO players (uuid, active, coins, `group`) VALUES(?, '', 0, 'default')";
                try (PreparedStatement selectStmt = connection.prepareStatement(selectSql)) {
                    selectStmt.setString(1, uuid);
                    try (ResultSet rs = selectStmt.executeQuery()) {
                        if (!rs.next()) {
                            try (PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {
                                insertStmt.setString(1, uuid);
                                insertStmt.executeUpdate();
                            }
                        }
                    }
                } catch (SQLException e) {
                    plugin.getTextUtils().exception(e.getStackTrace(), e.getMessage());
                }
            }
        };
        task.runTaskAsynchronously(plugin);
    }

    public void insertDrone(String uuid, String drone) {
        if (!checkConnection()) return;
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                String selectSql = "SELECT 1 FROM `" + drone + "` WHERE uuid = ?";
                String insertSql = "INSERT INTO `" + drone + "` (uuid, unlocked, level, ammo, monsters, animals, players, exclude, health, `left`) VALUES(?, 0, 1, 0, 1, 1, 1, '', 0, 0);";
                try (PreparedStatement selectStmt = connection.prepareStatement(selectSql)) {
                    selectStmt.setString(1, uuid);
                    try (ResultSet rs = selectStmt.executeQuery()) {
                        if (!rs.next()) {
                            try (PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {
                                insertStmt.setString(1, uuid);
                                insertStmt.executeUpdate();
                            }
                        }
                    }
                } catch (SQLException e) {
                    plugin.getTextUtils().exception(e.getStackTrace(), e.getMessage());
                }
            }
        };
        task.runTaskAsynchronously(plugin);
    }

    public void setPlayers(String uuid, String active, long coins, String group) {
        if (!checkConnection()) return;
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                String selectSql = "SELECT 1 FROM players WHERE uuid = ?";
                String updateSql = "UPDATE players SET active = ?, coins = ?, `group` = ? WHERE uuid = ?";
                try (PreparedStatement selectStmt = connection.prepareStatement(selectSql)) {
                    selectStmt.setString(1, uuid);
                    try (ResultSet rs = selectStmt.executeQuery()) {
                        if (rs.next()) {
                            try (PreparedStatement updateStmt = connection.prepareStatement(updateSql)) {
                                updateStmt.setString(1, active);
                                updateStmt.setLong(2, coins);
                                updateStmt.setString(3, group);
                                updateStmt.setString(4, uuid);
                                updateStmt.executeUpdate();
                            }
                        }
                    }
                } catch (SQLException e) {
                    plugin.getTextUtils().exception(e.getStackTrace(), e.getMessage());
                }
            }
        };
        task.runTaskAsynchronously(plugin);
    }

    public void setDrone(String uuid, String drone, int unlocked, int level, int ammo, int monsters, int animals, int players, String exclude, int health, int left) {
        if (!checkConnection()) return;
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                String selectSql = "SELECT 1 FROM `" + drone + "` WHERE uuid = ?";
                String updateSql = "UPDATE `" + drone + "` SET unlocked = ?, level = ?, ammo = ?, monsters = ?, animals = ?, players = ?, exclude = ?, health = ?, `left` = ? WHERE uuid = ?";
                try (PreparedStatement selectStmt = connection.prepareStatement(selectSql)) {
                    selectStmt.setString(1, uuid);
                    try (ResultSet rs = selectStmt.executeQuery()) {
                        if (rs.next()) {
                            try (PreparedStatement updateStmt = connection.prepareStatement(updateSql)) {
                                updateStmt.setInt(1, unlocked);
                                updateStmt.setInt(2, level);
                                updateStmt.setInt(3, ammo);
                                updateStmt.setInt(4, monsters);
                                updateStmt.setInt(5, animals);
                                updateStmt.setInt(6, players);
                                updateStmt.setString(7, exclude);
                                updateStmt.setInt(8, health);
                                updateStmt.setInt(9, left);
                                updateStmt.setString(10, uuid);
                                updateStmt.executeUpdate();
                            }
                        }
                    }
                } catch (SQLException e) {
                    plugin.getTextUtils().exception(e.getStackTrace(), e.getMessage());
                }
            }
        };
        task.runTaskAsynchronously(plugin);
    }

    public String[] getPlayers(String uuid) {
        try {
            if (connection == null || connection.isClosed()) return new String[]{"", "0", "default"};
        } catch (SQLException e) {
            plugin.getTextUtils().exception(e.getStackTrace(), e.getMessage());
        }
        String selectSql = "SELECT active, coins, `group` FROM players WHERE uuid = ?";
        try (PreparedStatement selectStmt = connection.prepareStatement(selectSql)) {
            selectStmt.setString(1, uuid);
            try (ResultSet rs = selectStmt.executeQuery()) {
                if (rs.next()) {
                    return new String[]{
                            rs.getString("active"),
                            String.valueOf(rs.getLong("coins")),
                            rs.getString("group")
                    };
                }
            }
        } catch (SQLException e) {
            plugin.getTextUtils().exception(e.getStackTrace(), e.getMessage());
        }
        return new String[]{"", "0", "default"};
    }

    public String[] getDrone(String uuid, String drone) {
        try {
            if (connection == null || connection.isClosed()) return new String[]{"0", "1", "0", "1", "1", "1", "", "0", "0"};
        } catch (SQLException e) {
            plugin.getTextUtils().exception(e.getStackTrace(), e.getMessage());
        }
        String selectSql = "SELECT unlocked, level, ammo, monsters, animals, players, exclude, health, `left` FROM `" + drone + "` WHERE uuid = ?";
        try (PreparedStatement selectStmt = connection.prepareStatement(selectSql)) {
            selectStmt.setString(1, uuid);
            try (ResultSet rs = selectStmt.executeQuery()) {
                if (rs.next()) {
                    return new String[] {
                            String.valueOf(rs.getInt("unlocked")),
                            String.valueOf(rs.getInt("level")),
                            String.valueOf(rs.getInt("ammo")),
                            String.valueOf(rs.getInt("monsters")),
                            String.valueOf(rs.getInt("animals")),
                            String.valueOf(rs.getInt("players")),
                            rs.getString("exclude"),
                            String.valueOf(rs.getInt("health")),
                            String.valueOf(rs.getInt("left"))
                    };
                }
            }
        } catch (SQLException e) {
            plugin.getTextUtils().exception(e.getStackTrace(), e.getMessage());
        }
        return new String[]{"0", "1", "0", "1", "1", "1", "", "0", "0"};
    }
}