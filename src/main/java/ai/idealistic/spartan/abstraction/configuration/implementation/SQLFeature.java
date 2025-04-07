package ai.idealistic.spartan.abstraction.configuration.implementation;

import ai.idealistic.spartan.Register;
import ai.idealistic.spartan.abstraction.check.CheckEnums;
import ai.idealistic.spartan.abstraction.configuration.ConfigurationBuilder;
import ai.idealistic.spartan.abstraction.protocol.PlayerProtocol;
import ai.idealistic.spartan.api.VacanAPI;
import ai.idealistic.spartan.functionality.concurrent.GeneralThread;
import ai.idealistic.spartan.functionality.moderation.AwarenessNotifications;
import ai.idealistic.spartan.functionality.moderation.CrossServerNotifications;
import ai.idealistic.spartan.functionality.server.MultiVersion;
import ai.idealistic.spartan.functionality.server.PluginBase;
import ai.idealistic.spartan.functionality.server.TPS;
import ai.idealistic.spartan.functionality.tracking.AntiCheatLogs;
import ai.idealistic.spartan.utils.java.StringUtils;
import ai.idealistic.spartan.utils.math.AlgebraUtils;
import org.bukkit.Material;

import java.sql.*;

public class SQLFeature extends ConfigurationBuilder {

    private static final GeneralThread.ThreadPool sqlThread = new GeneralThread.ThreadPool(TPS.tickTime);

    public SQLFeature() {
        super("sql");
    }

    private static boolean enabled = false;

    private static Connection con = null;

    // Separator

    public String getHost() {
        String result = getString("host");

        if (result != null) {
            result = result.toLowerCase().replace("localhost", "127.0.0.1").replace(" ", "");
        }
        return result;
    }

    public String getUser() {
        String result = getString("user");

        if (result != null) {
            result = result.replace(" ", "");
        }
        return result;
    }

    public String getPassword() {
        String result = getString("password");

        if (result != null && getBoolean("escape_special_characters")) {
            result = StringUtils.escapeMetaCharacters(result);
        }
        return result;
    }

    public String getDatabase() {
        String result = getString("database");

        if (result != null) {
            result = result.replace(" ", "");
        }
        return result;
    }

    public String getTable() {
        String result = getString("table");

        if (result != null) {
            result = result.replace(" ", "");
        }
        return result;
    }

    public String getPort() {
        String result = getString("port");

        if (result != null) {
            result = result.replace(" ", "");
            Double decimal = AlgebraUtils.returnValidDecimal(result);

            if (decimal != null) {
                result = String.valueOf(AlgebraUtils.integerFloor(decimal));
            }
            return result;
        } else {
            return null;
        }
    }

    public String getDriver() {
        String result = getString("driver");

        if (result == null) {
            result = "mysql";
        }
        return result;
    }

    public String getTLSVersion() {
        return getString("tls_Version");
    }

    public boolean getSSL() {
        return getBoolean("use_SSL");
    }

    public boolean getPublicKeyRetrieval() {
        return getBoolean("allow_public_key_retrieval");
    }

    // Separator

    @Override
    public void clear() {
        super.clear();
        enabled = true;
    }

    public void refreshDatabase() {
        if (isConnected(false)) {
            try {
                con.close();
            } catch (Exception ignored) {
            }
        }
        con = null;
    }

    public boolean isEnabled() {
        return enabled;
    }

    // Separator

    @Override
    public void create() {
        addOption("host", "");
        addOption("user", "");
        addOption("password", "");
        addOption("database", "");
        addOption("table", Register.command + "_logs");
        addOption("port", "3306");
        addOption("driver", "mysql");
        addOption("tls_Version", "");
        addOption("use_SSL", true);
        addOption("allow_public_key_retrieval", false);
        addOption("escape_special_characters", false);
        sqlThread.executeWithPriority(this::connect);
    }

    // Separator

    private boolean isConnected(boolean message) {
        if (con != null) {
            try {
                return !con.isClosed();
            } catch (Exception e) {
                if (message) {
                    AwarenessNotifications.forcefullySend("SQL Connection Check Error:\n" + e.getMessage());
                }
            }
        }
        return false;
    }

    public void connect() {
        String host = getHost(),
                user = getUser(),
                password = getPassword(),
                database = getDatabase(),
                table = getTable(),
                port = getPort();
        int hostLength = host.length(),
                userLength = user.length(),
                passwordLength = password.length(),
                databaseLength = database.length();

        // Check if the user has configured the config even a little
        if (hostLength > 0 || userLength > 0 || passwordLength > 0 || databaseLength > 0) { // Do not check table, port & driver
            if (hostLength == 0) {
                enabled = false;
                AwarenessNotifications.forcefullySend("SQL Configuration Error: Host is blank");
            } else if (userLength == 0) {
                enabled = false;
                AwarenessNotifications.forcefullySend("SQL Configuration Error: User is blank");
            } else if (passwordLength == 0) {
                enabled = false;
                AwarenessNotifications.forcefullySend("SQL Configuration Error: Password is blank");
            } else if (databaseLength == 0) {
                enabled = false;
                AwarenessNotifications.forcefullySend("SQL Configuration Error: Database is blank");
            } else if (table.isEmpty()) {
                enabled = false;
                AwarenessNotifications.forcefullySend("SQL Configuration Error: Table is blank");
            } else if (!AlgebraUtils.validInteger(port) && !AlgebraUtils.validDecimal(port)) {
                enabled = false;
                AwarenessNotifications.forcefullySend("SQL Configuration Error: Port is not a valid number");
            } else if (!isConnected(true)) {
                String driver = getDriver();

                try {
                    if (driver.isEmpty()) {
                        AwarenessNotifications.forcefullySend("SQL Configuration Error: Driver is blank");
                    } else {
                        String tlsVersion = getTLSVersion();
                        con = DriverManager.getConnection("jdbc:" + driver + "://" + host + ":" + port + "/" + database + "?" +
                                        "autoReconnect=true" +
                                        "&maxReconnects=10" +
                                        (tlsVersion != null && !tlsVersion.isEmpty() ? "&enabledTLSProtocols=TLSv" + tlsVersion : "") +
                                        "&useSSL=" + getSSL() +
                                        "&allowPublicKeyRetrieval=" + getPublicKeyRetrieval(),
                                user, password);
                        createTable(table);
                    }
                } catch (SQLException e) {
                    AwarenessNotifications.forcefullySend("SQL Initial Connection Error:\n" + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    // Separator

    public void update(String command) {
        sqlThread.execute(() -> {
            connect();

            try {
                if (con != null) {
                    Statement st = con.createStatement();
                    st.executeUpdate(command);
                    st.close();
                }
            } catch (Exception e) {
                AwarenessNotifications.forcefullySend("SQL Update Error:\n"
                        + "Command: " + command + "\n"
                        + "Exception: " + e.getMessage());
            }
        });
    }

    public ResultSet query(final String command) {
        ResultSet[] rs = new ResultSet[1];
        Thread thread = Thread.currentThread();

        sqlThread.execute(() -> {
            connect();

            try {
                if (con != null) {
                    final Statement st = con.createStatement();
                    rs[0] = st.executeQuery(command);

                    synchronized (thread) {
                        thread.notifyAll();
                    }
                }
            } catch (Exception e) {
                AwarenessNotifications.forcefullySend("SQL Query Error:\n"
                        + "Command: " + command + "\n"
                        + "Exception: " + e.getMessage());
            }
        });
        synchronized (thread) {
            if (rs[0] == null) {
                try {
                    thread.wait();
                } catch (Exception ignored) {
                }
            }
        }
        return rs[0];
    }

    // Separator

    private void createTable(String table) {
        update(
                "CREATE TABLE IF NOT EXISTS " + table + " (" +
                        "id INT(11) NOT NULL AUTO_INCREMENT, " +
                        "creation_date VARCHAR(30), " +

                        "server_name VARCHAR(64), " +
                        "plugin_version VARCHAR(16), " +
                        "server_version VARCHAR(7), " +
                        "online_players INT(11), " +

                        "type VARCHAR(32), " +
                        "information VARCHAR(4096), " +
                        "notification VARCHAR(4096), " +

                        "player_uuid VARCHAR(36), " +
                        "player_name VARCHAR(24), " +
                        "player_latency INT(11), " +

                        "functionality VARCHAR(32), " +

                        "primary key (id));"
        );
    }

    // Separator

    public void logInfo(PlayerProtocol p,
                        String notification,
                        String information,
                        Material material,
                        CheckEnums.HackType hackType,
                        long time) {
        if (enabled) {
            String table = getTable();
            boolean hasPlayer = p != null,
                    hasCheck = hackType != null,
                    hasMaterial = material != null;
            update(
                    "INSERT INTO " + table
                            + " (creation_date"
                            + ", server_name, plugin_version, server_version, online_players"
                            + ", type, notification, information"
                            + ", player_uuid, player_name, player_latency"
                            + ", functionality) "
                            + "VALUES (" + syntaxForColumn(AntiCheatLogs.getDate(AntiCheatLogs.dateFormat, time))
                            + ", " + syntaxForColumn(CrossServerNotifications.getServerName())
                            + ", " + syntaxForColumn(VacanAPI.getVersion())
                            + ", " + syntaxForColumn(MultiVersion.serverVersion.toString())
                            + ", " + syntaxForColumn(PluginBase.getPlayerCount())
                            + ", " + syntaxForColumn(hasMaterial ? "mining" : hasCheck ? "violation" : "other")
                            + ", " + (notification != null ? syntaxForColumn(notification) : "NULL")
                            + ", " + syntaxForColumn(information)
                            + ", " + (hasPlayer ? syntaxForColumn(p.getUUID()) : "NULL")
                            + ", " + (hasPlayer ? syntaxForColumn(p.bukkit().getName()) : "NULL")
                            + ", " + (hasPlayer ? syntaxForColumn(p.getPing()) : "NULL")
                            + ", " + (hasMaterial ? syntaxForColumn(material) : hasCheck ? syntaxForColumn(hackType) : "NULL")
                            + ");"
            );
        }
    }

    private String syntaxForColumn(Object obj) {
        return "'" + obj.toString() + "'";
    }
}
