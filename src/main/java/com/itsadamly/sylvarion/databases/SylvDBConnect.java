package com.itsadamly.sylvarion.databases;

import com.itsadamly.sylvarion.Sylvarion;
import com.itsadamly.sylvarion.databases.bank.SylvBankDBTasks;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;

public class SylvDBConnect
{
    private static final Sylvarion pluginInstance = Sylvarion.getInstance();
    private static Connection connection = null;

    public static Connection sqlConnect() throws SQLException
    {
        String URL = SylvDBDetails.getDBPath();
        String dbName = SylvDBDetails.getDBName();
        String userName = SylvDBDetails.getDBUserName();
        String password = SylvDBDetails.getDBPassword();
        String driver = SylvDBDetails.getDriver();

        connection = DriverManager.getConnection("jdbc:" + driver + "://" + URL + "/" + dbName + "?autoReconnect=true", userName, password);
        pluginInstance.getServer().getLogger().log(Level.FINEST, "Database " + dbName + " loaded" + " successfully!");

        new SylvBankDBTasks(connection).createTables();

        return connection;
    }

    public static void sqlDisconnect(Connection connection)
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
    }

    public void checkConnection()
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
    }
}
