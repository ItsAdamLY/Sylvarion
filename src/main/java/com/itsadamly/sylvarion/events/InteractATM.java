package com.itsadamly.sylvarion.events;

import com.itsadamly.sylvarion.Sylvarion;
import com.itsadamly.sylvarion.databases.SylvDBConnect;
import com.itsadamly.sylvarion.databases.bank.SylvBankDBTasks;
import com.itsadamly.sylvarion.events.ATM.SylvATMGUIOpener;
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
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class InteractATM implements Listener
{
    private static final Sylvarion pluginInstance = Sylvarion.getInstance();
    private final HashMap<String, String> cardTarget = new HashMap<>();

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
                new SylvATMGUIOpener("ATM", event.getPlayer()).openATM();
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
                    new SylvATMGUIOpener("ATM | Insert Card", player).openInputCardMenu();
                    break;
                //  Receive card details

                case 5:
                    new SylvATMGUIOpener("ATM | Test", player).openTestMenu();
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
            //new SylvATMGUIOpener("ATM", player).openTestMenu();

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
                    new SylvATMGUIOpener("ATM", player).openATM();
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

            // the scheduler is for handling timings for inventory updates
            Bukkit.getScheduler().runTaskLater(pluginInstance, () ->
                            itemInputProcess(event.getInventory(), (Player) event.getWhoClicked()),

                    1);
        }

        else if (event.getView().getTitle().equalsIgnoreCase(
                ChatColor.DARK_GREEN + "ATM | Operations"))
        {
            event.setCancelled(true);

            if (event.getClickedInventory() == null || event.getClickedInventory().equals(player.getInventory()))
            {
                return;
            }

            switch (event.getSlot())
            {
                case 0:
                    cardTarget.remove(player.getName());
                    new SylvATMGUIOpener("ATM", player).openATM();
                    break;

                case 2:
                    new SylvATMGUIOpener("ATM | Withdraw", player).openValuesMenu();
                    break;

                case 3:
                    try (Connection connection = SylvDBConnect.sqlConnect())
                    {
                        new SylvBankDBTasks(connection).getCardBalance(cardTarget.get(player.getName()));
                        double balance = new SylvBankDBTasks(connection).getCardBalance(cardTarget.get(player.getName()));

                        String title = "ATM | Balance: " + ChatColor.GREEN +
                                "Ⓤ " + String.format("%.2f", balance) + ChatColor.DARK_GREEN + ". Continue operations?";

                        new SylvATMGUIOpener(title, player).openConfirmMenu();
                    }
                    catch (SQLException error)
                    {
                        player.sendMessage(ChatColor.RED + "An error occurred. Cannot fetch balance.");
                    }
                    break;

                case 5:
                    new SylvATMGUIOpener("ATM | Deposit", player).openValuesMenu();
                    break;

                case 6:
                    event.getView().close();
                    cardTarget.remove(player.getName());
                    player.sendMessage(ChatColor.RED + "TBD");
                    break;

                case 8:
                    event.getView().close();
                    cardTarget.remove(player.getName());
                    break;
            }
        }

        else if (event.getView().getTitle().contains("ATM | Balance:"))
        {
            event.setCancelled(true);

            if (event.getClickedInventory() == null || event.getClickedInventory().equals(player.getInventory()))
            {
                return;
            }

            switch (event.getSlot())
            {
                case 3:
                    new SylvATMGUIOpener("ATM | Operations", player).openATMOperationsMenu();
                    break;

                case 5:
                    cardTarget.remove(player.getName());
                    event.getView().close();
                    break;
            }
        }

        else if (event.getView().getTitle().equalsIgnoreCase(ChatColor.DARK_GREEN + "ATM | Withdraw") ||
                    event.getView().getTitle().equalsIgnoreCase(ChatColor.DARK_GREEN + "ATM | Deposit"))
        {
            event.setCancelled(true);
            double amount = 0;

            if (event.getClickedInventory() == null || event.getClickedInventory().equals(player.getInventory()))
            {
                return;
            }

            double[] values = { 10.00, 20.00, 50.00, 100.00, 200.00, 500.00, 1000.00 };

            if (event.getSlot() >= 1 && event.getSlot() <= 7)
            {
                amount = values[event.getSlot() - 1];
            }

            else
            {
                if (event.getSlot() == 0)
                {
                    new SylvATMGUIOpener("ATM | Operations", player).openATMOperationsMenu();
                    return;
                }
                else if (event.getSlot() == 8)
                {
                    event.getView().close();
                    return;
                }
            }

            if (event.getSlot() != 0 && event.getSlot() != 8)
            {
                boolean success;

                if (event.getView().getTitle().equalsIgnoreCase(ChatColor.DARK_GREEN + "ATM | Withdraw"))
                {

                    //new SylvATMGUIOpener("ATM | Confirm Withdraw: $" + amount, player).openConfirmMenu();
                    try (Connection connection = SylvDBConnect.sqlConnect())
                    {
                        success = new SylvATMOperations(connection).withdraw(player, cardTarget.get(player.getName()), amount);
                        if (success) event.getView().close();
                    }
                    catch (SQLException error)
                    {
                        player.sendMessage(ChatColor.RED + "An error occurred. Cannot withdraw.");
                    }
                }
                else if (event.getView().getTitle().equalsIgnoreCase(ChatColor.DARK_GREEN + "ATM | Deposit"))
                {
                    //new SylvATMGUIOpener("ATM | Confirm Deposit: $" + amount, player).openConfirmMenu();
                    try (Connection connection = SylvDBConnect.sqlConnect())
                    {
                        success = new SylvATMOperations(connection).deposit(player, cardTarget.get(player.getName()), amount);
                        if (success) event.getView().close();
                    }
                    catch (SQLException error)
                    {
                        player.sendMessage(ChatColor.RED + "An error occurred. Cannot deposit.");
                    }
                }
            }
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

       if (inputItem != null && !inputItem.getType().equals(Material.AIR))
           event.getPlayer().getInventory().addItem(inputItem);

       cardTarget.remove(event.getPlayer().getName());
    }

    public void itemInputProcess(Inventory inventory, Player player)
    {
        ItemStack inputItem = inventory.getItem(4);

        if (inputItem == null || inputItem.getType().equals(Material.AIR))
            return;

        player.getInventory().addItem(inputItem);
        inventory.setItem(4, null);

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
                        return;
                    }

                    new SylvATMGUIOpener("ATM | Operations", player).openATMOperationsMenu();
                    cardTarget.put(player.getName(), getCardOwner);
                }
                catch (SQLException error)
                {
                    player.sendMessage(ChatColor.RED + "An error occurred, ATM operations cannot be performed.");
                }
            }

            else
            {
                player.sendMessage(ChatColor.RED + "Invalid card details.");
            }
        }

        else
        {
            player.sendMessage(ChatColor.RED + "Invalid item.");
        }
    }
}