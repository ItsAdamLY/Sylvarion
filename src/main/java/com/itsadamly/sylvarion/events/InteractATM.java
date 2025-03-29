package com.itsadamly.sylvarion.events;

import com.itsadamly.sylvarion.Sylvarion;
import com.itsadamly.sylvarion.databases.SylvDBConnect;
import com.itsadamly.sylvarion.databases.bank.SylvBankDBTasks;
import com.itsadamly.sylvarion.events.ATM.SylvATMGUI;
import com.itsadamly.sylvarion.events.ATM.SylvATMOperations;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class InteractATM implements Listener
{
    private static final Sylvarion pluginInstance = Sylvarion.getInstance();

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

        if (event.getView().getTitle().equalsIgnoreCase(ChatColor.DARK_GREEN + "ATM"))
        {
            if (event.getClickedInventory() == null || event.getClickedInventory().equals(player.getInventory()))
            {
                event.setCancelled(true);
                return;
            }

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
                    new SylvATMGUI("ATM | Insert Card", player).openInputCardMenu();
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
            event.setCancelled(true);
            //new SylvATMGUI("ATM", player).openTestMenu();

        }

        else if (!player.isSneaking() && event.getView().getTitle().equalsIgnoreCase(ChatColor.DARK_GREEN +
                "ATM | Test"))
        {
            event.setCancelled(true);

            switch (event.getSlot())
            {
                case 4:
                    player.setHealth(0);
                    player.sendTitle(ChatColor.RED + "so apparently you died", "you activated a death trap", 20, 100, 20);
                    break;

                default:
                    new SylvATMGUI("ATM", player).openATM();
            }
        }

        else if ((event.getView().getTitle().equalsIgnoreCase(
                        ChatColor.DARK_GREEN + "ATM | Insert Card")))
        {
            if (event.getClickedInventory() == null) return;

            if (event.getSlot() != 4 && !event.getClickedInventory().equals(event.getWhoClicked().getInventory()))
            {
                // if the player clicks on the black glasses
                event.setCancelled(true);
                return;
            }

            Bukkit.getScheduler().runTaskLater(pluginInstance, () ->
                            itemInputProcess(event.getInventory(), (Player) event.getWhoClicked()),

                    1);
        }
    }

    @EventHandler
    public void onMenuClose(InventoryCloseEvent event)
    {
       if (!(event.getView().getTitle().equalsIgnoreCase(
               ChatColor.DARK_GREEN + "ATM | Insert Card")))
           return;

       Inventory inventory = event.getInventory();
       ItemStack inputItem = inventory.getItem(4);

       if (inputItem != null)
           event.getPlayer().getInventory().addItem(inputItem);
    }

    public void itemInputProcess(Inventory inventory, Player player)
    {
        ItemStack inputItem = inventory.getItem(4);

        if (inputItem == null || inputItem.getType().equals(Material.AIR))
            return;

        if (inputItem.getType().equals(Material.NAME_TAG) &&
                Objects.requireNonNull(inputItem.getItemMeta()).hasLore())
        {
            String cardID = ChatColor.stripColor(Objects.requireNonNull(inputItem.getItemMeta().getLore()).get(0));
                                // remove any format & colour

            List<Integer> cardDigits = new ArrayList<>();

            for (char s: cardID.toCharArray())
            {
                if (Character.isDigit(s))
                {
                    cardDigits.add(Character.getNumericValue(s));
                }
            }

            player.sendMessage(ChatColor.GOLD + "Card Digits: " + cardDigits);

            if (cardDigits.size() == 16 && cardDigits.get(0) == 5)
            {
                cardDigits.clear();
                cardDigits = null;

                try (Connection connection = SylvDBConnect.sqlConnect())
                {
                    String getCardOwner = new SylvBankDBTasks(connection).getPlayerNameByCard(cardID);

                    if (getCardOwner == null)
                    {
                        player.sendMessage(ChatColor.RED + "Invalid card.");
                        player.getInventory().addItem(inputItem);
                        inventory.setItem(4, null);
                        return;
                    }

                    if (!player.getName().equalsIgnoreCase(getCardOwner))
                    {
                        player.sendMessage(ChatColor.RED + "This card does not belong to you.");
                        player.getInventory().addItem(inputItem);
                        inventory.setItem(4, null);
                        return;
                    }

                    player.sendMessage(ChatColor.GREEN + "Yes");
                    player.getInventory().addItem(inputItem);
                    inventory.setItem(4, null);
                    player.getOpenInventory().close();
                }
                catch (SQLException error)
                {
                    player.sendMessage(ChatColor.RED + "An error occurred, ATM operations cannot be performed.");
                    player.getInventory().addItem(inputItem);
                    inventory.setItem(4, null);
                }
            }

            else
            {
                player.sendMessage(ChatColor.RED + "Invalid card.");
                player.getInventory().addItem(inputItem);
                inventory.setItem(4, null);
            }
        }

        else
        {
            player.sendMessage(ChatColor.RED + "No");
            player.getInventory().addItem(inputItem);
            inventory.setItem(4, null);
        }
    }
}
