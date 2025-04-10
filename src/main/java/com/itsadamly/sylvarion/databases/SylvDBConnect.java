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

public class SylvDBConnect
{
    private static final Sylvarion pluginInstance = Sylvarion.getInstance();

    private static final int POOL_SIZE = 10;
    private static final List<Connection> connections = new ArrayList<>(POOL_SIZE);

    private static Connection sqlConnect() throws SQLException
    {
        String URL = SylvDBDetails.getDBPath();
        String dbName = SylvDBDetails.getDBName();
        String userName = SylvDBDetails.getDBUserName();
        String password = SylvDBDetails.getDBPassword();
        String driver = SylvDBDetails.getDriver();

        Connection initConnection = DriverManager.getConnection("jdbc:" + driver + "://" + URL + "/" + dbName + "?autoReconnect=true", userName, password);
        new SylvBankDBTasks(initConnection).createTables();

        return initConnection;
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
            pluginInstance.getServer().getLogger().log(Level.SEVERE, "An error occurred whilst terminating the database connection.");
            pluginInstance.getServer().getLogger().log(Level.WARNING, error.getMessage());
        }
    }*/

    public static Connection getConnection() throws SQLException
    {
        if (connections.isEmpty())
        {
            connections.add(sqlConnect()); // try to connect to SQL
        }

        return connections.remove(connections.size() - 1);
    }

    public static void releaseConnection(Connection connection)
    {
        if (connection == null) return;
        connections.add(connection);
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
                     pluginInstance.getLogger().log(Level.SEVERE, "An error occurred whilst reconnecting to the database.");
                     pluginInstance.getLogger().log(Level.WARNING, error.getMessage());
                }
            }
        }.runTaskTimer(pluginInstance, 0, 20*60*60);
    }*/
}
