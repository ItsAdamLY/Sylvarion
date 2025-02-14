package com.itsadamly.sylvarion.databases.bank;

import com.itsadamly.sylvarion.Sylvarion;
import com.itsadamly.sylvarion.databases.SylvDBConnect;
import com.itsadamly.sylvarion.databases.SylvDBDetails;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SylvBankDBTasks
{
    private final Sylvarion pluginInstance = Sylvarion.getInstance();
    private final Connection connectionSQL = SylvDBConnect.getSQLConnection();

    public void createTable() throws SQLException
    {
        PreparedStatement stmt = connectionSQL.prepareStatement (
                "CREATE TABLE IF NOT EXISTS " + SylvDBDetails.getDBTableName() + " (" +
                        "ID INT NOT NULL AUTO_INCREMENT," +
                        "Name VARCHAR(100)," +
                        "UUID VARCHAR(100)," +
                        "CardID VARCHAR(20)," +
                        "Balance DECIMAL(10, 2)," +
                        "PRIMARY KEY (ID)" +
                ")"
        );
        stmt.executeUpdate();
    }

    public void createUser(Player player, String cardCode) throws SQLException
    {
        PreparedStatement stmt = connectionSQL.prepareStatement(
                "INSERT INTO " + SylvDBDetails.getDBTableName() + " (Name, UUID, CardID, Balance) " +
                        "VALUES (?, ?, ?, ?)"
        );

        // replace ? with following args by index
        stmt.setString(1, player.getName());
        stmt.setString(2, player.getUniqueId().toString());
        stmt.setString(3, cardCode);
        stmt.setDouble(4, 0.00);
        stmt.executeUpdate();
    }

    public void deleteUser(Player player) throws SQLException
    {
        PreparedStatement stmt = connectionSQL.prepareStatement(
                "DELETE FROM " + SylvDBDetails.getDBTableName() + " WHERE UUID = ?"
        );
        stmt.setString(1, player.getUniqueId().toString());
        stmt.executeUpdate();
    }

    public boolean isUserInDB(String uuid) throws SQLException
    {
        PreparedStatement stmt = connectionSQL.prepareStatement(
            "SELECT UUID FROM " + SylvDBDetails.getDBTableName() + " WHERE UUID = ?"
        );

        stmt.setString(1, uuid);
        ResultSet result = stmt.executeQuery();

        return result.next();
    }

    public String getCardID(String uuid) throws SQLException
    {
        PreparedStatement stmt = connectionSQL.prepareStatement(
                "SELECT CardID FROM " + SylvDBDetails.getDBTableName() + " WHERE UUID = ?"
        );

        stmt.setString(1, uuid);
        ResultSet result = stmt.executeQuery();

        return result.next() ? result.getString(1) : null;
        // if (result.next()) return result.getString(1);
        // ─ Used to move the cursor to the next row, and check if the data exists & matches
    }

    public double getCardBalance(String uuid) throws SQLException
    {
        PreparedStatement stmt = connectionSQL.prepareStatement(
                "SELECT Balance FROM " + SylvDBDetails.getDBTableName() + " WHERE UUID = ?"
        );

        stmt.setString(1, uuid);
        ResultSet result = stmt.executeQuery();

        return result.next() ? result.getDouble(1) : 0.00;
    }

}
