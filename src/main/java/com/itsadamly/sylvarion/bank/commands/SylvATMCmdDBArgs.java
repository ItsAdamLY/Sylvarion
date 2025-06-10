package com.itsadamly.sylvarion.bank.commands;

import com.itsadamly.sylvarion.Sylvarion;
import com.itsadamly.sylvarion.databases.bank.SylvBankDBTasks;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class SylvATMCmdDBArgs implements CustomArgumentType<String, String> {

    private final Connection connection;
    private final MiniMessage MM = MiniMessage.miniMessage();
    private final Sylvarion instance = Sylvarion.getInstance();

    private SylvATMCmdDBArgs(Connection connection) {
        this.connection = connection;
    }

    private final DynamicCommandExceptionType ERR_PLAYER_NOT_IN_DB = new DynamicCommandExceptionType(
            playerName -> MessageComponentSerializer.message().serialize(
                    MM.deserialize("<red>Player <yellow>" + playerName + "<red> does not have any account."))
    );

    public static SylvATMCmdDBArgs playerInDB(Connection connection) {
        return new SylvATMCmdDBArgs(connection);
    }

    @Override
    public @NotNull String parse(@NotNull StringReader stringReader) throws CommandSyntaxException {
        String playerName;
        try {
            playerName = stringReader.readString();
            boolean playerExistsInDB = new SylvBankDBTasks(connection).isUserInDB(playerName);
            if (!playerExistsInDB) {
                throw ERR_PLAYER_NOT_IN_DB.create(playerName);
            }
        } catch (SQLException e) {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherParseException().create("Database error: " + e.getMessage());
        }
        return playerName;
    }

    @Override
    public @NotNull ArgumentType<String> getNativeType() {
        return StringArgumentType.word();
    }

    @Override
    public @NotNull <S> CompletableFuture<Suggestions> listSuggestions(@NotNull CommandContext<S> context, @NotNull SuggestionsBuilder builder) {
        try {
            List<String> playersInDB = new SylvBankDBTasks(connection).getAllPlayers();
            for (String player : playersInDB) {
                if (player.toLowerCase().startsWith(builder.getRemaining().toLowerCase())) {
                    builder.suggest(player);
                }
            }
            return builder.buildFuture();
        }
        catch (SQLException e) {
            instance.getServer().getLogger().log(Level.WARNING, "Error fetching player names from database: " + e.getMessage());
        }

        return Suggestions.empty();
    }
}
