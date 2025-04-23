package com.itsadamly.sylvarion.databases.bank;

import com.itsadamly.sylvarion.Sylvarion;
import com.itsadamly.sylvarion.databases.SylvDBDetails;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SylvBankDBTasks
{
    private final Sylvarion pluginInstance = Sylvarion.getInstance();
    private final Connection connectionSQL;

    public SylvBankDBTasks(Connection connection)
    {
        this.connectionSQL = connection;
    }

    public void createTables() throws SQLException
    {
        PreparedStatement userTableStmt = connectionSQL.prepareStatement(
                "CREATE TABLE IF NOT EXISTS " + SylvDBDetails.getDBUserTableName() + "(" +
                        "ID INT NOT NULL AUTO_INCREMENT," +
                        "Name VARCHAR(100)," +
                        "UUID VARCHAR(100)," +
                        "CardID VARCHAR(20)," +
                        "Balance DECIMAL(10, 2)," +
                        "PRIMARY KEY (ID)" +
                ")"
        );
        userTableStmt.executeUpdate();

/*        PreparedStatement terminalTableStmt = connectionSQL.prepareStatement(
            "CREATE TABLE IF NOT EXISTS " + SylvDBDetails.getDBTerminalTableName() + "(" + 
                "ID INT NOT NULL AUTO_INCREMENT," + 
                "CoordX INT NOT NULL," + 
                "CoordY INT NOT NULL," + 
                "CoordZ INT NOT NULL," + 
                "IsPasswordProtected BOOLEAN NOT NULL DEFAULT FALSE, " + 
                "Password VARCHAR(32)," + 
                "StoredBalance DECIMAL(10, 2) NOT NULL DEFAULT 0.0," + 
                "PRIMARY KEY (ID)" + 
            ")"
        ); 
        terminalTableStmt.executeUpdate();*/
    }

    public void createProcedures() throws SQLException
    {
        PreparedStatement payUserStmt = connectionSQL.prepareStatement(
            "CREATE PROCEDURE IF NOT EXISTS PayPlayer (" + 
                "IN SrcPlayerUUID VARCHAR(100), " + 
                "IN TargetPlayerUUID VARCHAR(100)," + 
                "IN Amount DECIMAL(10, 2)," + 
            ") BEGIN " + 
                "UPDATE " + SylvDBDetails.getDBUserTableName() +  
                    "SET Balance = Balance + Amount" + 
                    "WHERE UUID = TargetPlayerUUID;" + 
                "UPDATE " + SylvDBDetails.getDBUserTableName() + 
                    "SET Balance = Balance - Amount" + 
                    "WHERE UUID = SrcPlayerUUID; " + 
            "END; "
        ); 
        payUserStmt.executeUpdate(); 

        PreparedStatement payTerminalStatement = connectionSQL.prepareStatement(
            "CREATE PROCEDURE IF NOT EXISTS PayTerminal (" + 
                "IN SrcPlayerUUID VARCHAR(100), " + 
                "IN TargetCoordX INT," +
                "IN TargetCoordY INT," +
                "IN TargetCoordZ INT," + 
                "IN Amount DECIMAL(10, 2)," + 
            ") BEGIN " + 
                "UPDATE " + SylvDBDetails.getDBTerminalTableName() +  
                    "SET StoredBalance = StoredBalance + Amount" + 
                    "WHERE CoordX = TargetCoordX AND" +
                        "CoordY = TargetCoordY AND" + 
                        "CoordZ = TargetCoordZ; " +
                "UPDATE " + SylvDBDetails.getDBUserTableName() + 
                    "SET Balance = Balance - Amount" + 
                    "WHERE UUID = SrcPlayerUUID; " + 
            "END; "
        ); 
        payTerminalStatement.executeUpdate(); 
        
        PreparedStatement transferTerminalBalStatement = connectionSQL.prepareStatement(
            "CREATE PROCEDURE IF NOT EXISTS TransferTerminalBal (" + 
                "IN SrcCoordX INT,  " + 
                "IN SrcCoordY INT,  " + 
                "IN SrcCoordZ INT,  " + 
                "IN TargetPlayerUUID VARCHAR(100)  " + 
            ") BEGIN   " + 
                "UPDATE " + SylvDBDetails.getDBUserTableName() + 
                "set `Balance` = `Balance` + (" + 
                    "SELECT StoredBalance from " + SylvDBDetails.getDBTerminalTableName() + 
                    "WHERE `CoordX` = SrcCoordX AND" + 
                        "`CoordY` = SrcCoordY AND" + 
                        "`CoordZ` = SrcCoordZ" + 
                    "); " + 
                "UPDATE " + SylvDBDetails.getDBTerminalTableName() + 
                "SET StoredBalance = 0  " + 
                    "WHERE CoordX = SrcCoordX AND " + 
                    "CoordY = SrcCoordY AND  " + 
                    "CoordZ = SrcCoordZ;" + 
            "END;"
        ); 
        transferTerminalBalStatement.executeUpdate(); 
    }

    public void createUser(Player player, String cardCode) throws SQLException
    {
        PreparedStatement stmt = connectionSQL.prepareStatement(
            "INSERT INTO " + SylvDBDetails.getDBUserTableName() + " (Name, UUID, CardID, Balance) " +
                "VALUES (?, ?, ?, ?)"
        );

        // replace ? with following args by index
        stmt.setString(1, player.getName());
        stmt.setString(2, player.getUniqueId().toString());
        stmt.setString(3, cardCode);
        stmt.setDouble(4, 0.00);
        stmt.executeUpdate();
    }

    public void deleteUser(String playerName) throws SQLException
    {
        PreparedStatement stmt = connectionSQL.prepareStatement(
            "DELETE FROM " + SylvDBDetails.getDBUserTableName() + " WHERE Name = ?"
        );
        stmt.setString(1, playerName);
        stmt.executeUpdate();
    }

    public boolean isUserInDB(String playerName) throws SQLException
    {
        PreparedStatement stmt = connectionSQL.prepareStatement(
            "SELECT UUID FROM " + SylvDBDetails.getDBUserTableName() + " WHERE Name = ?"
        );

        stmt.setString(1, playerName);
        ResultSet result = stmt.executeQuery();

        return result.next();
    }

    public List<String> getAllPlayers() throws SQLException
    {
        PreparedStatement stmt = connectionSQL.prepareStatement(
            "SELECT Name FROM " + SylvDBDetails.getDBUserTableName()
        );

        ResultSet result = stmt.executeQuery();
        List<String> players = new ArrayList<>();

        while (result.next()) // loop for each until there is no entry left
        {
            players.add(result.getString(1));
        }

        return players;
    }

    public String getCardID(String playerName) throws SQLException
    {
        PreparedStatement stmt = connectionSQL.prepareStatement(
            "SELECT CardID FROM " + SylvDBDetails.getDBUserTableName() + " WHERE Name = ?"
        );

        stmt.setString(1, playerName);
        ResultSet result = stmt.executeQuery();

        return result.next() ? result.getString(1) : null;
        // if (result.next()) return result.getString(1); return null;
        // ─ Used to move the cursor to the next row, and check if the data exists & matches
    }

    public String getPlayerNameByCard(String code) throws SQLException
    {
        PreparedStatement stmt = connectionSQL.prepareStatement(
                "SELECT Name FROM " + SylvDBDetails.getDBUserTableName() + " WHERE CardID = ?"
        );

        stmt.setString(1, code);
        ResultSet result = stmt.executeQuery();

        return result.next() ? result.getString(1) : null;
    }

    public double getCardBalance(String playerName) throws SQLException
    {
        PreparedStatement stmt = connectionSQL.prepareStatement(
            "SELECT Balance FROM " + SylvDBDetails.getDBUserTableName() + " WHERE Name = ?"
        );

        stmt.setString(1, playerName);
        ResultSet result = stmt.executeQuery();

        return result.next() ? result.getDouble(1) : 0.00;
    }

    public void setCardBalance(String operation, double balance) throws SQLException
    {
        PreparedStatement stmt = connectionSQL.prepareStatement(
                "UPDATE " + SylvDBDetails.getDBUserTableName() + " SET Balance = ? WHERE Name = ?"
        );

        for (String players : getAllPlayers())
        {
            balanceOperation(operation, balance, stmt, players);
            stmt.executeUpdate();
        }
    }

    public void setCardBalance(String playerName, String operation, double balance) throws SQLException
    {
        PreparedStatement stmt = connectionSQL.prepareStatement(
            "UPDATE " + SylvDBDetails.getDBUserTableName() + " SET Balance = ? WHERE Name = ?"
        );

        balanceOperation(operation, balance, stmt, playerName);

        stmt.executeUpdate();
    }

    // will be used
    public void payUser (String srcPlayeruuid, String targetPlayeruuid, float amount) throws SQLException
    {
        PreparedStatement stmt = connectionSQL.prepareStatement(
            "CALL `PayPlayer` (?, ?, ?);"
        ); 
        stmt.setString(1, srcPlayeruuid);
        stmt.setString(2, targetPlayeruuid);
        stmt.setDouble(3, amount);

        stmt.executeUpdate(); 
    }

    public void payTerminal (String srcPlayeruuid, int targetCoordX, int targetCoordY, int targetCoordZ, float amount) throws SQLException
    {
        PreparedStatement stmt = connectionSQL.prepareStatement(
            "CALL `PayTerminal` (?, ?, ?, ?, ?);"
        ); 
        stmt.setString(1, srcPlayeruuid);
        stmt.setInt(2, targetCoordX);
        stmt.setInt(3, targetCoordY);
        stmt.setInt(4, targetCoordZ);
        stmt.setDouble(5, amount);

        stmt.executeUpdate(); 
    }

    public void transferTerminalBal (int SrcCoordX, int SrcCoordY, int SrcCoordZ, String tragetPlayteruuid) throws SQLException
    {
        PreparedStatement stmt = connectionSQL.prepareStatement(
            "CALL `TransferTerminalBal` (?, ?, ?, ?);"
        ); 
        stmt.setInt(1, SrcCoordX);
        stmt.setInt(2, SrcCoordY);
        stmt.setInt(3, SrcCoordZ);
        stmt.setString(4, tragetPlayteruuid);

        stmt.executeUpdate(); 
    }

    private void balanceOperation(String operation, double balance, PreparedStatement stmt, String players) throws SQLException
    {
        double currentBalance = getCardBalance(players);

        stmt.setString(2, players);

        switch (operation.toLowerCase())
        {
            case "add":
                stmt.setDouble(1, currentBalance + balance);
                break;

            case "subtract":
                stmt.setDouble(1, currentBalance - balance);
                break;

            default:
                stmt.setDouble(1, balance);
                break;
        }
    }
}

// Authentication for payments/transfers - IceKing
// Loan/Mortgage system for houses and plots - Cherry and JC

// Message from Akis - I'm not even gonna touch this one, I don't wanna break stuff :pray: