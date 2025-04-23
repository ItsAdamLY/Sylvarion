package com.itsadamly.sylvarion.events.ATM;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

public class SylvATMSigns implements Listener
{
    @EventHandler
    public void onSignEdit(SignChangeEvent event)
    {
        if (event.getLine(0) == null || (event.getLine(0) != null &&
                !event.getLine(0).equalsIgnoreCase("[ATM]")))
            return;

        if (!event.getPlayer().hasPermission(""))
            return;

        event.setLine(0, ChatColor.GREEN + "[ATM]");
    }
}
