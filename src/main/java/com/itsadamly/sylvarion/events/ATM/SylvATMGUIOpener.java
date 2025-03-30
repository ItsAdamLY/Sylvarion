package com.itsadamly.sylvarion.events.ATM;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class SylvATMGUIOpener extends SylvATMGUI
{
    public SylvATMGUIOpener(String title, Player player)
    {
        super(title, player);
    }

    private void openGUI(Inventory menu)
    {
        player.openInventory(menu);
    }

    public void openATM()
    {
        openGUI(atmMenu());
    }

    public void openTestMenu()
    {
        openGUI(testMenu());
    }

    public void openInputCardMenu() { openGUI(inputCardMenu()); }

    public void openValuesMenu()
    {
        openGUI(valuesMenu());
    }

    public void openATMOperationsMenu()
    {
        openGUI(atmOperationsMenu());
    }

    public void openConfirmMenu()
    {
        openGUI(confirmMenu());
    }
}
