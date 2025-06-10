package com.itsadamly.sylvarion.bank.events;

import com.itsadamly.sylvarion.Sylvarion;
import com.itsadamly.sylvarion.databases.SylvDBConnect;
import com.itsadamly.sylvarion.databases.SylvDBDetails;
import com.itsadamly.sylvarion.databases.bank.SylvBankDBTasks;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.util.*;

public class InteractATM extends SylvATMSigns
{
    private static final Sylvarion pluginInstance = Sylvarion.getInstance();
    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final String CURRENCY = SylvDBDetails.getCurrencySymbol();
    private final HashMap<String, String> cardTarget = new HashMap<>();
    private static Connection connection = null;

    private static final Map<Player, SylvATM> atmUsers = new HashMap<>();

    @EventHandler
    public void onInteractATM(PlayerInteractEvent event)
    {
        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && !event.getPlayer().isSneaking())
        {
            if (Objects.requireNonNull(event.getClickedBlock()).getState() instanceof Sign &&
                    ((Sign) event.getClickedBlock().getState()).getSide(Side.FRONT).line(0)
                            .equals(MM.deserialize("[ATM]")))
            {
                event.setCancelled(true);
                atmUsers.putIfAbsent(event.getPlayer(), new SylvATM(event.getPlayer()));
                atmUsers.get(event.getPlayer()).openATM();
            }
        }
    }

    @EventHandler
    public void menuEvent(InventoryClickEvent event) 
    {
        Player player = (Player) event.getWhoClicked();

        if (event.getView().title().equals(MM.deserialize("<dark_green>ATM"))) 
        {
            if (event.getClickedInventory() == null || event.getClickedInventory().equals(player.getInventory())) 
            {
                event.setCancelled(true);
                return;
            }

            switch (event.getSlot())
            {
                case 1:
                    try
                    {
                        connection = SylvDBConnect.getConnection();
                        new SylvATMOperations(connection).openAccount(event.getWhoClicked(), (Player) event.getWhoClicked());
                        event.getView().close();
                    }
                    catch (SQLException error)
                    {
                        event.getWhoClicked().sendMessage(MM.deserialize("<red>An error occurred, ATM operations cannot be performed."));
                    }
                    break;

                case 4:
                    atmUsers.get(player).openInputCardMenu();
                    break;

                case 5:
                    atmUsers.get(player).openTestMenu();
                    break;

                case 7:
                    try
                    {
                        connection = SylvDBConnect.getConnection();
                        new SylvATMOperations(connection).closeAccount(event.getWhoClicked(), event.getWhoClicked().getName());
                        event.getView().close();
                    }
                    catch (SQLException error)
                    {
                        event.getWhoClicked().sendMessage(MM.deserialize("<red>An error occurred, ATM operations cannot be performed."));
                    }
                    /*finally {
                        SylvDBConnect.releaseConnection(connection);
                    }*/
                    break;
            }
            event.setCancelled(true);
        }
        else if (!player.isSneaking() && event.getView().title().equals(MM.deserialize("<dark_green>ATM | Test")))
        {
            event.setCancelled(true);
            if (event.getSlot() == 4)
            {
                player.setHealth(0);
                player.showTitle(Title.title(
                                MM.deserialize("<red>so apparently you died"),
                                MM.deserialize("you activated a death trap"),
                                Title.Times.times(Duration.ofSeconds(1), Duration.ofMillis(5), Duration.ofMillis(1))
                                )
                );
            }
            else
            {
                atmUsers.get(player).openATM();
            }
        }
        else if (event.getView().title().equals(MM.deserialize("<dark_green>ATM | Insert Card")))
        {
            if (event.getClickedInventory() == null) return;
            if (event.getSlot() != 4 && !event.getClickedInventory().equals(event.getWhoClicked().getInventory())) {
                event.setCancelled(true);
                return;
            }

            // Scheduler is used to delay the item input process (allowing user interaction to be processed)
            Bukkit.getScheduler().runTaskLater(pluginInstance, () ->
                    itemInputProcess(event.getInventory(), (Player) event.getWhoClicked()), 1);
        }
        else if (event.getView().title().equals(MM.deserialize("<dark_green>ATM | Operations")))
        {
            event.setCancelled(true);

            if (event.getClickedInventory() == null || event.getClickedInventory().equals(player.getInventory()))
                return;

            switch (event.getSlot())
            {
                case 0:
                    cardTarget.remove(player.getName());
                    atmUsers.get(player).openATM();
                    break;

                case 2:
                    atmUsers.get(player).openValuesMenu(MM.deserialize("<dark_green>ATM | Withdraw"));
                    break;

                case 3:
                    try
                    {
                        connection = SylvDBConnect.getConnection();
                        double balance = new SylvBankDBTasks(connection).getCardBalance(cardTarget.get(player.getName()));

                        Component titleComp = MM.deserialize("<dark_green>ATM | Balance: <green>" +
                                CURRENCY + " " + String.format("%.2f", balance) + "<dark_green>. Continue?");

                        atmUsers.get(player).openConfirmMenu(titleComp);
                    }
                    catch (SQLException error)
                    {
                        player.sendMessage(MM.deserialize("<red>An error occurred. Cannot fetch balance."));
                    }
                    /*finally
                    {
                        SylvDBConnect.releaseConnection(connection);
                    }*/
                    break;

                case 5:
                    atmUsers.get(player).openValuesMenu(MM.deserialize("<dark_green>ATM | Deposit"));
                    break;

                case 6:
                    event.getView().close();
                    cardTarget.remove(player.getName());
                    player.sendMessage(MM.deserialize("<red>TBD"));
                    break;

                case 8:
                    event.getView().close();
                    cardTarget.remove(player.getName());
                    break;
            }
        }
        else if (PlainTextComponentSerializer.plainText().serialize(event.getView().title()).contains("ATM | Balance:"))
        {
            event.setCancelled(true);

            if (event.getClickedInventory() == null || event.getClickedInventory().equals(player.getInventory()))
                return;

            switch (event.getSlot())
            {
                case 2:
                    atmUsers.get(player).openATMOperationsMenu();
                    break;

                case 6:
                    cardTarget.remove(player.getName());
                    event.getView().close();
                    break;
            }
        }
        else if (event.getView().title().equals(MM.deserialize("<dark_green>ATM | Withdraw")) ||
                event.getView().title().equals(MM.deserialize("<dark_green>ATM | Deposit")))
        {
            event.setCancelled(true);

            double amount = 0;

            if (event.getClickedInventory() == null || event.getClickedInventory().equals(player.getInventory()))
                return;

            double[] values = {10.00, 20.00, 50.00, 100.00, 200.00, 500.00, 1000.00};

            if (event.getSlot() >= 1 && event.getSlot() <= 7)
            {
                amount = values[event.getSlot() - 1];
            }
            else
            {
                if (event.getSlot() == 0)
                {
                    atmUsers.get(player).openATMOperationsMenu();
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

                if (event.getView().title().equals(MM.deserialize("<dark_green>ATM | Withdraw")))
                {
                    try
                    {
                        connection = SylvDBConnect.getConnection();
                        success = new SylvATMOperations(connection).withdraw(player, cardTarget.get(player.getName()),
                                player.getName(), amount);
                        if (success) event.getView().close();
                    }
                    catch (SQLException error)
                    {
                        player.sendMessage(MM.deserialize("<red>An error occurred. Cannot withdraw."));
                    }
                    /*finally
                    {
                        SylvDBConnect.releaseConnection(connection);
                    }*/
                }
                else if (event.getView().title().equals(MM.deserialize("<dark_green>ATM | Deposit")))
                {
                    try
                    {
                        connection = SylvDBConnect.getConnection();
                        success = new SylvATMOperations(connection).deposit(player, cardTarget.get(player.getName()), amount);
                        if (success) event.getView().close();
                    }
                    catch (SQLException error)
                    {
                        player.sendMessage(MM.deserialize("<red>An error occurred. Cannot deposit."));
                    }
                    /*finally {
                        SylvDBConnect.releaseConnection(connection);
                    }*/
                }
            }
        }
    }

    @EventHandler
    public void onMenuClose(InventoryCloseEvent event)
    {
        if (!event.getView().title().equals(MM.deserialize("<dark_green>ATM | Insert Card")))
            return;

        Inventory inventory = event.getInventory();
        ItemStack inputItem = inventory.getItem(4);

        if (inputItem != null && !inputItem.getType().equals(Material.AIR))
            event.getPlayer().getInventory().addItem(inputItem);

        cardTarget.remove(event.getPlayer().getName());
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event)
    {
        atmUsers.remove(event.getPlayer());
    }

    private void itemInputProcess(Inventory inventory, Player player)
    {
        ItemStack inputItem = inventory.getItem(4);

        if (inputItem == null || inputItem.getType().equals(Material.AIR))
            return;

        player.getInventory().addItem(inputItem);
        inventory.setItem(4, null);

        if (inputItem.getType().equals(Material.NAME_TAG) && inputItem.getItemMeta() != null &&
                inputItem.getItemMeta().hasLore())
        {
            String cardID = PlainTextComponentSerializer.plainText()
                    .serialize(Objects.requireNonNull(inputItem.getItemMeta().lore()).get(1));

            String cardDigits = cardID.replace("-", "");

            if (cardDigits.length() == 16 && cardDigits.charAt(0) == '5')
            {
                try
                {
                    connection = SylvDBConnect.getConnection();
                    String getCardOwner = new SylvBankDBTasks(connection).getPlayerNameByCard(cardID);

                    if (getCardOwner == null)
                    {
                        player.sendMessage(MM.deserialize("<red>Invalid card."));
                        return;
                    }
                    atmUsers.get(player).openATMOperationsMenu();
                    cardTarget.put(player.getName(), getCardOwner);
                }
                catch (SQLException error)
                {
                    player.sendMessage(MM.deserialize("<red>An error occurred, ATM operations cannot be performed."));
                }
                /*finally
                {
                    SylvDBConnect.releaseConnection(connection);
                }*/
            }
            else
            {
                player.sendMessage(MM.deserialize("<red>Invalid card details."));
            }
        }
        else
        {
            player.sendMessage(MM.deserialize("<red>Invalid item."));
        }
    }
}