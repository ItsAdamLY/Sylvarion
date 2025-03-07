package com.itsadamly.sylvarion.commands.tabcomplete;

import com.itsadamly.sylvarion.commands.SylvATMCommands;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class SylvATMTabComplete extends SylvATMCommands implements TabExecutor
{
    // TAB COMPLETION //
    @Override
    public List<String> onTabComplete (CommandSender commandSender, Command command, String s, String[] args)
    {
        List<String> argsList = new ArrayList<>();
        if (command.getName().equalsIgnoreCase("atm"))
        {
            switch (args.length)
            {
                case 1:
                    argsList.addAll(commandArgs());
                    break;

                case 2:
                    if (!args[0].equalsIgnoreCase(commandArgs().get(4)))
                    {
                        for (Player p: Bukkit.getOnlinePlayers())
                        {
                            argsList.add(p.getName());
                        }
                    }
                    break;

                case 3:
                    if (args[0].equalsIgnoreCase("updateBalance") ||
                        args[0].equalsIgnoreCase("updatemoney"))
                    {
                        argsList.add("add");
                        argsList.add("subtract");
                        argsList.add("set");
                    }
                    break;
            }
        }

        return argsList;
    }
}
