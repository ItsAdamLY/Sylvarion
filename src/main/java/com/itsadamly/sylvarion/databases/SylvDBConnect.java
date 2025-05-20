package com.itsadamly.sylvarion.databases;

import com.itsadamly.sylvarion.Sylvarion;
import com.itsadamly.sylvarion.databases.bank.SylvBankDBTasks;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;

public class SylvDBConnect
{
    private static final Sylvarion pluginInstance = Sylvarion.getInstance();
    private static final int POOL_SIZE = 10;
    //private static final List<Connection> connections = new ArrayList<>(POOL_SIZE);

    private static Connection connection = null;

    public static void sqlConnect(String driver) throws SQLException, IOException
    {
        if (!driver.equalsIgnoreCase("sqlite") && !driver.equalsIgnoreCase("mysql"))
        {
            throw new SQLException("Unsupported database driver: " + driver + " (yet). Please use SQLite.");
        }

        if (driver.equalsIgnoreCase("sqlite"))
        {
            String dbName = SylvDBDetails.getDBName();
            File pluginFileDir = pluginInstance.getDataFolder();
            File newDBFile = new File(pluginFileDir, dbName + ".db");

            if (newDBFile.createNewFile()) {
                System.out.println("File created: " + newDBFile.getName());
            }

            String url = "jdbc:" + driver + ":" + newDBFile.getPath();
            connection = DriverManager.getConnection(url);
        }

        else
        {
            String url = "jdbc:" + driver + "://" + SylvDBDetails.getDBPath() + "/" + SylvDBDetails.getDBName() + "?autoReconnect=true&connectionTimeout=10000";
            connection = DriverManager.getConnection(url, SylvDBDetails.getDBUserName(), SylvDBDetails.getDBPassword());
        }

        connection.setAutoCommit(true);
        new SylvBankDBTasks(connection).createTables();
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

    public static Connection getConnection() throws SQLException
    {
        /*if (connections.isEmpty()) {
            for (int i = 0; i < POOL_SIZE; i++) {
                connections.add(sqlConnect());
            }
        }*/

        return connection;
    }

    public static void sqlDisconnect()
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
    }
}