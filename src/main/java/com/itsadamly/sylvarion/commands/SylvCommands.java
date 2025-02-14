package com.itsadamly.sylvarion.commands;

import com.itsadamly.sylvarion.Sylvarion;
import com.itsadamly.sylvarion.databases.bank.BankCard;
import com.itsadamly.sylvarion.databases.bank.SylvBankDBTasks;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class SylvCommands implements CommandExecutor
{
    List<String> perms = allPerms();
    List<String> commandList = commandArgs();
    private static final Sylvarion pluginInstance = Sylvarion.getInstance();

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

            if (args[0].equalsIgnoreCase(commandList.get(3))) // /atm reload
            {
                pluginInstance.getConfig().options().copyDefaults();
            }

            else if (args[0].equalsIgnoreCase(commandList.get(0))) // /atm open
            {
                if (args.length > 1)
                {
                    commandSender.sendMessage(ChatColor.GOLD + "Function to add other players to be implemented later.");
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

                    if (isUserExist)
                    {
                        commandSender.sendMessage(ChatColor.RED + "You already have an account opened.");
                        return true;
                    }
                }

                catch (SQLException error)
                {
                    commandSender.sendMessage(ChatColor.RED + "An error occured. Check console for details.");
                    pluginInstance.getServer().getLogger().log(Level.WARNING, error.getMessage());
                }

                try
                {
                    String cardID = new BankCard().cardID();
                    ItemStack card = new BankCard().createCard(player, cardID);

                    new SylvBankDBTasks().createUser(player, cardID);
                    player.sendMessage(ChatColor.GREEN + "User has successfully been created.");

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
                    commandSender.sendMessage(ChatColor.RED + "An error occured. Check console for details.");
                    pluginInstance.getServer().getLogger().log(Level.WARNING, error.getMessage());
                }

                try
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

            else if (args[0].equalsIgnoreCase(commandList.get(4))) // /atm getCard
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

                try
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
                    commandSender.sendMessage(ChatColor.RED + "An error occured. Check console for details.");
                    pluginInstance.getServer().getLogger().log(Level.WARNING, error.getMessage());
                }

                try
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
        }

        return true;
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
        commandList.add("updatemoney");
        commandList.add("reload");
        commandList.add("getCard");

        return commandList;
    }
}