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
        return getConfigValue("dbpath");
    }

    public static String getDBName()
    {
        return getConfigValue("dbname");
    }

    public static String getDBUserName()
    {
        return getConfigValue("db_username");
    }

    public static String getDBPassword()
    {
        return getConfigValue("db_password");
    }

    public static String getDBTableName()
    {
        return getConfigValue("db_tablename");
    }

    public static double getDepositValue()
    {
        return Double.parseDouble(getConfigValue("initialDeposit"));
    }

}
