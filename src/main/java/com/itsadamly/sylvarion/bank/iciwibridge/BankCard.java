package com.itsadamly.sylvarion.bank.iciwibridge;
import com.itsadamly.sylvarion.databases.bank.SylvBankDBTasks;
import mikeshafter.iciwi.api.IcCard;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class BankCard implements IcCard
{
private final String serial;
SylvBankDBTasks bankTasks;

public BankCard (ItemStack item)
{
	ItemMeta meta = item.getItemMeta();
	if (meta == null)
	{
		this.serial = "";
		return;
	}
	List<String> lore = meta.getLore();
	if (lore == null)
	{
		this.serial = "";
		return;
	}
	this.serial = lore.get(1);
}

@Override
public boolean withdraw (double v)
{
	try
	{
		String username = bankTasks.getPlayerNameByCard(this.serial);
		bankTasks.setCardBalance(username, "subtract", v);
		return true;
	}
	catch (SQLException e)
	{
		return false;
	}
}

@Override
public String getSerial ()
{
	return this.serial;
}

@Override
public boolean deposit (double v)
{
	try
	{
		String username = bankTasks.getPlayerNameByCard(this.serial);
		bankTasks.setCardBalance(username, "add", v);
		return true;
	}
	catch (SQLException e)
	{
		return false;
	}
}

@Override
public Map<String, Long> getRailPasses () {
	return Map.of();
	// Iciwi: No support for rail passes on external cards yet
}

@Override
public void setRailPass (String railPassName, long start) {
	// Iciwi: No support for rail passes on external cards yet
}

@Override
public long getExpiry (String railPassName) {
	return 0;
	// Iciwi: No support for rail passes on external cards yet
}
}
