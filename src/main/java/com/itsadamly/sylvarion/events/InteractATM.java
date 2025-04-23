package com.itsadamly.sylvarion.events;

import com.itsadamly.sylvarion.Sylvarion;
import com.itsadamly.sylvarion.databases.SylvDBConnect;
import com.itsadamly.sylvarion.databases.SylvDBDetails;
import com.itsadamly.sylvarion.databases.bank.SylvBankDBTasks;
import com.itsadamly.sylvarion.events.ATM.SylvATMGUIOpener;
import com.itsadamly.sylvarion.events.ATM.SylvATMOperations;
import com.itsadamly.sylvarion.events.ATM.SylvATMSigns;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
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
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

public class InteractATM extends SylvATMSigns {
    private static final Sylvarion pluginInstance = Sylvarion.getInstance();
    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final String CURRENCY = SylvDBDetails.getCurrencySymbol();
    private final HashMap<String, String> cardTarget = new HashMap<>();
    private static Connection connection = null;

    static {
        try {
            connection = SylvDBConnect.getConnection();
        } catch (SQLException e) {
            pluginInstance.getLogger().log(Level.WARNING, "An error occurred, cannot connect to database.");
        }
    }

    @EventHandler
    public void onInteractATM(PlayerInteractEvent event) {
        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && !event.getPlayer().isSneaking()) {
            if (Objects.requireNonNull(event.getClickedBlock()).getState() instanceof Sign &&
                    ((Sign) event.getClickedBlock().getState()).getSide(Side.FRONT).line(0).toString()
                            .equalsIgnoreCase("[ATM]")) {
                event.setCancelled(true);
                new SylvATMGUIOpener("<dark_green>ATM", event.getPlayer()).openATM();
            }
        }
    }

    @EventHandler
    public void menuEvent(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().title().toString();

        if (title.equals(MM.deserialize("<dark_green>ATM").toString())) {
            if (event.getClickedInventory() == null || event.getClickedInventory().equals(player.getInventory())) {
                event.setCancelled(true);
                return;
            }

            switch (event.getSlot()) {
                case 1:
                    try {
                        new SylvATMOperations(connection).openAccount(event.getWhoClicked(), (Player) event.getWhoClicked());
                        event.getView().close();
                    } catch (SQLException error) {
                        event.getWhoClicked().sendMessage(MM.deserialize("<red>An error occurred, ATM operations cannot be performed."));
                    }
                    break;
                case 4:
                    new SylvATMGUIOpener("<dark_green>ATM | Insert Card", player).openInputCardMenu();
                    break;
                case 5:
                    new SylvATMGUIOpener("<dark_green>ATM | Test", player).openTestMenu();
                    break;
                case 7:
                    try {
                        new SylvATMOperations(connection).closeAccount(event.getWhoClicked(), event.getWhoClicked().getName());
                        event.getView().close();
                    } catch (SQLException error) {
                        event.getWhoClicked().sendMessage(MM.deserialize("<red>An error occurred, ATM operations cannot be performed."));
                    } finally {
                        SylvDBConnect.releaseConnection(connection);
                    }
                    break;
            }
            event.setCancelled(true);
        }
        else if (!player.isSneaking() && title.equals(MM.deserialize("<dark_green>ATM | Test").toString())) {
            event.setCancelled(true);
            if (event.getSlot() == 4) {
                player.setHealth(0);
                player.showTitle(Title.title(
                                MM.deserialize("<red>so apparently you died"),
                                MM.deserialize("you activated a death trap"),
                                Title.Times.times(Duration.ofSeconds(1), Duration.ofMillis(5), Duration.ofMillis(1))
                        )
                );
            } else {
                new SylvATMGUIOpener("<dark_green>ATM", player).openATM();
            }
        }
        else if (title.equals(MM.deserialize("<dark_green>ATM | Insert Card").toString())) {
            if (event.getClickedInventory() == null) return;
            if (event.getSlot() != 4 && !event.getClickedInventory().equals(event.getWhoClicked().getInventory())) {
                event.setCancelled(true);
                return;
            }
            Bukkit.getScheduler().runTaskLater(pluginInstance, () ->
                    itemInputProcess(event.getInventory(), (Player) event.getWhoClicked()), 1);
        }
        else if (title.equals(MM.deserialize("<dark_green>ATM | Operations").toString())) {
            event.setCancelled(true);
            if (event.getClickedInventory() == null || event.getClickedInventory().equals(player.getInventory())) {
                return;
            }
            switch (event.getSlot()) {
                case 0:
                    cardTarget.remove(player.getName());
                    new SylvATMGUIOpener("<dark_green>ATM", player).openATM();
                    break;
                case 2:
                    new SylvATMGUIOpener("<dark_green>ATM | Withdraw", player).openValuesMenu();
                    break;
                case 3:
                    try {
                        double balance = new SylvBankDBTasks(connection).getCardBalance(cardTarget.get(player.getName()));
                        Component titleComp = MM.deserialize("<dark_green>ATM | Balance: <green>" +
                                CURRENCY + " " + String.format("%.2f", balance) + "<dark_green>. Continue?");
                        new SylvATMGUIOpener(titleComp, player).openConfirmMenu();
                    } catch (SQLException error) {
                        player.sendMessage(MM.deserialize("<red>An error occurred. Cannot fetch balance."));
                    } finally {
                        SylvDBConnect.releaseConnection(connection);
                    }
                    break;
                case 5:
                    new SylvATMGUIOpener("<dark_green>ATM | Deposit", player).openValuesMenu();
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
        else if (title.contains("ATM | Balance:")) {
            event.setCancelled(true);
            if (event.getClickedInventory() == null || event.getClickedInventory().equals(player.getInventory())) {
                return;
            }
            switch (event.getSlot()) {
                case 2:
                    new SylvATMGUIOpener("<dark_green>ATM | Operations", player).openATMOperationsMenu();
                    break;
                case 6:
                    cardTarget.remove(player.getName());
                    event.getView().close();
                    break;
            }
        }
        else if (title.equals(MM.deserialize("<dark_green>ATM | Withdraw").toString()) ||
                title.equals(MM.deserialize("<dark_green>ATM | Deposit").toString())) {
            event.setCancelled(true);
            double amount = 0;
            if (event.getClickedInventory() == null || event.getClickedInventory().equals(player.getInventory())) {
                return;
            }
            double[] values = {10.00, 20.00, 50.00, 100.00, 200.00, 500.00, 1000.00};
            if (event.getSlot() >= 1 && event.getSlot() <= 7) {
                amount = values[event.getSlot() - 1];
            } else {
                if (event.getSlot() == 0) {
                    new SylvATMGUIOpener("<dark_green>ATM | Operations", player).openATMOperationsMenu();
                    return;
                } else if (event.getSlot() == 8) {
                    event.getView().close();
                    return;
                }
            }
            if (event.getSlot() != 0 && event.getSlot() != 8) {
                boolean success;
                if (title.equals(MM.deserialize("<dark_green>ATM | Withdraw").toString())) {
                    try {
                        success = new SylvATMOperations(connection).withdraw(player, cardTarget.get(player.getName()), amount);
                        if (success) event.getView().close();
                    } catch (SQLException error) {
                        player.sendMessage(MM.deserialize("<red>An error occurred. Cannot withdraw."));
                    } finally {
                        SylvDBConnect.releaseConnection(connection);
                    }
                } else if (title.equals(MM.deserialize("<dark_green>ATM | Deposit").toString())) {
                    try {
                        success = new SylvATMOperations(connection).deposit(player, cardTarget.get(player.getName()), amount);
                        if (success) event.getView().close();
                    } catch (SQLException error) {
                        player.sendMessage(MM.deserialize("<red>An error occurred. Cannot deposit."));
                    } finally {
                        SylvDBConnect.releaseConnection(connection);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onMenuClose(InventoryCloseEvent event) {
        if (!event.getView().title().toString().equals(MM.deserialize("<dark_green>ATM | Insert Card").toString()))
            return;
        Inventory inventory = event.getInventory();
        ItemStack inputItem = inventory.getItem(4);
        if (inputItem != null && !inputItem.getType().equals(Material.AIR))
            event.getPlayer().getInventory().addItem(inputItem);
        cardTarget.remove(event.getPlayer().getName());
    }

    public void itemInputProcess(Inventory inventory, Player player) {
        ItemStack inputItem = inventory.getItem(4);
        if (inputItem == null || inputItem.getType().equals(Material.AIR))
            return;
        player.getInventory().addItem(inputItem);
        inventory.setItem(4, null);
        if (inputItem.getType().equals(Material.NAME_TAG) &&
                inputItem.getItemMeta() != null &&
                inputItem.getItemMeta().hasLore()) {
            String cardID = Objects.requireNonNull(inputItem.getItemMeta().lore()).get(0).toString().replaceAll("§.", "");
            List<Integer> cardDigits = new ArrayList<>();
            for (char s : cardID.toCharArray()) {
                if (Character.isDigit(s)) {
                    cardDigits.add(Character.getNumericValue(s));
                }
            }
            if (cardDigits.size() == 16 && cardDigits.get(0) == 5) {
                cardDigits.clear();
                try {
                    String getCardOwner = new SylvBankDBTasks(connection).getPlayerNameByCard(cardID);
                    if (getCardOwner == null) {
                        player.sendMessage(MM.deserialize("<red>Invalid card."));
                        return;
                    }
                    new SylvATMGUIOpener("<dark_green>ATM | Operations", player).openATMOperationsMenu();
                    cardTarget.put(player.getName(), getCardOwner);
                } catch (SQLException error) {
                    player.sendMessage(MM.deserialize("<red>An error occurred, ATM operations cannot be performed."));
                } finally {
                    SylvDBConnect.releaseConnection(connection);
                }
            } else {
                player.sendMessage(MM.deserialize("<red>Invalid card details."));
            }
        } else {
            player.sendMessage(MM.deserialize("<red>Invalid item."));
        }
    }
}