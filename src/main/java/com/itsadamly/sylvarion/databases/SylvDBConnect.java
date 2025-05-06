package com.itsadamly.sylvarion.databases;

import com.itsadamly.sylvarion.Sylvarion;
import com.itsadamly.sylvarion.databases.bank.SylvBankDBTasks;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class SylvDBConnect {

    private static final Sylvarion pluginInstance = Sylvarion.getInstance();
    private static final int POOL_SIZE = 10;
    private static final List<Connection> connections = new ArrayList<>(POOL_SIZE);

    private static Connection sqlConnect() throws SQLException {
        String url = "jdbc:" + SylvDBDetails.getDriver() + "://" +
                SylvDBDetails.getDBPath() + "/" + SylvDBDetails.getDBName() +
                "?autoReconnect=true";

        Connection connection = DriverManager.getConnection(
                url,
                SylvDBDetails.getDBUserName(),
                SylvDBDetails.getDBPassword()
        );

        new SylvBankDBTasks(connection).createTables();
        return connection;
    }

/*    public static void sqlDisconnect(Connection connection)
    {
        if (connection == null) return;

        try
        {
            connection.close();
            pluginInstance.getServer().getLogger().log(Level.FINEST, "Database connection closed.");
        }
        catch (SQLException error)
        {
            pluginInstance.getServer().getLogger().log(Level.SEVERE, "An error occurred whilst terminating the database connection.", error);
        }
    }*/

    public static Connection getConnection() throws SQLException {
        if (connections.isEmpty()) {
            for (int i = 0; i < POOL_SIZE; i++) {
                connections.add(sqlConnect());
            }
        }

        return connections.remove(connections.size() - 1);
    }

    public static void releaseConnection(Connection connection) {
        if (connection != null) {
            connections.add(connection);
        }
    }

/*    public void checkConnection()
    {
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                try
                {
                    if (connection.isClosed())
                    {
                        sqlConnect();
                    }
                }
                catch (SQLException error)
                {
                     pluginInstance.getLogger().log(Level.SEVERE, "An error occurred whilst reconnecting to the database.", error);
                }
            }
        }.runTaskTimer(pluginInstance, 0, 20*60*60);
    }*/
}
