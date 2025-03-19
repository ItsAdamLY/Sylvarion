package com.itsadamly.sylvarion.events.ATM;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class SylvATMGUI
{
    private final Inventory atmGUI;
    private final Player player;

    public SylvATMGUI(String title, Player player)
    {
        this.atmGUI = Bukkit.createInventory(null, 9, ChatColor.DARK_GREEN + title);
        this.player = player;
    }

    private static ItemStack atmMenuElement(Material material, String itemName, ArrayList<String> description)
    {
        ItemStack elementItem = new ItemStack(material);
        ItemMeta elementMeta = elementItem.getItemMeta();

        List<String> elementDesc = new ArrayList<>(description);

        assert elementMeta != null;
        elementMeta.setDisplayName(itemName);
        elementMeta.setLore(elementDesc);

        elementItem.setItemMeta(elementMeta);

        return elementItem;
    }

    private Inventory atmMenu()
    {
        ItemStack open = atmMenuElement(Material.GOLD_BLOCK, ChatColor.GREEN + "Open", new ArrayList<>() {{
            add("§oOpen your account.");
        }});

        ItemStack close = atmMenuElement(Material.REDSTONE_BLOCK, ChatColor.RED + "Close", new ArrayList<>() {{
            add("§oClose your account.");
        }});

        ItemStack card = atmMenuElement(Material.NAME_TAG, ChatColor.YELLOW + "Card", new ArrayList<>() {{
            add("§oCheck your card.");
        }});

        atmGUI.setItem(1, open);
        atmGUI.setItem(4, card);
        atmGUI.setItem(7, close);

        return atmGUI;
    }

    private Inventory testMenu()
    {
        ItemStack test = atmMenuElement(Material.DIAMOND, ChatColor.YELLOW + "Test", new ArrayList<>() {{
            add("§oTest");
        }});

        atmGUI.setItem(4, test);

        return atmGUI;
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
}
