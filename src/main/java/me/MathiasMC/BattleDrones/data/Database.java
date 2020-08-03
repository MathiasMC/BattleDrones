package me.MathiasMC.BattleDrones.data;

import me.MathiasMC.BattleDrones.BattleDrones;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.sql.*;

public class Database {

    private final BattleDrones plugin;

    private Connection connection;

    public Database(final BattleDrones plugin) {
        this.plugin = plugin;
        (new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    if (connection != null && !connection.isClosed()) {
                        connection.createStatement().execute("SELECT 1");
                    }
                } catch (SQLException e) {
                    connection = get();
                }
            }
        }).runTaskTimerAsynchronously(plugin, 60 * 20, 60 * 20);
    }

    private Connection get() {
        try {
            if (plugin.config.get.getBoolean("mysql.use")) {
                plugin.textUtils.info("Database ( Connected ) ( MySQL )");
                Class.forName("com.mysql.jdbc.Driver");
                return DriverManager.getConnection("jdbc:mysql://" + plugin.config.get.getString("mysql.host") + ":" + plugin.config.get.getString("mysql.port") + "/" + plugin.config.get.getString("mysql.database"), plugin.config.get.getString("mysql.username"), plugin.config.get.getString("mysql.password"));
            } else {
                plugin.textUtils.info("Database ( Connected ) ( SQLite )");
                Class.forName("org.sqlite.JDBC");
                return DriverManager.getConnection("jdbc:sqlite:" + new File(plugin.getDataFolder(), "data.db"));
            }
        } catch (ClassNotFoundException | SQLException e) {
            plugin.textUtils.exception(e.getStackTrace(), e.getMessage());
            return null;
        }
    }

    public void close() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }

    private boolean check() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = get();
            if (connection == null || connection.isClosed()) {
                return false;
            }
            connection.createStatement().execute("CREATE TABLE IF NOT EXISTS `players` (`uuid` char(36) PRIMARY KEY, `active` TINYTEXT(255), `coins` BIGINT(255), `group` TINYTEXT(255));");
            for (String drone : plugin.drones) {
                connection.createStatement().execute("CREATE TABLE IF NOT EXISTS `" + drone + "` (`uuid` char(36) PRIMARY KEY, `unlocked` TINYINT(1), `level` SMALLINT(255), `ammo` SMALLINT(255), `monsters` TINYINT(1), `animals` TINYINT(1), `players` TINYINT(1), `exclude` LONGTEXT(255), `health` SMALLINT(255), `left` SMALLINT(255));");
            }
        }
        return true;
    }

    public boolean set() {
        try {
            return check();
        } catch (SQLException e) {
            return false;
        }
    }

    public void insertPlayer(final String uuid) {
        if (set()) {
            BukkitRunnable r = new BukkitRunnable() {
                @Override
                public void run() {
                    PreparedStatement preparedStatement = null;
                    ResultSet resultSet = null;
                    try {
                        resultSet = connection.createStatement().executeQuery("SELECT * FROM players WHERE uuid= '" + uuid + "';");
                        if (!resultSet.next()) {
                            preparedStatement = connection.prepareStatement("INSERT INTO players (uuid, active, coins, `group`) VALUES(?, ?, ?, ?);");
                            preparedStatement.setString(1, uuid);
                            preparedStatement.setString(2, "");
                            preparedStatement.setLong(3, 0);
                            preparedStatement.setString(4, "default");
                            preparedStatement.executeUpdate();
                        }
                    } catch (SQLException exception) {
                        plugin.textUtils.exception(exception.getStackTrace(), exception.getMessage());
                    } finally {
                        if (resultSet != null)
                            try {
                                resultSet.close();
                            } catch (SQLException exception) {
                                plugin.textUtils.exception(exception.getStackTrace(), exception.getMessage());
                            }
                        if (preparedStatement != null)
                            try {
                                preparedStatement.close();
                            } catch (SQLException exception) {
                                plugin.textUtils.exception(exception.getStackTrace(), exception.getMessage());
                            }
                    }
                }
            };
            r.runTaskAsynchronously(plugin);
        }
    }
    public void insertDrone(final String uuid, final String drone) {
        if (set()) {
            BukkitRunnable r = new BukkitRunnable() {
                @Override
                public void run() {
                    PreparedStatement preparedStatement = null;
                    ResultSet resultSet = null;
                    try {
                        resultSet = connection.createStatement().executeQuery("SELECT * FROM " + drone + " WHERE uuid= '" + uuid + "';");
                        if (!resultSet.next()) {
                            preparedStatement = connection.prepareStatement("INSERT INTO " + drone + " (uuid, unlocked, level, ammo, monsters, animals, players, exclude, health, left) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");
                            preparedStatement.setString(1, uuid);
                            preparedStatement.setInt(2, 0);
                            preparedStatement.setInt(3, 1);
                            preparedStatement.setInt(4, 0);
                            preparedStatement.setInt(5, 1);
                            preparedStatement.setInt(6, 1);
                            preparedStatement.setInt(7, 1);
                            preparedStatement.setString(8, "");
                            preparedStatement.setInt(9, 0);
                            preparedStatement.setInt(10, 0);
                            preparedStatement.executeUpdate();
                        }
                    } catch (SQLException exception) {
                        plugin.textUtils.exception(exception.getStackTrace(), exception.getMessage());
                    } finally {
                        if (resultSet != null)
                            try {
                                resultSet.close();
                            } catch (SQLException exception) {
                                plugin.textUtils.exception(exception.getStackTrace(), exception.getMessage());
                            }
                        if (preparedStatement != null)
                            try {
                                preparedStatement.close();
                            } catch (SQLException exception) {
                                plugin.textUtils.exception(exception.getStackTrace(), exception.getMessage());
                            }
                    }
                }
            };
            r.runTaskAsynchronously(plugin);
        }
    }

    public void setPlayers(final String uuid, final String active, final long coins, final String group) {
        if (set()) {
            BukkitRunnable r = new BukkitRunnable() {
                public void run() {
                    PreparedStatement preparedStatement = null;
                    ResultSet resultSet = null;
                    try {
                        resultSet = connection.createStatement().executeQuery("SELECT * FROM players WHERE uuid= '" + uuid + "';");
                        if (resultSet.next()) {
                            preparedStatement = connection.prepareStatement("UPDATE players SET active = ?, coins = ?, `group` = ? WHERE uuid = ?");
                            preparedStatement.setString(1, active);
                            preparedStatement.setLong(2, coins);
                            preparedStatement.setString(3, group);
                            preparedStatement.setString(4, uuid);
                            preparedStatement.executeUpdate();
                        }
                    } catch (SQLException exception) {
                        plugin.textUtils.exception(exception.getStackTrace(), exception.getMessage());
                    } finally {
                        if (resultSet != null)
                            try {
                                resultSet.close();
                            } catch (SQLException exception) {
                                plugin.textUtils.exception(exception.getStackTrace(), exception.getMessage());
                            }
                        if (preparedStatement != null)
                            try {
                                preparedStatement.close();
                            } catch (SQLException exception) {
                                plugin.textUtils.exception(exception.getStackTrace(), exception.getMessage());
                            }
                    }
                }
            };
            r.runTaskAsynchronously(plugin);
        }
    }

    public void setDrone(final String uuid, final String drone, final int unlocked, final int level, final int ammo, final int monsters, final int animals, final int players, final String exclude, final int health, final int left) {
        if (set()) {
            BukkitRunnable r = new BukkitRunnable() {
                public void run() {
                    PreparedStatement preparedStatement = null;
                    ResultSet resultSet = null;
                    try {
                        resultSet = connection.createStatement().executeQuery("SELECT * FROM " + drone + " WHERE uuid= '" + uuid + "';");
                        if (resultSet.next()) {
                            preparedStatement = connection.prepareStatement("UPDATE " + drone + " SET unlocked = ?, level = ?, ammo = ?, monsters = ?, animals = ?, players = ?, exclude = ?, health = ?, left = ? WHERE uuid = ?");
                            preparedStatement.setInt(1, unlocked);
                            preparedStatement.setInt(2, level);
                            preparedStatement.setInt(3, ammo);
                            preparedStatement.setInt(4, monsters);
                            preparedStatement.setInt(5, animals);
                            preparedStatement.setInt(6, players);
                            preparedStatement.setString(7, exclude);
                            preparedStatement.setInt(8, health);
                            preparedStatement.setInt(9, left);
                            preparedStatement.setString(10, uuid);
                            preparedStatement.executeUpdate();
                        }
                    } catch (SQLException exception) {
                        plugin.textUtils.exception(exception.getStackTrace(), exception.getMessage());
                    } finally {
                        if (resultSet != null)
                            try {
                                resultSet.close();
                            } catch (SQLException exception) {
                                plugin.textUtils.exception(exception.getStackTrace(), exception.getMessage());
                            }
                        if (preparedStatement != null)
                            try {
                                preparedStatement.close();
                            } catch (SQLException exception) {
                                plugin.textUtils.exception(exception.getStackTrace(), exception.getMessage());
                            }
                    }
                }
            };
            r.runTaskAsynchronously(plugin);
        }
    }

    public String[] getPlayers(final String uuid) {
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            statement = connection.createStatement();
            resultSet = statement.executeQuery("SELECT * FROM players WHERE uuid= '" + uuid + "';");
            if (resultSet.next()) {
                return new String[]{ resultSet.getString("active"), String.valueOf(resultSet.getLong("coins")), resultSet.getString("group")};
            }
        } catch (SQLException exception) {
            plugin.textUtils.exception(exception.getStackTrace(), exception.getMessage());
        } finally {
            if (resultSet != null)
                try {
                    resultSet.close();
                } catch (SQLException exception) {
                    plugin.textUtils.exception(exception.getStackTrace(), exception.getMessage());
                }
            if (statement != null)
                try {
                    statement.close();
                } catch (SQLException exception) {
                    plugin.textUtils.exception(exception.getStackTrace(), exception.getMessage());
                }
        }
        return new String[] { "", String.valueOf(0), "default" };
    }

    public String[] getDrone(final String uuid, final String drone) {
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            statement = connection.createStatement();
            resultSet = statement.executeQuery("SELECT * FROM " + drone + " WHERE uuid= '" + uuid + "';");
            if (resultSet.next()) {
                return new String[]{String.valueOf(resultSet.getInt("unlocked")), String.valueOf(resultSet.getInt("level")), String.valueOf(resultSet.getInt("ammo")), String.valueOf(resultSet.getInt("monsters")), String.valueOf(resultSet.getInt("animals")), String.valueOf(resultSet.getInt("players")), resultSet.getString("exclude"), String.valueOf(resultSet.getInt("health")), String.valueOf(resultSet.getInt("left"))
                };
            }
        } catch (SQLException exception) {
            plugin.textUtils.exception(exception.getStackTrace(), exception.getMessage());
        } finally {
            if (resultSet != null)
                try {
                    resultSet.close();
                } catch (SQLException exception) {
                    plugin.textUtils.exception(exception.getStackTrace(), exception.getMessage());
                }
            if (statement != null)
                try {
                    statement.close();
                } catch (SQLException exception) {
                    plugin.textUtils.exception(exception.getStackTrace(), exception.getMessage());
                }
        }
        return new String[] { String.valueOf(0), String.valueOf(1), String.valueOf(0), String.valueOf(1), String.valueOf(1), String.valueOf(1), "", String.valueOf(0), String.valueOf(0) };
    }

    public void loadOnlinePlayers() {
        if (set()) {
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                final String uuid = player.getUniqueId().toString();
                if (!plugin.list().contains(uuid)) {
                    plugin.load(uuid);
                }
            }
        }
    }
}