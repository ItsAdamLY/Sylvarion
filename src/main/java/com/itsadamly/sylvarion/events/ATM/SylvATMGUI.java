package com.itsadamly.sylvarion.events.ATM;

import com.itsadamly.sylvarion.databases.SylvDBDetails;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class SylvATMGUI {
    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final String CURRENCY = SylvDBDetails.getCurrencySymbol();

    protected final Inventory atmGUI;
    protected final Player player;

    public SylvATMGUI(Component title, Player player) {
        this.atmGUI = Bukkit.createInventory(null, 9, title);
        this.player = player;
    }

    protected static ItemStack atmMenuElement(Material material, Component itemName, List<Component> description) {
        ItemStack elementItem = new ItemStack(material);
        ItemMeta elementMeta = elementItem.getItemMeta();

        if (elementMeta != null) {
            elementMeta.displayName(itemName);
            elementMeta.lore(description);
            elementItem.setItemMeta(elementMeta);
        }

        return elementItem;
    }

    protected Inventory atmMenu() {
        ItemStack open = atmMenuElement(
                Material.GOLD_BLOCK,
                MM.deserialize("<green>Open"),
                List.of(MM.deserialize("<italic>Open your account."))
        );

        ItemStack close = atmMenuElement(
                Material.REDSTONE_BLOCK,
                MM.deserialize("<red>Close"),
                List.of(MM.deserialize("<italic>Close your account."))
        );

        ItemStack card = atmMenuElement(
                Material.NAME_TAG,
                MM.deserialize("<yellow>Card"),
                List.of(MM.deserialize("<italic>Check your card."))
        );

        atmGUI.setItem(1, open);
        atmGUI.setItem(4, card);
        atmGUI.setItem(7, close);

        return atmGUI;
    }

    protected Inventory testMenu() {
        ItemStack test = atmMenuElement(
                Material.DIAMOND,
                MM.deserialize("<yellow>Test"),
                List.of(MM.deserialize("<italic>Test"))
        );

        atmGUI.setItem(4, test);
        return atmGUI;
    }

    protected Inventory inputCardMenu() {
        ItemStack glass = atmMenuElement(
                Material.BLACK_STAINED_GLASS_PANE,
                Component.empty(),
                new ArrayList<>()
        );

        for (int i = 0; i < 9; i++) {
            if (i != 4) {
                atmGUI.setItem(i, glass);
            }
        }

        return atmGUI;
    }

    protected Inventory valuesMenu() {
        double[] values = {10.00, 20.00, 50.00, 100.00, 200.00, 500.00, 1000.00};

        ItemStack back = atmMenuElement(
                Material.PAPER,
                MM.deserialize("<red>Back"),
                List.of(MM.deserialize("<italic>Return to previous menu."))
        );

        ItemStack close = atmMenuElement(
                Material.BARRIER,
                MM.deserialize("<red>Close"),
                new ArrayList<>()
        );

        atmGUI.setItem(0, back);

        for (int i = 0; i < values.length; i++) {
            Component valueName = MM.deserialize(String.format(
                    "<green>%s %.2f",
                    CURRENCY,
                    values[i]
            ));

            ItemStack value = atmMenuElement(
                    Material.LIME_STAINED_GLASS_PANE,
                    valueName,
                    new ArrayList<>()
            );

            atmGUI.setItem(i + 1, value);
        }

        atmGUI.setItem(8, close);
        return atmGUI;
    }

    protected Inventory confirmMenu() {
        ItemStack yes = atmMenuElement(
                Material.GREEN_TERRACOTTA,
                MM.deserialize("<green>Yes"),
                new ArrayList<>()
        );

        ItemStack no = atmMenuElement(
                Material.RED_TERRACOTTA,
                MM.deserialize("<red>No"),
                new ArrayList<>()
        );

        atmGUI.setItem(2, yes);
        atmGUI.setItem(6, no);

        return atmGUI;
    }

    protected Inventory atmOperationsMenu() {
        ItemStack deposit = atmMenuElement(
                Material.DIAMOND_BLOCK,
                MM.deserialize("<yellow>Deposit"),
                List.of(MM.deserialize("<italic>Deposit money."))
        );

        ItemStack withdraw = atmMenuElement(
                Material.EMERALD_BLOCK,
                MM.deserialize("<aqua>Withdraw"),
                List.of(MM.deserialize("<italic>Withdraw money."))
        );

        ItemStack transfer = atmMenuElement(
                Material.GOLD_INGOT,
                MM.deserialize("<green>Transfer"),
                List.of(MM.deserialize("<italic>Transfer money to another account."))
        );

        ItemStack balance = atmMenuElement(
                Material.EMERALD,
                MM.deserialize("<green>Balance"),
                List.of(MM.deserialize("<italic>Check account balance."))
        );

        ItemStack back = atmMenuElement(
                Material.PAPER,
                MM.deserialize("<red>Back"),
                List.of(MM.deserialize("<italic>Return to main menu."))
        );

        ItemStack close = atmMenuElement(
                Material.BARRIER,
                MM.deserialize("<red>Close"),
                List.of(MM.deserialize("<italic>Abort menu."))
        );

        atmGUI.setItem(0, back);
        atmGUI.setItem(2, withdraw);
        atmGUI.setItem(3, balance);
        atmGUI.setItem(5, deposit);
        atmGUI.setItem(6, transfer);
        atmGUI.setItem(8, close);

        return atmGUI;
    }
}