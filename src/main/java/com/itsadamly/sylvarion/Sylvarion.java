package com.itsadamly.sylvarion;

import com.itsadamly.sylvarion.commands.SylvATMCommands;
import com.itsadamly.sylvarion.databases.SylvDBConnect;
import com.itsadamly.sylvarion.events.InteractATM;
import com.itsadamly.sylvarion.iciwibridge.BankCard;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import mikeshafter.iciwi.api.IciwiPlugin;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.UnknownDependencyException;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;

public class Sylvarion extends JavaPlugin
{
    private static Sylvarion pluginInstance;
    private static Economy economy;

    @Override
    public void onEnable()
    {
        pluginInstance = this;

        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            commands.registrar().register(SylvATMCommands.command);
        });

        //getServer().getPluginManager().registerEvents(new SylvATMGUI(), this);
        getServer().getPluginManager().registerEvents(new InteractATM(), this);
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();

        // to create new table if not exist
        try
        {
            SylvDBConnect.sqlConnect("sqlite");
            setupEconomy();
        }
        catch (SQLException | IOException error)
        {
            getServer().getLogger().log(Level.SEVERE, "Cannot connect to database. Make sure the DB details are correct.");
            getServer().getLogger().log(Level.WARNING, error.getMessage());

            for (StackTraceElement element : error.getStackTrace())
                getServer().getLogger().log(Level.WARNING, element.toString());

            getServer().getPluginManager().disablePlugin(this);
        }
        catch (UnknownDependencyException error)
        {
            getServer().getLogger().log(Level.SEVERE, "No Economy plugin found.");
            getServer().getLogger().log(Level.WARNING, error.getMessage());

            for (StackTraceElement element : error.getStackTrace())
                getServer().getLogger().log(Level.WARNING, element.toString());

            getServer().getPluginManager().disablePlugin(this);
        }

        if (Bukkit.getServer().getPluginManager().getPlugin("Iciwi") != null)
        {
            IciwiPlugin.registerCard("Sylvarion", BankCard.class);
        }
    }

    public void setupEconomy()
    {
        RegisteredServiceProvider<Economy> economyRSP = getServer().getServicesManager().getRegistration(Economy.class);

        if (economyRSP == null)
        {
            throw new UnknownDependencyException(ChatColor.RED + "No Economy plugin found.");
        }

        economy = economyRSP.getProvider();
    }

    public static Economy getEconomy()
    {
        return economy;
    }

    public static Sylvarion getInstance()
    {
        return pluginInstance;
    }

    @Override
    public void onDisable()
    {
        // Plugin shutdown logic
    }
}
