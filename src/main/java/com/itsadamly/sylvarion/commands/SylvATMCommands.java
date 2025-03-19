package com.itsadamly.sylvarion.commands;

import com.itsadamly.sylvarion.Sylvarion;
import com.itsadamly.sylvarion.databases.SylvDBConnect;
import com.itsadamly.sylvarion.databases.bank.SylvBankCard;
import com.itsadamly.sylvarion.databases.bank.SylvBankDBTasks;
import com.itsadamly.sylvarion.events.ATM.SylvATMOperations;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class SylvATMCommands implements CommandExecutor
{
    /* This Class only handles /atm commands */

    List<String> perms = allPerms();
    List<String> commandList = commandArgs();
    private static final Sylvarion pluginInstance = Sylvarion.getInstance();
    private final Connection connection = SylvDBConnect.getSQLConnection();
    private final Economy economy = Sylvarion.getEconomy();

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args)
    {

        if (!commandSender.hasPermission(perms.get(0)))
        {
            commandSender.sendMessage(ChatColor.RED + "You do not have the following permission:");
            commandSender.sendMessage(ChatColor.GOLD + " " + perms.get(0));
            return true;
        }

        if (args.length == 0)
        {
            commandSender.sendMessage(ChatColor.GOLD + "Available command arguments:");

            for (String commandName : commandList)
                commandSender.sendMessage(ChatColor.GOLD + "/atm " + commandName);

            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) // /atm reload
        {
            pluginInstance.reloadConfig();
            commandSender.sendMessage(ChatColor.GREEN + "Configuration has been reloaded.");
        }

        else if (args[0].equalsIgnoreCase("open")) // /atm open
        {
            Player player = null;

            if (args.length == 1)
            {
                // console
                if (!(commandSender instanceof Player))
                {
                    commandSender.sendMessage(ChatColor.RED + "You are a console. Shut up.");
                    return true;
                }

                player = (Player) commandSender;
            }

            if (args.length > 1)
            {
                try
                {
                    player = Bukkit.getPlayerExact(args[1]);
                }
                catch (NullPointerException error)
                {
                    commandSender.sendMessage(ChatColor.RED + "Player not found. Is the player online?");
                    return true;
                }
            }

            try
            {
                new SylvATMOperations(connection).openAccount(commandSender, player);
            }
            catch (SQLException error)
            {
                commandSender.sendMessage(ChatColor.RED + "Cannot create user. Check console for details.");
                pluginInstance.getServer().getLogger().log(Level.WARNING, error.getMessage());
            }

            return true;
        }

        else if (args[0].equalsIgnoreCase("close")) // /atm close
        {
            String username = new SylvATMOperations(connection).getUsername(commandSender, args);
            if (username == null) return true; // Console

            new SylvATMOperations(connection).closeAccount(commandSender, args[1]);
            return true;
        }

        else if (args[0].equalsIgnoreCase("getCard")) // /atm getCard
        {
            if (!(commandSender instanceof Player))
            {
                commandSender.sendMessage(ChatColor.RED + "You are a console. Shut up.");
                return true;
            }

            try (connection)
            {
                String username = new SylvATMOperations(connection).getUsername(commandSender, args);

                boolean isUserExist = new SylvBankDBTasks().isUserInDB(username);

                if (!isUserExist)
                {
                    if (username.equalsIgnoreCase(commandSender.getName()))
                        commandSender.sendMessage(ChatColor.RED + "You don't have an account. Create an account first");

                    else
                        commandSender.sendMessage(ChatColor.RED + "This player does not have any account.");

                    return true;
                }

                String cardID = new SylvBankDBTasks().getCardID(username);
                ItemStack card = new SylvBankCard().createCard(username, cardID);

                Player player = (Player) commandSender;

                player.getInventory().addItem(card);

                if (username.equalsIgnoreCase(commandSender.getName()))
                    player.sendMessage(ChatColor.GREEN + "You have reobtained your card.");

                else
                    player.sendMessage(ChatColor.GREEN + "You retrieved " + username + "'s card.");
            }
            catch (SQLException error)
            {
                commandSender.sendMessage(ChatColor.RED + "An error occurred. Check console for details.");
                pluginInstance.getServer().getLogger().log(Level.WARNING, error.getMessage());
            }
        }

        else if (args[0].equalsIgnoreCase("checkBalance")) // /atm checkBalance (name)
        {
            String username = new SylvATMOperations(connection).getUsername(commandSender, args);
            if (username == null) return true; // Console

            try (connection)
            {
                boolean isUserExist = new SylvBankDBTasks().isUserInDB(username);

                if (!isUserExist)
                {
                    if (commandSender.getName().equalsIgnoreCase(username))
                        commandSender.sendMessage(ChatColor.RED + "You don't have any account.");

                    else
                        commandSender.sendMessage(ChatColor.RED + "This player does not have any account.");

                    return true;
                }

                double balance = new SylvBankDBTasks().getCardBalance(username);
                commandSender.sendMessage(ChatColor.GREEN + "Balance: " + balance);
            }
            catch (SQLException error)
            {
                commandSender.sendMessage(ChatColor.RED + "An error occured. Check console for details.");
                pluginInstance.getServer().getLogger().log(Level.WARNING, error.getMessage());
            }
        }

        // /atm updateBalance
        else if (args[0].equalsIgnoreCase("updateBalance") ||
                args[0].equalsIgnoreCase("updatemoney"))
        {
            if (args.length < 4)
            {
                commandSender.sendMessage(ChatColor.GOLD + "Syntax:");
                commandSender.sendMessage(ChatColor.GOLD + "/atm updateMoney (name) (add/subtract/set) (amount)");

                return true;
            }

            try (connection)
            {
                // to be checked
                boolean isUserExist = new SylvBankDBTasks().isUserInDB(args[1]);

                if (!isUserExist)
                {
                    if (args[1].equalsIgnoreCase(commandSender.getName()))
                        commandSender.sendMessage(ChatColor.RED + "You don't have any account.");

                    else
                        commandSender.sendMessage(ChatColor.RED + "This player does not have any account.");

                    return true;
                }

                switch (args[2].toLowerCase())
                {
                    case "add":
                        if (Double.parseDouble(args[3]) < 0)
                        {
                            commandSender.sendMessage(ChatColor.RED + "Amount must be non-negative.");
                            return true;
                        }

                        new SylvBankDBTasks().setCardBalance(args[1], "add", Double.parseDouble(args[3]));
                        commandSender.sendMessage(ChatColor.GREEN + "Balance has been updated.");
                        break;

                    case "subtract":
                        if (Double.parseDouble(args[3]) < 0)
                        {
                            commandSender.sendMessage(ChatColor.RED + "Amount must be non-negative.");
                            return true;
                        }

                        new SylvBankDBTasks().setCardBalance(args[1], "subtract", Double.parseDouble(args[3]));
                        commandSender.sendMessage(ChatColor.GREEN + "Balance has been updated.");
                        break;

                    case "set":
                        new SylvBankDBTasks().setCardBalance(args[1], "set", Double.parseDouble(args[3]));
                        commandSender.sendMessage(ChatColor.GREEN + "Balance has been updated.");
                        break;

                    default:
                        commandSender.sendMessage(ChatColor.RED + "Invalid operation. Syntax:");
                        commandSender.sendMessage(ChatColor.GOLD + "/atm updateBalance (name) (add/subtract/set) (amount)");
                        break;
                }
            }
            catch (SQLException error)
            {
                commandSender.sendMessage(ChatColor.RED + "An error occurred. Check console for details.");
                pluginInstance.getServer().getLogger().log(Level.WARNING, error.getMessage());
            }
        }

        else if (args[0].equalsIgnoreCase("updateBalanceAll") ||
                args[0].equalsIgnoreCase("updateAll") ||
                args[0].equalsIgnoreCase("updateMoneyAll"))
        {
            if (args.length < 3)
            {
                commandSender.sendMessage(ChatColor.GOLD + "Syntax:");
                commandSender.sendMessage(ChatColor.GOLD + "/atm updateBalanceAll (add/subtract/set) (amount)");
                return true;
            }

            try (connection)
            {
                switch (args[1].toLowerCase())
                {
                    case "add":
                        if (Double.parseDouble(args[2]) < 0)
                        {
                            commandSender.sendMessage(ChatColor.RED + "Amount must be non-negative.");
                            return true;
                        }

                        new SylvBankDBTasks().setCardBalance("add",
                                Double.parseDouble(String.format("%.2f", Double.parseDouble(args[2]))));
                        commandSender.sendMessage(ChatColor.GREEN + "All balances have been updated.");
                        break;

                    case "subtract":
                        if (Double.parseDouble(args[2]) < 0)
                        {
                            commandSender.sendMessage(ChatColor.RED + "Amount must be non-negative.");
                            return true;
                        }

                        new SylvBankDBTasks().setCardBalance("subtract",
                                Double.parseDouble(String.format("%.2f", Double.parseDouble(args[2]))));
                        commandSender.sendMessage(ChatColor.GREEN + "All balances have been updated.");
                        break;

                    case "set":
                        new SylvBankDBTasks().setCardBalance("set",
                                Double.parseDouble(String.format("%.2f", Double.parseDouble(args[2]))));
                        commandSender.sendMessage(ChatColor.GREEN + "All balances have been updated.");
                        break;

                    default:
                        commandSender.sendMessage(ChatColor.RED + "Invalid operation. Syntax:");
                        commandSender.sendMessage(ChatColor.GOLD + "/atm updateBalanceAll (add/subtract/set) (amount)");
                        break;
                }
            }
            catch (SQLException error)
            {
                commandSender.sendMessage(ChatColor.RED + "An error occurred. Check console for details.");
                pluginInstance.getServer().getLogger().log(Level.WARNING, error.getMessage());
            }
        }

        else if (args[0].equalsIgnoreCase("deposit"))
        {
            if (args.length == 1)
            {
                commandSender.sendMessage(ChatColor.GOLD + "Syntax:");
                commandSender.sendMessage(ChatColor.GOLD + "/atm deposit <targetname> (amount)");
                return true;
            }

            String username = new SylvATMOperations(connection).getUsername(commandSender, args);
            if (username == null) return true; // Console

            try (connection)
            {
                if (args.length == 2)
                    new SylvATMOperations(connection).deposit(commandSender, username,
                            Double.parseDouble(args[1]));

                else new SylvATMOperations(connection).deposit(commandSender, username,
                        Double.parseDouble(args[2]));
            }
            catch (SQLException error)
            {
                commandSender.sendMessage(ChatColor.RED + "An error occurred. Check console for details.");
                pluginInstance.getServer().getLogger().log(Level.WARNING, error.getMessage());
            }
        }

        else
        {
            commandSender.sendMessage(ChatColor.RED + "Invalid command. Use /atm to view available commands.");
            return true;
        }

        return true;
    }

    private List<String> allPerms()
    {
        perms = new ArrayList<>();
        perms.add("bankcommand");
        return perms;
    }

    protected List<String> commandArgs()
    {
        commandList = new ArrayList<>();
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