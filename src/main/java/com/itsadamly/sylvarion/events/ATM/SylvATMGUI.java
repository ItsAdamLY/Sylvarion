package com.itsadamly.sylvarion.events.ATM;

import com.itsadamly.sylvarion.databases.SylvDBDetails;
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
    private static final String CURRENCY = SylvDBDetails.getCurrencySymbol();

    protected final Inventory atmGUI;
    protected final Player player;

    public SylvATMGUI(String title, Player player)
    {
        this.atmGUI = Bukkit.createInventory(null, 9, ChatColor.DARK_GREEN + title);
        this.player = player;
    }

    protected static ItemStack atmMenuElement(Material material, String itemName, ArrayList<String> description)
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

    protected Inventory atmMenu()
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

    protected Inventory testMenu()
    {
        ItemStack test = atmMenuElement(Material.DIAMOND, ChatColor.YELLOW + "Test", new ArrayList<>() {{
            add("§oTest");
        }});

        atmGUI.setItem(4, test);

        return atmGUI;
    }

    protected Inventory inputCardMenu()
    {
        ItemStack glass = atmMenuElement(Material.BLACK_STAINED_GLASS_PANE, ChatColor.WHITE + "", new ArrayList<>());

        for (int i = 0; i < 9; i++)
        {
            if (i != 4)
                atmGUI.setItem(i, glass);
        }

        return atmGUI;
    }

    protected Inventory valuesMenu()
    {
        double[] values = { 10.00, 20.00, 50.00, 100.00, 200.00, 500.00, 1000.00 };

        ItemStack back = atmMenuElement(Material.PAPER, ChatColor.RED + "Back", new ArrayList<>() {{
            add("§oReturn to previous menu.");
        }});

        ItemStack close = atmMenuElement(Material.BARRIER, ChatColor.RED + "Close", new ArrayList<>());

        atmGUI.setItem(0, back);

        for (int i = 0; i < values.length; i++)
        {
            ItemStack value = atmMenuElement(Material.LIME_STAINED_GLASS_PANE, ChatColor.GREEN +
                      CURRENCY + " " + String.format("%.2f", values[i]), new ArrayList<>());

            atmGUI.setItem(i+1, value);
        }

        atmGUI.setItem(8, close);

        return atmGUI;
    }

    protected Inventory confirmMenu()
    {
        ItemStack yes = atmMenuElement(Material.GREEN_TERRACOTTA, ChatColor.GREEN + "Yes", new ArrayList<>());
        ItemStack no = atmMenuElement(Material.RED_TERRACOTTA, ChatColor.RED + "No", new ArrayList<>());

        atmGUI.setItem(2, yes);
        atmGUI.setItem(6, no);

        return atmGUI;
    }

    protected Inventory atmOperationsMenu()
    {
        ItemStack deposit = atmMenuElement(Material.DIAMOND_BLOCK, ChatColor.YELLOW + "Deposit", new ArrayList<>() {{
            add("§oDeposit money.");
        }});

        ItemStack withdraw = atmMenuElement(Material.EMERALD_BLOCK, ChatColor.AQUA + "Withdraw", new ArrayList<>() {{
            add("§oWithdraw money.");
        }});

        ItemStack transfer = atmMenuElement(Material.GOLD_INGOT, ChatColor.GREEN + "Transfer", new ArrayList<>() {{
            add("§oTransfer money to another account.");
        }});


        ItemStack balance = atmMenuElement(Material.EMERALD, ChatColor.GREEN + "Balance", new ArrayList<>() {{
            add("§oCheck account balance.");
        }});

        ItemStack back = atmMenuElement(Material.PAPER, ChatColor.RED + "Back", new ArrayList<>() {{
            add("§oReturn to main menu.");
        }});

        ItemStack close = atmMenuElement(Material.BARRIER, ChatColor.RED + "Close", new ArrayList<>() {{
            add("§oAbort menu.");
        }});

        atmGUI.setItem(0, back);

        atmGUI.setItem(2, withdraw);
        atmGUI.setItem(3, balance);
        atmGUI.setItem(5, deposit);
        atmGUI.setItem(6, transfer);

        atmGUI.setItem(8, close);

        return atmGUI;
    }
}
