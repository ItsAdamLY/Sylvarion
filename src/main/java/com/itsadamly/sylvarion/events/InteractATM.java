package com.itsadamly.sylvarion.events;

import com.itsadamly.sylvarion.databases.SylvDBConnect;
import com.itsadamly.sylvarion.events.ATM.SylvATMGUI;
import com.itsadamly.sylvarion.events.ATM.SylvATMOperations;
import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.sql.Connection;
import java.sql.SQLException;

public class InteractATM implements Listener
{
    private final Connection connection = SylvDBConnect.getSQLConnection();

    @EventHandler
    public void onInteractATM(PlayerInteractEvent event)
    {
        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
        {
            if (event.getClickedBlock().getState() instanceof Sign &&
                    ((Sign) event.getClickedBlock().getState()).getSide(Side.FRONT).getLine(0)
                                    .equalsIgnoreCase("[ATM]"))
            {
                event.setCancelled(true);
                new SylvATMGUI(event.getPlayer()).openGUI();
            }
        }
    }

    @EventHandler
    public void menuEvent(InventoryClickEvent event)
    {
        if (event.getView().getTitle().equalsIgnoreCase(ChatColor.DARK_GREEN + "ATM"))
        {
            event.setCancelled(true);
            event.setCurrentItem(null);

            switch (event.getSlot())
            {
                // Open Account
                case 1:
                    try (connection)
                    {
                        new SylvATMOperations(connection).openAccount(event.getWhoClicked(), (Player) event.getWhoClicked());
                        event.getView().close();
                    }
                    catch (SQLException error)
                    {
                        event.getWhoClicked().sendMessage(ChatColor.RED + "An error occurred, ATM operations cannot be performed.");
                    }
                    break;

                case 4:
                    //  Receive card details


                // Close Account
                case 7:
                    new SylvATMOperations(connection).closeAccount(event.getWhoClicked(), event.getWhoClicked().getName());
                    event.getView().close();
                    break;
            }
        }
    }
}
