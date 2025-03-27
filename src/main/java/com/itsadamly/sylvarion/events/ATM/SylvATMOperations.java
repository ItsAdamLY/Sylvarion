package com.itsadamly.sylvarion.events.ATM;

import com.itsadamly.sylvarion.Sylvarion;
import com.itsadamly.sylvarion.databases.SylvDBConnect;
import com.itsadamly.sylvarion.databases.bank.SylvBankCard;
import com.itsadamly.sylvarion.databases.bank.SylvBankDBTasks;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;
import java.util.logging.Level;

public class SylvATMOperations
{
    private static final Sylvarion pluginInstance = Sylvarion.getInstance();
    private final Connection connection;

    public SylvATMOperations(Connection sqlConnection)
    {
        this.connection = sqlConnection;
    }

    public void openAccount(CommandSender sender, Player targetPlayer) throws SQLException
    {
        try (connection)
        {
            boolean isUserExist = new SylvBankDBTasks(connection).isUserInDB(targetPlayer.getName());

            if (isUserExist)
            {
                if (sender.getName().equalsIgnoreCase(targetPlayer.getName())) // if player is opening his/her own account
                    sender.sendMessage(ChatColor.RED + "You already have an account.");

                else
                    sender.sendMessage(ChatColor.RED + "This player already has an account.");

                return;
            }

            String cardID = new SylvBankCard().cardID();
            ItemStack card = new SylvBankCard().createCard(targetPlayer.getName(), cardID);

            new SylvBankDBTasks(connection).createUser(targetPlayer, cardID);

            if (targetPlayer.getName().equalsIgnoreCase(sender.getName()))
                targetPlayer.sendMessage(ChatColor.GREEN + "Your account has been opened.");

            else
            {
                sender.sendMessage(ChatColor.GREEN + "Account for this player has been opened.");
                targetPlayer.sendMessage(ChatColor.GREEN + "Your bank account has been opened by " + sender.getName() + '.');
            }

            targetPlayer.getInventory().addItem(card);

        }
        catch (NullPointerException error)
        {
            sender.sendMessage(ChatColor.RED + "Player not found.");
        }
    }

    public void closeAccount(CommandSender commandSender, String targetName)
    {
        try (connection)
        {
            boolean isUserExist = new SylvBankDBTasks(connection).isUserInDB(targetName);

            if (!isUserExist)
            {
                if (commandSender.getName().equalsIgnoreCase(targetName))
                    commandSender.sendMessage(ChatColor.RED + "You don't have any account.");

                else
                    commandSender.sendMessage(ChatColor.RED + "This player does not have any account.");

                return;
            }

            new SylvBankDBTasks(connection).deleteUser(targetName);

            if (commandSender.getName().equalsIgnoreCase(targetName))
                commandSender.sendMessage(ChatColor.GREEN + "Your account has successfully been deleted.");

            else
            {
                commandSender.sendMessage(ChatColor.GREEN + "Player's account has successfully been deleted.");

                if (Bukkit.getPlayerExact(targetName) != null)
                    Objects.requireNonNull(Bukkit.getPlayerExact(targetName)).sendMessage
                            (ChatColor.GOLD + "Your bank account has been closed by " + commandSender.getName() + '.');
            }

        }
        catch (SQLException error)
        {
            commandSender.sendMessage(ChatColor.RED + "Cannot delete user. Check console for details.");
            pluginInstance.getServer().getLogger().log(Level.WARNING, error.getMessage());
        }
    }
    
    public void deposit(CommandSender commandSender, String targetName, double amount)
    {
        try (connection)
        {
            boolean isUserExist = new SylvBankDBTasks(connection).isUserInDB(targetName);

            if (!isUserExist)
            {
                if (targetName.equalsIgnoreCase(commandSender.getName()))
                    commandSender.sendMessage(ChatColor.RED + "You don't have any account.");

                else
                    commandSender.sendMessage(ChatColor.RED + "This player does not have any account.");

                return;
            }

            Economy economy = Sylvarion.getEconomy();

            if (economy.getBalance(Bukkit.getOfflinePlayer(targetName)) < amount)
            {
                commandSender.sendMessage(ChatColor.RED + "Insufficient money.");
                return;
            }

            economy.withdrawPlayer(Bukkit.getOfflinePlayer(targetName), amount);

            new SylvBankDBTasks(connection).setCardBalance(targetName, "add", amount);

            commandSender.sendMessage(ChatColor.GREEN + "You have successfully deposited " + amount + " into your account.");
        }
        catch (SQLException error)
        {
            commandSender.sendMessage(ChatColor.RED + "Cannot deposit into user's account. Check console for details.");
            pluginInstance.getServer().getLogger().log(Level.WARNING, error.getMessage());
        }
    }

    public String getUsername(CommandSender commandSender, String[] args)
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
}
