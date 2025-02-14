package com.itsadamly.sylvarion;

import com.itsadamly.sylvarion.commands.SylvCommands;
import com.itsadamly.sylvarion.databases.SylvDBConnect;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;

import java.util.logging.Level;

public class Sylvarion extends JavaPlugin
{
    private static Sylvarion pluginInstance;

    @Override
    public void onEnable()
    {
        pluginInstance = this;
        getCommand("atm").setExecutor(new SylvCommands());

        getConfig().options().copyDefaults(true);
        saveDefaultConfig();

        try
        {
            new SylvDBConnect().sqlConnect();
        }

        catch (SQLException error)
        {
            getServer().getLogger().log(Level.SEVERE, "Cannot connect to database. Make sure the DB details are correct.");
            getServer().getLogger().log(Level.WARNING, error.getMessage());
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    public static Sylvarion getInstance() {return pluginInstance;}

    @Override
    public void onDisable()
    {
        // Plugin shutdown logic
    }
}
