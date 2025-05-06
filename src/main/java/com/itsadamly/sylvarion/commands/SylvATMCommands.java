package com.itsadamly.sylvarion.commands;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.itsadamly.sylvarion.Sylvarion;
import com.itsadamly.sylvarion.databases.SylvDBConnect;
import com.itsadamly.sylvarion.databases.SylvDBDetails;
import com.itsadamly.sylvarion.databases.bank.SylvBankCard;
import com.itsadamly.sylvarion.databases.bank.SylvBankDBTasks;
import com.itsadamly.sylvarion.events.ATM.SylvATMOperations;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;

// TODO: Someone help
// it randomly throws [23:44:35 WARN]: [Sylvarion] No operations allowed after connection closed.
// after running for a while

public class SylvATMCommands {
    // This Class only handles /atm commands
    private static final MiniMessage MM = MiniMessage.miniMessage();

    private static final Sylvarion pluginInstance = Sylvarion.getInstance();
    private static final String CURRENCY = SylvDBDetails.getCurrencySymbol();
    private static Connection connection = null;

    static {
        try {
            connection = SylvDBConnect.getConnection();
        } catch (SQLException error) {
            pluginInstance.getLogger().log(Level.WARNING,
                    "An error occurred, cannot connect to database. Check console for details.");
            pluginInstance.getLogger().log(Level.WARNING, error.getMessage());
            for (StackTraceElement element : error.getStackTrace())
                pluginInstance.getLogger().log(Level.WARNING, element.toString());
        }
    }

    private static RequiredArgumentBuilder<CommandSourceStack,PlayerSelectorArgumentResolver>
        updateBalanceSubcommand = Commands.argument("players", ArgumentTypes.players())
            .then(Commands.literal("set")
                .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0))
                    .executes(ctx -> handleUpdateBalance(ctx, "set"))))
            .then(Commands.literal("add")
                .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0))
                    .executes(ctx -> handleUpdateBalance(ctx, "add"))))
            .then(Commands.literal("remove")
                .then(Commands.argument("amount", DoubleArgumentType.doubleArg())
                    .executes(ctx -> handleUpdateBalance(ctx, "remove"))));

    public static LiteralCommandNode<CommandSourceStack> command = Commands.literal("atm")
        .requires(src -> src.getSender().hasPermission("sylv.atm"))

        .then(Commands.literal("reload").executes(SylvATMCommands::handleReload))

        .then(Commands.literal("open")
            .executes(ctx -> handleOpenAccount(ctx.getSource().getSender(), ctx.getSource().getExecutor()))
            .then(Commands.argument("player", ArgumentTypes.player())
                .requires(src -> src.getSender().hasPermission("sylv.atm.admin"))
                .executes(ctx -> handleOpenAccount(
                    ctx.getSource().getSender(), 
                    ctx.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).get(0)
                ))))

        .then(Commands.literal("close")
            .executes(SylvATMCommands::handleCloseAccount)
            .then(Commands.argument("player", ArgumentTypes.player())
                .requires(src -> src.getSender().hasPermission("sylv.atm.admin"))
                .executes(SylvATMCommands::handleCloseAccount)))

        .then(Commands.literal("getcard")
            .requires(src -> (src.getSender() instanceof Player))
            .executes(SylvATMCommands::handleGetCard)
            .then(Commands.argument("player", ArgumentTypes.player())
                .requires(src -> src.getSender().hasPermission("sylv.atm.admin"))
                .executes(SylvATMCommands::handleGetCard)))

        .then(Commands.literal("checkbalance")
            .executes(SylvATMCommands::handleCheckBalance)
            .then(Commands.argument("player", ArgumentTypes.player())
                .requires(src -> src.getSender().hasPermission("sylv.atm.admin"))
                .executes(SylvATMCommands::handleCheckBalance)))

        .then(Commands.literal("updatebalance")
            .requires(src -> src.getSender().hasPermission("sylv.atm.admin"))
            .then(updateBalanceSubcommand))

        .then(Commands.literal("deposit")
            .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0))
                .requires(src -> (src.getSender() instanceof Player))
                .executes(SylvATMCommands::handleDeposit))
            .then(Commands.argument("player", ArgumentTypes.player())
                .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0))
                    .executes(SylvATMCommands::handleDeposit))))

        .then(Commands.literal("withdraw")
            .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0))
                .requires(src -> (src.getSender() instanceof Player))
                .executes(SylvATMCommands::handleWithdraw))
            .then(Commands.argument("player", ArgumentTypes.player())
                .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0))
                    .executes(SylvATMCommands::handleWithdraw))))
        .build(); 

    private static int handleReload (CommandContext<CommandSourceStack> ctx) {
        pluginInstance.reloadConfig();
        ctx.getSource().getSender().sendMessage(MM.deserialize("<green>Configuration has been reloaded."));
        return Command.SINGLE_SUCCESS; 
    }

    private static int handleNewConnection (CommandContext<CommandSourceStack> ctx) {
        // TODO debug purposes
        return 0; 
    }

    private static int handleOpenAccount (CommandSender sender, Entity executor) {
        //TODO reformat
        if (!(executor instanceof Player)) {
            sender.sendMessage(MM.deserialize("<red>You are a console. Shut up."));
            return 0;
        }
        Player player = (Player) executor; 
        try {
            new SylvATMOperations(connection).openAccount(sender, player);
            return Command.SINGLE_SUCCESS; 
        } catch (SQLException error) {
            sender.sendMessage(MM.deserialize("<red>Cannot create user. Check console for details."));
            pluginInstance.getLogger().log(Level.WARNING, error.getMessage());
            for (StackTraceElement element : error.getStackTrace())
                pluginInstance.getLogger().log(Level.WARNING, element.toString());
            return 0; 
        }
    }

    private static int handleCloseAccount (CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSender sender = ctx.getSource().getSender(); 
        Entity executor; 
        try {
            executor = ctx.getArgument("player", PlayerSelectorArgumentResolver.class)
                .resolve(ctx.getSource())
                .get(0); 
        } catch (IllegalArgumentException e) {
            pluginInstance.getLogger().log(Level.INFO, "no arg 'player', using sender by default"); 
            executor = ctx.getSource().getExecutor(); 
        }
        String username = executor.getName(); 
        if (username == null) {
            sender.sendMessage(MM.deserialize("<red>No such user currently exists"));
            return 0;
        }
        try {
            new SylvATMOperations(connection).closeAccount(sender, username);
            return Command.SINGLE_SUCCESS; 
        } catch (SQLException error) {
            sender.sendMessage(MM.deserialize("<red>Cannot create user. Check console for details."));
            pluginInstance.getLogger().log(Level.WARNING, error.getMessage());
            for (StackTraceElement element : error.getStackTrace())
                pluginInstance.getLogger().log(Level.WARNING, element.toString());
            return 0; 
        }
    }

    private static int handleGetCard (CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSender sender = ctx.getSource().getSender(); 
        Entity target; 
        try {
            target = ctx.getArgument("player", PlayerSelectorArgumentResolver.class)
                .resolve(ctx.getSource())
                .get(0); 
        } catch (IllegalArgumentException e) {
            pluginInstance.getLogger().log(Level.INFO, "no arg 'player', using sender by default"); 
            target = ctx.getSource().getExecutor(); 
        }

        try {
            String username = target.getName(); 
            boolean isUserExist = new SylvBankDBTasks(connection).isUserInDB(username);

            if (!isUserExist) {
                if (username.equalsIgnoreCase(sender.getName())) {
                    sender.sendMessage(MM.deserialize("<red>You don't have an account. Create an account first"));
                } else {
                    sender.sendMessage(MM.deserialize("<red>This player does not have any account."));
                }
                return 0;
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
            
            return Command.SINGLE_SUCCESS;
        } catch (SQLException error) {
            sender.sendMessage(MM.deserialize("<red>Cannot create user. Check console for details."));
            pluginInstance.getLogger().log(Level.WARNING, error.getMessage());
            for (StackTraceElement element : error.getStackTrace())
                pluginInstance.getLogger().log(Level.WARNING, element.toString());
            return 0; 
        }
    }

    private static int handleCheckBalance (CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSender sender = ctx.getSource().getSender(); 
        Entity executor; 
        try {
            executor = ctx.getArgument("player", PlayerSelectorArgumentResolver.class)
                .resolve(ctx.getSource())
                .get(0); 
        } catch (IllegalArgumentException e) {
            pluginInstance.getLogger().log(Level.INFO, "no arg 'player', using sender by default"); 
            executor = ctx.getSource().getExecutor(); 
        }

        String username = executor.getName(); 
        if (username == null) {
            sender.sendMessage(MM.deserialize("<red>No such user currently exists"));
            return 0;
        }
        
        try {
            boolean isUserExist = new SylvBankDBTasks(connection).isUserInDB(username);
            if (!isUserExist) {
                if (sender.getName().equalsIgnoreCase(username)) {
                    sender.sendMessage(MM.deserialize("<red>You don't have any account."));
                } else {
                    sender.sendMessage(MM.deserialize("<red>This player does not have any account."));
                }
                return 0;
            }

            long startTime = System.nanoTime();
            double balance = new SylvBankDBTasks(connection).getCardBalance(username);
            sender.sendMessage(MM.deserialize(
                "<yellow>Balance: <green>" + CURRENCY + " " + String.format("%.2f", balance)));
            long endTime = System.nanoTime();
            sender.sendMessage(Component.text(startTime + " " + endTime));
            sender.sendMessage(MM.deserialize(
                "<yellow>Time taken: " + (endTime - startTime) / 1000000 + "ms"));
            return Command.SINGLE_SUCCESS; 
        } catch (SQLException error) {
            sender.sendMessage(MM.deserialize("<red>Cannot create user. Check console for details."));
            pluginInstance.getLogger().log(Level.WARNING, error.getMessage());
            for (StackTraceElement element : error.getStackTrace())
                pluginInstance.getLogger().log(Level.WARNING, element.toString());
            return 0; 
        }

    }

    private static int handleUpdateBalance (CommandContext<CommandSourceStack> ctx, String type) throws CommandSyntaxException {
        if (!(List.of("add", "remove", "set").contains(type))) 
            throw new IllegalArgumentException(String.format("Type should be either \"add\", \"remove\" or \"set\", found %s", type)); 

        CommandSender sender = ctx.getSource().getSender(); 
        List<Player> targets = ctx.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()); 
        double amount = DoubleArgumentType.getDouble(ctx, "amount"); 
        try {
            for (Player target: targets) {
                boolean isUserExist = new SylvBankDBTasks(connection).isUserInDB(target.getName());
                
                if (!isUserExist) {
                    sender.sendMessage(MM.deserialize(String.format("<red>Player %s does not have any account", target.getName())));
                    continue; 
                }
                
                new SylvBankDBTasks(connection).setCardBalance(target.getName(), type, amount);
            }
            sender.sendMessage(MM.deserialize(String.format("<green>Successfully updated %g players' records.", targets.size())));
            return Command.SINGLE_SUCCESS; 
        } catch (SQLException error) {
            sender.sendMessage(MM.deserialize("<red>Cannot create user. Check console for details."));
            pluginInstance.getLogger().log(Level.WARNING, error.getMessage());
            for (StackTraceElement element : error.getStackTrace())
                pluginInstance.getLogger().log(Level.WARNING, element.toString());
            return 0; 
        }
    }

    private static int handleDeposit (CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSender sender = ctx.getSource().getSender(); 
        Entity executor; 
        try {
            executor = ctx.getArgument("player", PlayerSelectorArgumentResolver.class)
                .resolve(ctx.getSource())
                .get(0); 
        } catch (IllegalArgumentException e) {
            executor = ctx.getSource().getExecutor(); 
            if (executor == null) {
                sender.sendMessage(MM.deserialize("<red>Please specify a player, pretty please"));
                return 0;
            }
        }

        String username = executor.getName(); 
        if (username == null) {
            sender.sendMessage(MM.deserialize("<red>No such user currently exists"));
            return 0;
        }

        double amount = DoubleArgumentType.getDouble(ctx, "amount"); 
        try {
            new SylvATMOperations(connection).deposit(sender, username, amount);
            return Command.SINGLE_SUCCESS;
        } catch (SQLException error) {
            sender.sendMessage(MM.deserialize("<red>Cannot create user. Check console for details."));
            pluginInstance.getLogger().log(Level.WARNING, error.getMessage());
            for (StackTraceElement element : error.getStackTrace())
                pluginInstance.getLogger().log(Level.WARNING, element.toString());
            return 0; 
        }
    }

    private static int handleWithdraw (CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSender sender = ctx.getSource().getSender(); 
        Entity executor; 
        try {
            executor = ctx.getArgument("player", PlayerSelectorArgumentResolver.class)
                .resolve(ctx.getSource())
                .get(0); 
        } catch (IllegalArgumentException e) {
            executor = ctx.getSource().getExecutor(); 
            if (executor == null) {
                sender.sendMessage(MM.deserialize("<red>Please specify a player, pretty please"));
                return 0;
            }
        }

        String username = executor.getName(); 
        if (username == null) {
            sender.sendMessage(MM.deserialize("<red>No such user currently exists"));
            return 0;
        }

        double amount = DoubleArgumentType.getDouble(ctx, "amount"); 
        try {
            new SylvATMOperations(connection).withdraw(sender, username, amount);
            return Command.SINGLE_SUCCESS;
        } catch (SQLException error) {
            sender.sendMessage(MM.deserialize("<red>Cannot create user. Check console for details."));
            pluginInstance.getLogger().log(Level.WARNING, error.getMessage());
            for (StackTraceElement element : error.getStackTrace())
                pluginInstance.getLogger().log(Level.WARNING, element.toString());
            return 0; 
        }
    }
}