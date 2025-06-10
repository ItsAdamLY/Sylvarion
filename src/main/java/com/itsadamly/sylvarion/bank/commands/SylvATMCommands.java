package com.itsadamly.sylvarion.bank.commands;

import com.itsadamly.sylvarion.Sylvarion;
import com.itsadamly.sylvarion.bank.events.SylvATMOperations;
import com.itsadamly.sylvarion.databases.SylvDBConnect;
import com.itsadamly.sylvarion.databases.SylvDBDetails;
import com.itsadamly.sylvarion.databases.bank.SylvBankCard;
import com.itsadamly.sylvarion.databases.bank.SylvBankDBTasks;
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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;

public class SylvATMCommands {
    //TODO: Between-Account Transfers

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

    private static final RequiredArgumentBuilder<CommandSourceStack, String>
        updateBalanceSubcommand = Commands.argument("players", SylvATMCmdDBArgs.playerInDB(connection))
            .then(Commands.literal("set")
                .then(Commands.argument("amount", DoubleArgumentType.doubleArg())
                    .executes(ctx -> handleUpdateBalance(ctx, "set"))))
            .then(Commands.literal("add")
                .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0))
                    .executes(ctx -> handleUpdateBalance(ctx, "add"))))
            .then(Commands.literal("remove")
                .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0))
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
            .executes(SylvATMCommands::handleCloseAccount) // case if the sender is a player
            .then(Commands.argument("player", SylvATMCmdDBArgs.playerInDB(connection)) //case if the sender is a console @ there is a target
                .requires(src -> src.getSender().hasPermission("sylv.atm.admin"))
                .executes(SylvATMCommands::handleCloseAccount)))

        .then(Commands.literal("getcard")
            .requires(src -> (src.getSender() instanceof Player))
            .executes(SylvATMCommands::handleGetCard)
            .then(Commands.argument("player", SylvATMCmdDBArgs.playerInDB(connection))
                .requires(src -> src.getSender().hasPermission("sylv.atm.admin"))
                .executes(SylvATMCommands::handleGetCard)))

        .then(Commands.literal("checkbalance")
            .executes(SylvATMCommands::handleCheckBalance)
            .then(Commands.argument("player", SylvATMCmdDBArgs.playerInDB(connection))
                .requires(src -> src.getSender().hasPermission("sylv.atm.admin"))
                .executes(SylvATMCommands::handleCheckBalance)))

        .then(Commands.literal("updatebalance")
            .requires(src -> src.getSender().hasPermission("sylv.atm.admin"))
            .then(updateBalanceSubcommand))

        .then(Commands.literal("deposit")
                .requires(src -> (src.getSender().hasPermission("sylv.atm.admin")))
            .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0))
                .requires(src -> (src.getSender() instanceof Player))
                .executes(ctx -> handleDepositWithdraw(ctx, "deposit")))
            .then(Commands.argument("player", SylvATMCmdDBArgs.playerInDB(connection))
                .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0))
                    .executes(ctx -> handleDepositWithdraw(ctx, "deposit")))))

        .then(Commands.literal("withdraw")
                .requires(src -> (src.getSender().hasPermission("sylv.atm.admin")))
            .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0))
                .requires(src -> (src.getSender() instanceof Player))
                .executes(ctx -> handleDepositWithdraw(ctx, "withdraw")))
            .then(Commands.argument("player", SylvATMCmdDBArgs.playerInDB(connection))
                .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0))
                    .executes(ctx -> handleDepositWithdraw(ctx, "withdraw")))))
        .build();

    private static int handleReload(CommandContext<CommandSourceStack> ctx) {
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

    private static int handleCloseAccount(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSender sender = ctx.getSource().getSender();
        String playerName;
        try {
            try {
                playerName = ctx.getArgument("player", String.class); // check for argument
            } catch (IllegalArgumentException e) {
                // if the argument is not provided and is a console
                if (!(sender instanceof Player)) {
                    sender.sendMessage(MM.deserialize("<red>Please provide a player's name."));
                    return 0;
                }
                // if the sender is a player and no argument is provided, use the sender's name instead
                if (!(new SylvBankDBTasks(connection).isUserInDB(sender.getName()))) {
                    sender.sendMessage(MM.deserialize("<red>You do not have an account."));
                    return 0;
                }
                playerName = sender.getName();
            }
            new SylvATMOperations(connection).closeAccount(sender, playerName);
            return Command.SINGLE_SUCCESS;
        } catch (SQLException error) {
            sender.sendMessage(MM.deserialize("<red>Cannot create user. Check console for details."));
            pluginInstance.getLogger().log(Level.WARNING, error.getMessage());
            for (StackTraceElement element : error.getStackTrace())
                pluginInstance.getLogger().log(Level.WARNING, element.toString());
            return 0;
        }
    }

    private static int handleGetCard(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSender sender = ctx.getSource().getSender();
        String target;
        try {
            try {
                target = ctx.getArgument("player", String.class); // check for argument
            } catch (IllegalArgumentException e) {
                // if the argument is not provided and is a console
                if (!(sender instanceof Player)) {
                    sender.sendMessage(MM.deserialize("<red>Please provide a player's name."));
                    return 0;
                }
                // if the sender is a player and no argument is provided, use the sender's name instead
                if (!(new SylvBankDBTasks(connection).isUserInDB(sender.getName()))) {
                    sender.sendMessage(MM.deserialize("<red>You do not have an account."));
                    return 0;
                }

                target = sender.getName();
            }

            String cardID = new SylvBankDBTasks(connection).getCardID(target);
            ItemStack card = new SylvBankCard().createCard(target, cardID);

            Player player = (Player) sender;
            player.getInventory().addItem(card);

            if (target.equalsIgnoreCase(sender.getName())) {
                sender.sendMessage(MM.deserialize("<green>You have reobtained your card."));
            } else {
                sender.sendMessage(MM.deserialize("<green>You retrieved " + target + "'s card."));
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

    private static int handleCheckBalance(CommandContext<CommandSourceStack> ctx) throws IllegalArgumentException
    {
        CommandSender sender = ctx.getSource().getSender();
        String playerName;

        try {
            try {
                playerName = ctx.getArgument("player", String.class); // check for argument
            } catch (IllegalArgumentException e) {
                // if the argument is not provided and is a console
                if (!(sender instanceof Player)) {
                    sender.sendMessage(MM.deserialize("<red>Please provide a player's name."));
                    return 0;
                }
                // if the sender is a player and no argument is provided, use the sender's name instead
                    if (!(new SylvBankDBTasks(connection).isUserInDB(sender.getName()))) {
                        sender.sendMessage(MM.deserialize("<red>You do not have an account."));
                        return 0;
                    }

                playerName = sender.getName();
            }

            long startTime = System.nanoTime();
            double balance = new SylvBankDBTasks(connection).getCardBalance(playerName);
            sender.sendMessage(MM.deserialize(
                "<yellow>Balance: <green>" + CURRENCY + " " + String.format("%.2f", balance)));
            long endTime = System.nanoTime();
            sender.sendMessage(Component.text(startTime + " " + endTime));
            sender.sendMessage(MM.deserialize(
                "<yellow>Time taken: " + (endTime - startTime) / 1000000 + "ms"));
            return Command.SINGLE_SUCCESS;
        } catch (SQLException error) {
            sender.sendMessage(MM.deserialize("<red>Cannot check balance. Check console for details."));
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
        String playerName;
        //List<Player> targets = ctx.getArgument("player", List.class);
        double amount = DoubleArgumentType.getDouble(ctx, "amount");
        try {
          /* for (Player target: targets) {
                boolean isUserExist = new SylvBankDBTasks(connection).isUserInDB(target.getName());

                if (!isUserExist) {
                    sender.sendMessage(MM.deserialize(String.format("<red>Player %s does not have any account", target.getName())));
                    continue;
                }

                new SylvBankDBTasks(connection).setCardBalance(target.getName(), type, amount);
            }
            sender.sendMessage(MM.deserialize(String.format("<green>Successfully updated %g players' records.", targets.size())));*/

            try {
                playerName = ctx.getArgument("players", String.class);
            } catch (IllegalArgumentException e) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(MM.deserialize("<red>Please provide a player's name."));
                    return 0;
                }
                if (!(new SylvBankDBTasks(connection).isUserInDB(sender.getName()))) {
                    sender.sendMessage(MM.deserialize("<red>You do not have an account."));
                    return 0;
                }
                playerName = sender.getName();
            }
            new SylvBankDBTasks(connection).setCardBalance(playerName, type, amount);

            if (playerName.equalsIgnoreCase(sender.getName()))
                sender.sendMessage(MM.deserialize("<green>Successfully updated your balance to <yellow>"
                        + CURRENCY + " " + String.format("%.2f", amount)));
            else
                sender.sendMessage(MM.deserialize("<green>Successfully updated <yellow>" + playerName +
                        "<green>'s balance to <yellow>" + CURRENCY + " " + String.format("%.2f", amount)));
            return Command.SINGLE_SUCCESS;
        } catch (SQLException error) {
            sender.sendMessage(MM.deserialize("<red>Cannot create user. Check console for details."));
            pluginInstance.getLogger().log(Level.WARNING, error.getMessage());
            for (StackTraceElement element : error.getStackTrace())
                pluginInstance.getLogger().log(Level.WARNING, element.toString());
            return 0;
        }
    }

    private static int handleDepositWithdraw(CommandContext<CommandSourceStack> ctx, String operation) throws CommandSyntaxException {
        //TODO: third member withdrawl/deposit (i.e.: withdraw from another player to a completely different player and vice versa)

        CommandSender sender = ctx.getSource().getSender();
        String playerName;
        try {
            try {
                playerName = ctx.getArgument("player", String.class); // check for argument
            } catch (IllegalArgumentException e) {
                // if the argument is not provided and is a console
                if (!(sender instanceof Player)) {
                    sender.sendMessage(MM.deserialize("<red>Please provide a player's name."));
                    return 0;
                }
                // if the sender is a player and no argument is provided, use the sender's name instead
                if (!(new SylvBankDBTasks(connection).isUserInDB(sender.getName()))) {
                    sender.sendMessage(MM.deserialize("<red>You do not have an account."));
                    return 0;
                }
                playerName = sender.getName();
            }

            double amount = DoubleArgumentType.getDouble(ctx, "amount");

            if (operation.equalsIgnoreCase("deposit")) {
                new SylvATMOperations(connection).deposit(sender, playerName, amount);
            } else if (operation.equalsIgnoreCase("withdraw")) {
                new SylvATMOperations(connection).withdraw(sender, playerName, sender.getName(), amount);
            } else {
                throw new IllegalArgumentException("Operation should be either 'deposit' or 'withdraw'.");
            }
            return Command.SINGLE_SUCCESS;

        } catch (SQLException error) {
            if (operation.equalsIgnoreCase("deposit")) {
                sender.sendMessage(MM.deserialize("<red>Cannot perform deposit. Check console for details."));
            } else if (operation.equalsIgnoreCase("withdraw")) {
                sender.sendMessage(MM.deserialize("<red>Cannot perform withdrawal. Check console for details."));
            }
            pluginInstance.getLogger().log(Level.WARNING, error.getMessage());
            for (StackTraceElement element : error.getStackTrace())
                pluginInstance.getLogger().log(Level.WARNING, element.toString());
            return 0; 
        }
    }
}