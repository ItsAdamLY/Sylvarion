package com.itsadamly.sylvarion.events.ATM;

import com.itsadamly.sylvarion.Sylvarion;
import com.itsadamly.sylvarion.databases.bank.SylvBankCard;
import com.itsadamly.sylvarion.databases.bank.SylvBankDBTasks;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;

public class SylvATMOperations
{
    private final Connection connection;
    private static final Sylvarion pluginInstance = Sylvarion.getInstance();

    public SylvATMOperations(Connection connection)
    {
        this.connection = connection;
    }

    public void openAccount(CommandSender sender, Player targetPlayer) throws SQLException
    {
        try
        {
            boolean isUserExist = new SylvBankDBTasks().isUserInDB(targetPlayer.getName());

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

            new SylvBankDBTasks().createUser(targetPlayer, cardID);

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
            boolean isUserExist = new SylvBankDBTasks().isUserInDB(targetName);

            if (!isUserExist)
            {
                if (commandSender.getName().equalsIgnoreCase(targetName))
                    commandSender.sendMessage(ChatColor.RED + "You haven't created any account.");

                else
                    commandSender.sendMessage(ChatColor.RED + "This player has not created any account.");

                return;
            }

            new SylvBankDBTasks().deleteUser(targetName);

            if (commandSender.getName().equalsIgnoreCase(targetName))
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
