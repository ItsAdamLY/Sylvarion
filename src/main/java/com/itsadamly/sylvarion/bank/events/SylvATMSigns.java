package com.itsadamly.sylvarion.bank.events;

import com.itsadamly.sylvarion.Sylvarion;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Objects;

public class SylvATMSigns implements Listener
{
    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final Sylvarion plugin = Sylvarion.getInstance();

    @EventHandler
    public void onSignEdit(SignChangeEvent event)
    {
        if (event.line(0) != null &&
               PlainTextComponentSerializer.plainText().serialize(Objects.requireNonNull(event.line(0)))
                       .equalsIgnoreCase("[ATM]"))
        {
            if (!event.getPlayer().hasPermission("sylv.atm.sign.create")) return;

            event.line(0, MM.deserialize("<green>[ATM]"));
            event.line(1, MM.deserialize("<yellow><italic>© SylvBank"));

            event.getBlock().setMetadata(plugin + "_atm", new FixedMetadataValue(plugin, true));
            event.getPlayer().sendMessage(MM.deserialize("<green>You have created an ATM Machine!"));
        }
    }
}
