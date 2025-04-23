package com.itsadamly.sylvarion.commands;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.itsadamly.sylvarion.Sylvarion;
import com.itsadamly.sylvarion.databases.SylvDBConnect;
import com.itsadamly.sylvarion.databases.SylvDBDetails;
import com.itsadamly.sylvarion.databases.bank.SylvBankCard;
import com.itsadamly.sylvarion.databases.bank.SylvBankDBTasks;
import com.itsadamly.sylvarion.events.ATM.SylvATMOperations;

public class SylvATMCommands implements CommandExecutor {
    /* This Class only handles /atm commands */
    private static final MiniMessage MM = MiniMessage.miniMessage();

    List<String> perms = allPerms();
    List<String> commandList = commandArgs();
    private static final Sylvarion pluginInstance = Sylvarion.getInstance();
    private static final String CURRENCY = SylvDBDetails.getCurrencySymbol();
    private static Connection connection = null;

    static {
        try {
            connection = SylvDBConnect.getConnection();
        } catch (SQLException e) {
            pluginInstance.getLogger().log(Level.WARNING,
                    "An error occurred, cannot connect to database. Check console for details.");
            pluginInstance.getLogger().log(Level.WARNING, e.getMessage());
        }
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (!commandSender.hasPermission(perms.get(0))) {
            commandSender.sendMessage(MM.deserialize(
                    "<red>You do not have the following permission:\n<gold>" + perms.get(0)));
            return true;
        }

        if (args.length == 0) {
            Component availableCommands = MM.deserialize("<gold>Available command arguments:");
            commandSender.sendMessage(availableCommands);

            for (String commandName : commandList) {
                commandSender.sendMessage(MM.deserialize("<gold>/atm " + commandName));
            }
            return true;
        }

        try {
            switch (args[0].toLowerCase()) {
                case "reload":
                    handleReload(commandSender);
                    break;

                case "open":
                case "create":
                    handleOpenAccount(commandSender, args);
                    break;

                case "close":
                case "delete":
                    handleCloseAccount(commandSender, args);
                    break;

                case "getcard":
                    handleGetCard(commandSender, args);
                    break;

                case "checkbalance":
                    handleCheckBalance(commandSender, args);
                    break;

                case "updatebalance":
                case "updatemoney":
                    handleUpdateBalance(commandSender, args);
                    break;

                case "updatebalanceall":
                case "updateall":
                case "updatemoneyall":
                    handleUpdateAllBalances(commandSender, args);
                    break;

                case "deposit":
                    handleDeposit(commandSender, args);
                    break;

                case "withdraw":
                    handleWithdraw(commandSender, args);
                    break;

                default:
                    commandSender.sendMessage(MM.deserialize(
                            "<red>Invalid command. Use <gold>/atm</gold> to view available commands."));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            SylvDBConnect.releaseConnection(connection);
        }

        return true;
    }

    private void handleReload(CommandSender sender) {
        pluginInstance.reloadConfig();
        sender.sendMessage(MM.deserialize("<green>Configuration has been reloaded."));
    }

    private void handleOpenAccount(CommandSender sender, String[] args) {
        Player player;

        if (args.length == 1) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(MM.deserialize("<red>You are a console. Shut up."));
                return;
            }
            player = (Player) sender;
        } else {
            try {
                player = Bukkit.getPlayerExact(args[1]);
            } catch (NullPointerException error) {
                sender.sendMessage(MM.deserialize("<red>Player not found. Is the player online?"));
                return;
            }
        }

        try {
            new SylvATMOperations(connection).openAccount(sender, player);
        } catch (SQLException error) {
            sender.sendMessage(MM.deserialize("<red>Cannot create user. Check console for details."));
            pluginInstance.getLogger().log(Level.WARNING, error.getMessage());
        }
    }

    private void handleCloseAccount(CommandSender sender, String[] args) throws SQLException {
        String username = new SylvATMOperations(connection).getUsername(sender, args);
        if (username == null) return;

        new SylvATMOperations(connection).closeAccount(sender, username);
    }

    private void handleGetCard(CommandSender sender, String[] args) throws SQLException {
        if (!(sender instanceof Player)) {
            sender.sendMessage(MM.deserialize("<red>You are a console. Shut up."));
            return;
        }

        String username = new SylvATMOperations(connection).getUsername(sender, args);
        boolean isUserExist = new SylvBankDBTasks(connection).isUserInDB(username);

        if (!isUserExist) {
            if (username.equalsIgnoreCase(sender.getName())) {
                sender.sendMessage(MM.deserialize("<red>You don't have an account. Create an account first"));
            } else {
                sender.sendMessage(MM.deserialize("<red>This player does not have any account."));
            }
            return;
        }

        String cardID = new SylvBankDBTasks(connection).getCardID(username);
        ItemStack card = new SylvBankCard().createCard(username, cardID);
        Player player = (Player) sender;
        player.getInventory().addItem(card);

        if (username.equalsIgnoreCase(sender.getName())) {
            player.sendMessage(MM.deserialize("<green>You have reobtained your card."));
        } else {
            player.sendMessage(MM.deserialize("<green>You retrieved " + username + "'s card."));
        }
    }

    private void handleCheckBalance(CommandSender sender, String[] args) throws SQLException {
        String username = new SylvATMOperations(connection).getUsername(sender, args);
        if (username == null) return;

        boolean isUserExist = new SylvBankDBTasks(connection).isUserInDB(username);

        if (!isUserExist) {
            if (sender.getName().equalsIgnoreCase(username)) {
                sender.sendMessage(MM.deserialize("<red>You don't have any account."));
            } else {
                sender.sendMessage(MM.deserialize("<red>This player does not have any account."));
            }
            return;
        }

        long startTime = System.nanoTime();
        double balance = new SylvBankDBTasks(connection).getCardBalance(username);
        sender.sendMessage(MM.deserialize(
                "<yellow>Balance: <green>" + CURRENCY + " " + String.format("%.2f", balance)));
        long endTime = System.nanoTime();
        sender.sendMessage(Component.text(startTime + " " + endTime));
        sender.sendMessage(MM.deserialize(
                "<yellow>Time taken: " + (endTime - startTime) / 1000000 + "ms"));
    }

    private void handleUpdateBalance(CommandSender sender, String[] args) throws SQLException {
        if (args.length < 4) {
            sender.sendMessage(MM.deserialize(
                    "<gold>Syntax:\n<gold>/atm updateMoney (name) (add/subtract/set) (amount)"));
            return;
        }

        boolean isUserExist = new SylvBankDBTasks(connection).isUserInDB(args[1]);

        if (!isUserExist) {
            if (args[1].equalsIgnoreCase(sender.getName())) {
                sender.sendMessage(MM.deserialize("<red>You don't have any account."));
            } else {
                sender.sendMessage(MM.deserialize("<red>This player does not have any account."));
            }
            return;
        }

        switch (args[2].toLowerCase()) {
            case "add":
                if (Double.parseDouble(args[3]) < 0) {
                    sender.sendMessage(MM.deserialize("<red>Amount must be non-negative."));
                    return;
                }
                new SylvBankDBTasks(connection).setCardBalance(args[1], "add", Double.parseDouble(args[3]));
                sender.sendMessage(MM.deserialize("<green>Balance has been updated."));
                break;

            case "subtract":
                if (Double.parseDouble(args[3]) < 0) {
                    sender.sendMessage(MM.deserialize("<red>Amount must be non-negative."));
                    return;
                }
                new SylvBankDBTasks(connection).setCardBalance(args[1], "subtract", Double.parseDouble(args[3]));
                sender.sendMessage(MM.deserialize("<green>Balance has been updated."));
                break;

            case "set":
                new SylvBankDBTasks(connection).setCardBalance(args[1], "set", Double.parseDouble(args[3]));
                sender.sendMessage(MM.deserialize("<green>Balance has been updated."));
                break;

            default:
                sender.sendMessage(MM.deserialize(
                        "<red>Invalid operation. Syntax:\n<gold>/atm updateBalance (name) (add/subtract/set) (amount)"));
                break;
        }
    }

    private void handleUpdateAllBalances(CommandSender sender, String[] args) throws SQLException {
        if (args.length < 3) {
            sender.sendMessage(MM.deserialize(
                    "<gold>Syntax:\n<gold>/atm updateBalanceAll (add/subtract/set) (amount)"));
            return;
        }

        switch (args[1].toLowerCase()) {
            case "add":
                if (Double.parseDouble(args[2]) < 0) {
                    sender.sendMessage(MM.deserialize("<red>Amount must be non-negative."));
                    return;
                }
                new SylvBankDBTasks(connection).setCardBalance("add",
                        Double.parseDouble(String.format("%.2f", Double.valueOf(args[2]))));
                sender.sendMessage(MM.deserialize("<green>All balances have been updated."));
                break;

            case "subtract":
                if (Double.parseDouble(args[2]) < 0) {
                    sender.sendMessage(MM.deserialize("<red>Amount must be non-negative."));
                    return;
                }
                new SylvBankDBTasks(connection).setCardBalance("subtract",
                        Double.parseDouble(String.format("%.2f", Double.valueOf(args[2]))));
                sender.sendMessage(MM.deserialize("<green>All balances have been updated."));
                break;

            case "set":
                new SylvBankDBTasks(connection).setCardBalance("set",
                        Double.parseDouble(String.format("%.2f", Double.valueOf(args[2]))));
                sender.sendMessage(MM.deserialize("<green>All balances have been updated."));
                break;

            default:
                sender.sendMessage(MM.deserialize(
                        "<red>Invalid operation. Syntax:\n<gold>/atm updateBalanceAll (add/subtract/set) (amount)"));
                break;
        }
    }

    private void handleDeposit(CommandSender sender, String[] args) throws SQLException {
        if (args.length == 1) {
            sender.sendMessage(MM.deserialize(
                    "<gold>Syntax:\n<gold>/atm deposit <targetname> (amount)"));
            return;
        }

        String username = new SylvATMOperations(connection).getUsername(sender, args);
        if (username == null) return;

        if (args.length == 2) {
            new SylvATMOperations(connection).deposit(sender, username, Double.parseDouble(args[1]));
        } else {
            new SylvATMOperations(connection).deposit(sender, username, Double.parseDouble(args[2]));
        }
    }

    private void handleWithdraw(CommandSender sender, String[] args) throws SQLException {
        if (args.length == 1) {
            sender.sendMessage(MM.deserialize(
                    "<gold>Syntax:\n<gold>/atm withdraw <targetname> (amount)"));
            return;
        }

        String username = new SylvATMOperations(connection).getUsername(sender, args);
        if (username == null) return;

        if (args.length == 2) {
            new SylvATMOperations(connection).withdraw(sender, username, Double.parseDouble(args[1]));
        } else {
            new SylvATMOperations(connection).withdraw(sender, username, Double.parseDouble(args[2]));
        }
    }

    private List<String> allPerms() {
        List<String> perms = new ArrayList<>();
        perms.add("bankcommand");
        perms.add("banksign");
        return perms;
    }

    protected List<String> commandArgs() {
        List<String> commandList = new ArrayList<>();
        commandList.add("open");
        commandList.add("close");
        commandList.add("checkBalance");
        commandList.add("updateBalance");
        commandList.add("updateBalanceAll");
        commandList.add("reload");
        commandList.add("getCard");
        commandList.add("help");
        commandList.sort(String.CASE_INSENSITIVE_ORDER);
        return commandList;
    }
}