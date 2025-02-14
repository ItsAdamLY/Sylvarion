package com.itsadamly.sylvarion.databases;

import com.itsadamly.sylvarion.Sylvarion;
import com.itsadamly.sylvarion.databases.bank.SylvBankDBTasks;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;

public class SylvDBConnect
{
    private static Connection connection = null;
    private static final Sylvarion pluginInstance = Sylvarion.getInstance();

    public void sqlConnect() throws SQLException
    {
        String URL = SylvDBDetails.getDBPath();
        String dbName = SylvDBDetails.getDBName();
        String userName = SylvDBDetails.getDBUserName();
        String password = SylvDBDetails.getDBPassword();

        connection = DriverManager.getConnection("jdbc:mysql://" + URL + "/" + dbName, userName, password);
        pluginInstance.getServer().getLogger().log(Level.FINEST, "Database " + dbName + " loaded" +
                " successfully!");

        new SylvBankDBTasks().createTables();
    }

    public static Connection getSQLConnection()
    {
        return connection;
    }
}
