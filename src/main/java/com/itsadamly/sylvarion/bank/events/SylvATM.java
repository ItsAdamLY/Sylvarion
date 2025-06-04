package com.itsadamly.sylvarion.bank.events;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class SylvATM extends SylvATMGUI
{
    public SylvATM(Player player) {
        super(player);
    }

    private void openGUI(Inventory menu) {
        player.openInventory(menu);
    }

    public void openATM() {
        openGUI(atmMenu());
    }

    public void openTestMenu() {
        openGUI(testMenu());
    }

    public void openInputCardMenu() {
        openGUI(inputCardMenu());
    }

    public void openValuesMenu(Component title) {
        openGUI(valuesMenu(title));
    }

    public void openATMOperationsMenu() {
        openGUI(atmOperationsMenu());
    }

    public void openConfirmMenu(Component title) {
        openGUI(confirmMenu(title));
    }
}