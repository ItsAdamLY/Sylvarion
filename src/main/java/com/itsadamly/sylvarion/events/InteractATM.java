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
    @EventHandler
    public void onInteractATM(PlayerInteractEvent event)
    {
        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && !event.getPlayer().isSneaking())
        {
            if (event.getClickedBlock().getState() instanceof Sign &&
                    ((Sign) event.getClickedBlock().getState()).getSide(Side.FRONT).getLine(0)
                                    .equalsIgnoreCase("[ATM]"))
            {
                event.setCancelled(true);
                new SylvATMGUI("ATM", event.getPlayer()).openATM();
            }
        }
    }

    @EventHandler
    public void menuEvent(InventoryClickEvent event)
    {
        Player player = (Player) event.getWhoClicked();

        if (!player.isSneaking() && event.getView().getTitle().equalsIgnoreCase(ChatColor.DARK_GREEN + "ATM"))
        {
            event.setCancelled(true);
            event.setCurrentItem(null);

            switch (event.getSlot())
            {
                // Open Account
                case 1:
                    try (Connection connection = SylvDBConnect.sqlConnect())
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
                    break;
                    //  Receive card details

                case 5:
                    new SylvATMGUI("ATM | Test", player).openTestMenu();
                    break;


                // Close Account
                case 7:
                    try (Connection connection = SylvDBConnect.sqlConnect())
                    {
                        new SylvATMOperations(connection).closeAccount(event.getWhoClicked(), event.getWhoClicked().getName());
                        event.getView().close();
                    }
                    catch (SQLException error)
                    {
                        event.getWhoClicked().sendMessage(ChatColor.RED + "An error occurred, ATM operations cannot be performed.");
                    }
                    break;

            }
        }

        else if (!player.isSneaking() && event.getView().getTitle().equalsIgnoreCase(ChatColor.DARK_GREEN +
                "ATM | Test"))
        {
            event.setCancelled(true);

            switch (event.getSlot())
            {
                case 4:
                    player.setHealth(0);
                    player.sendTitle(ChatColor.RED + "so apparently you died", "you activated a death trap",
                            20, 100, 20);
                    break;

                default:
                    new SylvATMGUI("ATM", player).openATM();
            }
        }
    }
}
