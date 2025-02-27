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
    public SylvATMGUI(Player player)
    {
        Inventory atmGUI = Bukkit.createInventory(null, 9, ChatColor.DARK_GREEN + "ATM");

        ItemStack open = atmMenuElement(Material.GOLD_BLOCK, ChatColor.YELLOW + "Balance", ChatColor.GRAY, new ArrayList<>() {{
            add("§oOpen your account.");
        }});

        ItemStack close = atmMenuElement(Material.REDSTONE_BLOCK, ChatColor.RED + "Close", ChatColor.GRAY, new ArrayList<>() {{
            add("§oClose your account.");
        }});

        atmGUI.setItem(1, open);
        atmGUI.setItem(7, close);

        player.openInventory(atmGUI);
    }

    private ItemStack atmMenuElement(Material material, String itemName, ChatColor color, ArrayList<String> description)
    {
        ItemStack elementItem = new ItemStack(material);
        ItemMeta elementMeta = elementItem.getItemMeta();

        List<String> elementDesc = new ArrayList<>();

        for (String desc : description)
        {
            elementDesc.add(color + desc);
        }

        assert elementMeta != null;
        elementMeta.setDisplayName(itemName);
        elementMeta.setLore(elementDesc);

        elementItem.setItemMeta(elementMeta);

        return elementItem;
    }

}
