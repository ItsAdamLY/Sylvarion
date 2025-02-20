package com.itsadamly.sylvarion.commands;

import com.itsadamly.sylvarion.Sylvarion;
import com.itsadamly.sylvarion.databases.SylvDBConnect;
import com.itsadamly.sylvarion.databases.bank.BankCard;
import com.itsadamly.sylvarion.databases.bank.SylvBankDBTasks;
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
    List<String> perms = allPerms();
    List<String> commandList = commandArgs();
    private static final Sylvarion pluginInstance = Sylvarion.getInstance();
    private final Connection connection = SylvDBConnect.getSQLConnection();

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args)
    {
        if (command.getName().equalsIgnoreCase("atm"))
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
                try (connection)
                {
                    boolean isUserExist;
                    
                    if (args.length == 1)
                    {
                        if (!(commandSender instanceof Player))
                        {
                            commandSender.sendMessage(ChatColor.RED + "Only players are allowed to run this command.");
                            return true;
                        }

                        Player player = (Player) commandSender;
                        isUserExist = new SylvBankDBTasks().isUserInDB(player.getName());
                    }
                    
                    else
                        isUserExist = new SylvBankDBTasks().isUserInDB
                                (Bukkit.getPlayerExact(args[1]).getName());

                    if (isUserExist)
                    {
                        commandSender.sendMessage(ChatColor.RED + "This person already has an account opened.");
                        return true;
                    }
                }
                catch (SQLException error)
                {
                    commandSender.sendMessage(ChatColor.RED + "An error occurred. Check console for details.");
                    pluginInstance.getServer().getLogger().log(Level.WARNING, error.getMessage());
                }
                catch (NullPointerException error)
                {
                    commandSender.sendMessage(ChatColor.RED + "Player is not online.");
                    return true;
                }

                Player player;

                if (args.length == 1)
                {
                    assert commandSender instanceof Player;
                    player = (Player) commandSender;
                }

                else
                    player = Bukkit.getPlayerExact(args[1]);

                try (connection)
                {
                    String cardID = new BankCard().cardID();
                    assert player != null;
                    ItemStack card = new BankCard().createCard(player, cardID);

                    new SylvBankDBTasks().createUser(player, cardID);

                    if (args.length == 1)
                        player.sendMessage(ChatColor.GREEN + "Your account has been opened.");

                    else
                    {
                        commandSender.sendMessage(ChatColor.GREEN + "Account for this player has been opened.");
                        player.sendMessage(ChatColor.GREEN + "Your account has been opened by " + commandSender.getName() + '.');
                    }

                    player.getInventory().addItem(card);
                }
                catch (SQLException error)
                {
                    player.sendMessage(ChatColor.RED + "Cannot create user. Check console for details.");
                    pluginInstance.getServer().getLogger().log(Level.WARNING, error.getMessage());
                }
            }

            else if (args[0].equalsIgnoreCase("close")) // /atm close
            {
                String username = getUsername(commandSender, args);
                if (username == null) return true; // Console

                try (connection)
                {
                    boolean isUserExist = new SylvBankDBTasks().isUserInDB(args[1]);

                    if (!isUserExist)
                    {
                        if (commandSender.getName().equalsIgnoreCase(username))
                            commandSender.sendMessage(ChatColor.RED + "You haven't created any account.");

                        else
                            commandSender.sendMessage(ChatColor.RED + "This player has not created any account.");

                        return true;
                    }

                    new SylvBankDBTasks().deleteUser(username);

                    if (commandSender.getName().equalsIgnoreCase(username))
                        commandSender.sendMessage(ChatColor.GREEN + "Your account has successfully been deleted.");

                    else
                        commandSender.sendMessage(ChatColor.GREEN + "Player's account has successfully been deleted.");
                }
                catch (SQLException error)
                {
                    commandSender.sendMessage(ChatColor.RED + "Cannot delete user. Check console for details.");
                    pluginInstance.getServer().getLogger().log(Level.WARNING, error.getMessage());
                }
            }

            else if (args[0].equalsIgnoreCase("getCard")) // /atm getCard
            {
                if (args.length > 1)
                {
                    commandSender.sendMessage(ChatColor.GOLD + "Function to get other players' cards to be implemented later.");
                    return true;
                }

                if (!(commandSender instanceof Player))
                {
                    commandSender.sendMessage(ChatColor.RED + "Only players are allowed to run this command.");
                    return true;
                }

                Player player = (Player) commandSender;

                try (connection)
                {
                    boolean isUserExist = new SylvBankDBTasks().isUserInDB(player.getName());

                    if (!isUserExist)
                    {
                        commandSender.sendMessage(ChatColor.RED + "You don't have an account. Create an account first");
                        return true;
                    }

                    String cardID = new SylvBankDBTasks().getCardID(player.getName());
                    ItemStack card = new BankCard().createCard(player, cardID);

                    player.getInventory().setItemInMainHand(card);
                    player.sendMessage(ChatColor.GREEN + "You have reobtained your card.");
                }
                catch (SQLException error)
                {
                    player.sendMessage(ChatColor.RED + "An error occurred. Check console for details.");
                    pluginInstance.getServer().getLogger().log(Level.WARNING, error.getMessage());
                }
            }

            else if (args[0].equalsIgnoreCase("checkBalance")) // /atm checkBalance (name)
            {
                String username = getUsername(commandSender, args);
                if (username == null) return true; // Console

                try (connection)
                {
                    boolean isUserExist = new SylvBankDBTasks().isUserInDB(username);

                    if (!isUserExist)
                    {
                        if (commandSender.getName().equalsIgnoreCase(username))
                            commandSender.sendMessage(ChatColor.RED + "You haven't created any account.");

                        else
                            commandSender.sendMessage(ChatColor.RED + "This player has not created any account.");
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
            else if (args[0].equalsIgnoreCase("updateBalance") || args[0].equalsIgnoreCase("updatemoney"))
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
                            commandSender.sendMessage(ChatColor.RED + "You haven't created any account.");

                        else
                            commandSender.sendMessage(ChatColor.RED + "This player has not created any account.");
                        
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

            else if (args[0].equalsIgnoreCase("updateBalanceAll") || args[0].equalsIgnoreCase("updateAll")
            || args[0].equalsIgnoreCase("updateMoneyAll"))
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

                            new SylvBankDBTasks().setCardBalance("add", Double.parseDouble(args[2]));
                            commandSender.sendMessage(ChatColor.GREEN + "All balances have been updated.");
                            break;

                        case "subtract":
                            if (Double.parseDouble(args[2]) < 0)
                            {
                                commandSender.sendMessage(ChatColor.RED + "Amount must be non-negative.");
                                return true;
                            }

                            new SylvBankDBTasks().setCardBalance("subtract", Double.parseDouble(args[2]));
                            commandSender.sendMessage(ChatColor.GREEN + "All balances have been updated.");
                            break;

                        case "set":
                            new SylvBankDBTasks().setCardBalance("set", Double.parseDouble(args[2]));
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

            else
            {
                commandSender.sendMessage(ChatColor.RED + "Invalid command. Use /atm to view available commands.");
                return true;
            }
        }

        return true;
    }

    private List<String> allPerms()
    {
        perms = new ArrayList<>();
        perms.add("bankcommand");
        return perms;
    }

    private String getUsername(CommandSender commandSender, String[] args)
    {
        String username = null;

        switch (args.length)
        {
            case 1:
                if (!(commandSender instanceof Player))
                {
                    commandSender.sendMessage(ChatColor.RED + "Only players are allowed to run this command.");
                    return null;
                }

                Player player = (Player) commandSender;
                username = player.getName();
                break;

            case 2:
                username = args[1];
                break;
        }

        return username;
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