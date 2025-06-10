package com.itsadamly.sylvarion.bank.events;

import com.itsadamly.sylvarion.Sylvarion;
import com.itsadamly.sylvarion.databases.SylvDBDetails;
import com.itsadamly.sylvarion.databases.bank.SylvBankCard;
import com.itsadamly.sylvarion.databases.bank.SylvBankDBTasks;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;
import java.util.logging.Level;

public class SylvATMOperations {
    private static final Sylvarion pluginInstance = Sylvarion.getInstance();
    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final String CURRENCY = SylvDBDetails.getCurrencySymbol();
    private final Connection connection;

    public SylvATMOperations(Connection sqlConnection) {
        this.connection = sqlConnection;
    }

    /**
     * Inserts an account to SQL (for command use)
     * @param sender Command sender
     * @param targetPlayer Command executor (the one who opens the account)
     * @throws SQLException if SQL error occurs
     */
    public void openAccount(CommandSender sender, Player targetPlayer) throws SQLException {
        try {
            boolean isUserExist = new SylvBankDBTasks(connection).isUserInDB(targetPlayer.getName());

            if (isUserExist) {
                if (sender.getName().equalsIgnoreCase(targetPlayer.getName())) {
                    sender.sendMessage(MM.deserialize("<red>You already have an account."));
                } else {
                    sender.sendMessage(MM.deserialize("<red>This player already has an account."));
                }
                return;
            }

            String cardID = new SylvBankCard().cardID();
            ItemStack card = new SylvBankCard().createCard(targetPlayer.getName(), cardID);
            new SylvBankDBTasks(connection).createUser(targetPlayer, cardID);

            if (targetPlayer.getName().equalsIgnoreCase(sender.getName())) {
                targetPlayer.sendMessage(MM.deserialize("<green>Your account has been opened."));
            } else {
                sender.sendMessage(MM.deserialize("<green>Account for this player has been opened."));
                targetPlayer.sendMessage(MM.deserialize("<green>Your bank account has been opened by <gold>" + sender.getName() + "</gold>."));
            }

            targetPlayer.getInventory().addItem(card);
        } catch (NullPointerException error) {
            sender.sendMessage(MM.deserialize("<red>Player not found."));
        }
    }

    /**
     * Removes account entry in SQL (for command use)
     * @param commandSender - Command sender
     * @param targetName - Command executor
     * @throws SQLException if SQL error orrurs
     */
    public void closeAccount(CommandSender commandSender, String targetName) throws SQLException {
        try {
            boolean isUserExist = new SylvBankDBTasks(connection).isUserInDB(targetName);

            if (!isUserExist) {
                if (commandSender.getName().equalsIgnoreCase(targetName)) {
                    commandSender.sendMessage(MM.deserialize("<red>You don't have any account."));
                } else {
                    commandSender.sendMessage(MM.deserialize("<red>This player does not have any account."));
                }
                return;
            }

            new SylvBankDBTasks(connection).deleteUser(targetName);

            if (commandSender.getName().equalsIgnoreCase(targetName)) {
                commandSender.sendMessage(MM.deserialize("<green>Your account has successfully been deleted."));
            } else {
                commandSender.sendMessage(MM.deserialize("<green>Player's account has successfully been deleted."));
                if (Bukkit.getPlayerExact(targetName) != null) {
                    Objects.requireNonNull(Bukkit.getPlayerExact(targetName))
                            .sendMessage(MM.deserialize("<gold>Your bank account has been closed by <green>" + commandSender.getName() + "</green>."));
                }
            }
        } catch (SQLException error) {
            commandSender.sendMessage(MM.deserialize("<red>Cannot delete user. Check console for details."));
            pluginInstance.getLogger().log(Level.WARNING, error.getMessage());
        }
    }

    /**
     * Deposits amount of money to specified account (for command use)
     * @param commandSender {@link CommandSender} - Command Sender
     * @param targetName {@link String} - Command executor in string
     * @param amount double - Amount to deposit
     * @return boolean - success or not
     * @throws SQLException if SQL error occurs
     */
    public boolean deposit(CommandSender commandSender, String targetName, double amount) throws SQLException {
        try {
            boolean isUserExist = new SylvBankDBTasks(connection).isUserInDB(targetName);

            if (!isUserExist) {
                if (targetName.equalsIgnoreCase(commandSender.getName())) {
                    commandSender.sendMessage(MM.deserialize("<red>You don't have any account."));
                } else {
                    commandSender.sendMessage(MM.deserialize("<red>This player does not have any account."));
                }
                return false;
            }

            Economy economy = Sylvarion.getEconomy();
            if (economy.getBalance(Bukkit.getOfflinePlayer(commandSender.getName())) < amount) {
                commandSender.sendMessage(MM.deserialize("<red>Insufficient money."));
                return false;
            }

            economy.withdrawPlayer(Bukkit.getOfflinePlayer(targetName), amount);
            new SylvBankDBTasks(connection).setCardBalance(targetName, "add", amount);

            Component message = MM.deserialize("<green>You have successfully deposited <gold>" +
                    CURRENCY + " " + String.format("%.2f", amount) + "</gold> into ");

            if (commandSender.getName().equalsIgnoreCase(targetName)) {
                commandSender.sendMessage(message.append(MM.deserialize("your account.")));
            } else {
                commandSender.sendMessage(message.append(MM.deserialize("<green>" + targetName + "</green>'s account.")));
            }

            return true;
        } catch (SQLException error) {
            commandSender.sendMessage(MM.deserialize("<red>Cannot deposit into user's account. Check console for details."));
            pluginInstance.getLogger().log(Level.WARNING, error.getMessage());
        }
        return false;
    }

    /**
     * Withdraw (remove) money from specified account (for command use)
     * @param commandSender - Command sender
     * @param fromTargetName - Command executor
     * @param toTargetName - Target to send the money to
     * @param amount - amount of money to withdraw
     * @return boolean - success or not
     * @throws SQLException if SQL error occurs
     */
    public boolean withdraw(CommandSender commandSender, String fromTargetName, String toTargetName, double amount) throws SQLException {
        try {
            boolean isUserExist = new SylvBankDBTasks(connection).isUserInDB(fromTargetName);

            if (!isUserExist) {
                if (fromTargetName.equalsIgnoreCase(commandSender.getName())) {
                    commandSender.sendMessage(MM.deserialize("<red>You don't have any account."));
                } else {
                    commandSender.sendMessage(MM.deserialize("<red>This player does not have any account."));
                }
                return false;
            }

            Economy economy = Sylvarion.getEconomy();
            double balance = new SylvBankDBTasks(connection).getCardBalance(fromTargetName);

            if (balance < amount) {
                commandSender.sendMessage(MM.deserialize("<red>Insufficient money in the account."));
                return false;
            }

            economy.depositPlayer(Bukkit.getOfflinePlayer(toTargetName), amount);
            new SylvBankDBTasks(connection).setCardBalance(fromTargetName, "subtract", amount);

            Component message = MM.deserialize("<green>You have successfully withdrawn <gold>" +
                    CURRENCY + " " + String.format("%.2f", amount) + "</gold> from ");

            if (commandSender.getName().equalsIgnoreCase(fromTargetName)) {
                commandSender.sendMessage(message.append(MM.deserialize("your account.")));
            } else {
                commandSender.sendMessage(message.append(MM.deserialize("<green>" + fromTargetName + "</green>'s account.")));
            }

            return true;
        } catch (SQLException error) {
            commandSender.sendMessage(MM.deserialize("<red>Cannot withdraw from user's account. Check console for details."));
            pluginInstance.getLogger().log(Level.WARNING, error.getMessage());
        }
        return false;
    }
//
//    /**
//     * Gets username of player refered in command
//     * @param commandSender - sender instance
//     * @param args - <code>onCommand</code> command args
//     * @return {@link String} - the username
//     * @throws NullPointerException idk, go ask @ItsAdamLY
//     */
//    public String getUsername(CommandSender commandSender, String[] args) throws NullPointerException {
//        String username = null;
//
//        switch (args.length) {
//            case 1:
//                if (!(commandSender instanceof Player)) {
//                    commandSender.sendMessage(MM.deserialize("<red>Only players are allowed to run this command."));
//                    return null;
//                }
//                username = commandSender.getName();
//                break;
//            case 2:
//                username = args[1];
//                break;
//        }
//        return username;
//    }
}