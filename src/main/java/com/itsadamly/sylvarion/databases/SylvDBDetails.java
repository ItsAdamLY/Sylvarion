package com.itsadamly.sylvarion.databases;

import com.itsadamly.sylvarion.Sylvarion;

public class SylvDBDetails
{
    private static final Sylvarion pluginInstance = Sylvarion.getInstance();

    private static String getConfigValue(String key)
    {
        return pluginInstance.getConfig().getString(key);
    }

    public static String getDriver()
    {
        return getConfigValue("database.driver");
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

    public static String getDBTerminalTableName()
    {
        return getConfigValue("database.tablenames.terminal"); 
    }

    public static double getDepositValue()
    {
        return Double.parseDouble(getConfigValue("initialDeposit"));
    }

    public static String getCurrencySymbol()
    {
        if (getConfigValue("currencySymbol") == null || getConfigValue("currencySymbol").isEmpty())
            return "$"; //default currency symbol

        return getConfigValue("currencySymbol");
    }
}
