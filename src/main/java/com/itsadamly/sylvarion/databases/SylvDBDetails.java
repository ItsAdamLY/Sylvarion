package com.itsadamly.sylvarion.databases;

import com.itsadamly.sylvarion.Sylvarion;

import java.sql.Connection;

public class SylvDBDetails
{
    private static final Connection connectionSQL = SylvDBConnect.getSQLConnection();
    private static final Sylvarion pluginInstance = Sylvarion.getInstance();

    private static String getConfigValue(String key)
    {
        return pluginInstance.getConfig().getString(key);
    }

    public static String getDBPath()
    {
        return getConfigValue("database.path");
    }

    public static String getDBName()
    {
        return getConfigValue("database.name");
    }

    public static String getDBUserName()
    {
        return getConfigValue("database.username");
    }

    public static String getDBPassword()
    {
        return getConfigValue("database.password");
    }

    public static String getDBUserTableName()
    {
        return getConfigValue("database.tablenames.user");
    }

    public static String getDBTerminalTableName ()
    {
        return getConfigValue("database.tablenames.terminal"); 
    }

    public static double getDepositValue()
    {
        return Double.parseDouble(getConfigValue("initialDeposit"));
    }

}
