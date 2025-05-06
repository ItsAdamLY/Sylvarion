package com.itsadamly.sylvarion.events.ATM;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class SylvATMGUIOpener extends SylvATMGUI {
    private static final MiniMessage MM = MiniMessage.miniMessage();

    public SylvATMGUIOpener(Component title, Player player) {
        super(title, player);
    }

    private void openGUI(Inventory menu) {
        player.openInventory(menu);
    }

    public SylvATMGUIOpener(String miniMessageTitle, Player player) {
        this(MM.deserialize(miniMessageTitle), player);
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

    public void openValuesMenu() {
        openGUI(valuesMenu());
    }

    public void openATMOperationsMenu() {
        openGUI(atmOperationsMenu());
    }

    public void openConfirmMenu() {
        openGUI(confirmMenu());
    }
}