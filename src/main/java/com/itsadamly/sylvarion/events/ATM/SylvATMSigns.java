package com.itsadamly.sylvarion.events.ATM;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

import java.util.Objects;

public class SylvATMSigns implements Listener {
    private static final MiniMessage MM = MiniMessage.miniMessage();
    @EventHandler
    public void onSignEdit(SignChangeEvent event)
    {
        if (event.line(0) == null || (event.line(0) != null &&
                !Objects.requireNonNull(event.line(0)).toString().equalsIgnoreCase("[ATM]")))
            return;

        if (!event.getPlayer().hasPermission(""))
            return;

        event.line(0, MM.deserialize("<green>[ATM]"));
    }
}
