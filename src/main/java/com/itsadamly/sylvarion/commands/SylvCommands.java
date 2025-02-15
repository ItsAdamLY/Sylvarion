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
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class SylvCommands implements CommandExecutor, TabExecutor
{
    List<String> perms = allPerms();
    List<String> commandList = commandArgs();
    private static final Sylvarion pluginInstance = Sylvarion.getInstance();
    private final Connection connection = SylvDBConnect.getSQLConnection();

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args)
    {
        //commandList.sort(String.CASE_INSENSITIVE_ORDER);

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
                {
                    commandSender.sendMessage(ChatColor.GOLD + "/atm " + commandName);
                }

                return true;
            }

            if (args[0].equalsIgnoreCase(commandList.get(4))) // /atm reload
            {
                pluginInstance.reloadConfig();
            }
            else if (args[0].equalsIgnoreCase(commandList.get(0))) // /atm open
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
                        isUserExist = new SylvBankDBTasks().isUserInDB(player.getUniqueId().toString());
                    }
                    
                    else
                        isUserExist = new SylvBankDBTasks().isUserInDB
                                (Bukkit.getPlayerExact(args[1]).getUniqueId().toString());

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
                    commandSender.sendMessage(ChatColor.RED + "Player not found.");
                }

                Player player;

                if (args.length == 1) {
                    assert commandSender instanceof Player;
                    player = (Player) commandSender;
                }

                else
                    player = Bukkit.getPlayerExact(args[1]);

                try (connection)
                {
                    String cardID = new BankCard().cardID();
                    ItemStack card = new BankCard().createCard(player, cardID);

                    new SylvBankDBTasks().createUser(player, cardID);
                    player.sendMessage(ChatColor.GREEN + "Account for this user has been opened.");

                    player.getInventory().addItem(card);
                }
                catch (SQLException error)
                {
                    player.sendMessage(ChatColor.RED + "Cannot create user. Check console for details.");
                    pluginInstance.getServer().getLogger().log(Level.WARNING, error.getMessage());
                }
            }

            else if (args[0].equalsIgnoreCase(commandList.get(1))) // /atm close
            {
                if (args.length > 1)
                {
                    commandSender.sendMessage(ChatColor.GOLD + "Function to remove other players to be implemented later.");
                    return true;
                }

                if (!(commandSender instanceof Player))
                {
                    commandSender.sendMessage(ChatColor.RED + "Only players are allowed to run this command.");
                    return true;
                }

                Player player = (Player) commandSender;

                try
                {
                    boolean isUserExist = new SylvBankDBTasks().isUserInDB(player.getUniqueId().toString());

                    if (!isUserExist)
                    {
                        commandSender.sendMessage(ChatColor.RED + "You don't have an account.");
                        return true;
                    }
                }
                catch (SQLException error)
                {
                    commandSender.sendMessage(ChatColor.RED + "An error occurred. Check console for details.");
                    pluginInstance.getServer().getLogger().log(Level.WARNING, error.getMessage());
                }

                try (connection)
                {
                    new SylvBankDBTasks().deleteUser(player);
                    player.sendMessage(ChatColor.GREEN + "User has successfully been deleted.");
                }
                catch (SQLException error)
                {
                    player.sendMessage(ChatColor.RED + "Cannot delete user. Check console for details.");
                    pluginInstance.getServer().getLogger().log(Level.WARNING, error.getMessage());
                }
            }

            else if (args[0].equalsIgnoreCase(commandList.get(5))) // /atm getCard
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
                    boolean isUserExist = new SylvBankDBTasks().isUserInDB(player.getUniqueId().toString());

                    if (!isUserExist)
                    {
                        commandSender.sendMessage(ChatColor.RED + "You don't have an account. Create an account first");
                        return true;
                    }
                }
                catch (SQLException error)
                {
                    commandSender.sendMessage(ChatColor.RED + "An error occurred. Check console for details.");
                    pluginInstance.getServer().getLogger().log(Level.WARNING, error.getMessage());
                }

                try (connection)
                {
                    String cardID = new SylvBankDBTasks().getCardID(player.getUniqueId().toString());
                    ItemStack card = new BankCard().createCard(player, cardID);

                    player.getInventory().setItemInMainHand(card);
                    player.sendMessage(ChatColor.GREEN + "You have reobtained your card.");
                }
                catch (SQLException error)
                {
                    player.sendMessage(ChatColor.RED + "Cannot obtain card details. Check console for details.");
                    pluginInstance.getServer().getLogger().log(Level.WARNING, error.getMessage());
                }
            }

            else if (args[0].equalsIgnoreCase(commandList.get(2))) // /atm checkBalance (name)
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
                    boolean isUserExist = new SylvBankDBTasks().isUserInDB(player.getUniqueId().toString());

                    if (!isUserExist)
                    {
                        commandSender.sendMessage(ChatColor.RED + "User hasn't created any account.");
                        return true;
                    }
                }
                catch (SQLException error)
                {
                    commandSender.sendMessage(ChatColor.RED + "An error occurred. Check console for details.");
                    pluginInstance.getServer().getLogger().log(Level.WARNING, error.getMessage());
                }

                try (connection)
                {
                    double balance = new SylvBankDBTasks().getCardBalance(player.getUniqueId().toString());
                    commandSender.sendMessage(ChatColor.GREEN + "Balance: " + balance);
                }
                catch (SQLException error)
                {
                    commandSender.sendMessage(ChatColor.RED + "Cannot fetch balance. Check console for details.");
                    pluginInstance.getServer().getLogger().log(Level.WARNING, error.getMessage());
                }
            }

            else if (args[0].equalsIgnoreCase(commandList.get(3))) // /atm updateBalance
            {
                if (args.length < 4)
                {
                    commandSender.sendMessage(ChatColor.GOLD + "Syntax:");
                    commandSender.sendMessage(ChatColor.GOLD + "/atm updateMoney (name) (add/subtract/set) (amount)");

                    return true;
                }

                try (connection)
                {
                    // to be modified so it will include offline players (if exist)
                    Player player = Bukkit.getPlayerExact(args[1]);

                    // to be checked
                    boolean isUserExist = new SylvBankDBTasks().isUserInDB(player.getUniqueId().toString());

                    if (!isUserExist)
                    {
                        commandSender.sendMessage(ChatColor.RED + "User hasn't created any account.");
                        return true;
                    }

                    switch (args[2].toLowerCase())
                    {
                        case "add":
                            new SylvBankDBTasks().setCardBalance(player.getUniqueId().toString(), "add", Double.parseDouble(args[3]));
                            commandSender.sendMessage(ChatColor.GREEN + "Balance has been updated.");
                            break;

                        case "subtract":
                            new SylvBankDBTasks().setCardBalance(player.getUniqueId().toString(), "subtract", Double.parseDouble(args[3]));
                            commandSender.sendMessage(ChatColor.GREEN + "Balance has been updated.");
                            break;

                        case "set":
                            new SylvBankDBTasks().setCardBalance(player.getUniqueId().toString(), "set", Double.parseDouble(args[3]));
                            commandSender.sendMessage(ChatColor.GREEN + "Balance has been updated.");
                            break;

                        default:
                            commandSender.sendMessage(ChatColor.RED + "Invalid operation. Use add/subtract/set.");
                            break;
                    }
                }
                catch (SQLException error)
                {
                    commandSender.sendMessage(ChatColor.RED + "An error occurred. Check console for details.");
                    pluginInstance.getServer().getLogger().log(Level.WARNING, error.getMessage());
                }
                catch (NullPointerException error)
                {
                    commandSender.sendMessage(ChatColor.RED + "Player not found.");
                }
            }
        }

        return true;
    }

    @Override
    public List<String> onTabComplete (CommandSender commandSender, Command command, String s, String[] args)
    {
        List<String> toReturn = new ArrayList<>();
        if (command.getName().equalsIgnoreCase("atm")) {
            if (args.length == 1) 
            {
                toReturn.addAll(commandArgs()); 
            }
            else if (args.length == 3)
            {
                switch (args[0].toLowerCase())
                {
                    case "updatebalance":
                        toReturn.add("add");
                        toReturn.add("subtract");
                        toReturn.add("set");
                        break; 
                }
            }
        }

        return toReturn;
    }

    private List<String> allPerms()
    {
        perms = new ArrayList<>();
        perms.add("bankcommand");
        return perms;
    }

    private List<String> commandArgs()
    {
        commandList = new ArrayList<>();
        commandList.add("open");
        commandList.add("close");
        commandList.add("checkBalance");
        commandList.add("updateBalance");
        commandList.add("reload");
        commandList.add("getCard");

        return commandList;
    }
}