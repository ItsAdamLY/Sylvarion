package com.itsadamly.sylvarion.databases.bank;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BankCard
{

    public ItemStack createCard(Player player, String cardCode)
    {
        ItemStack card = new ItemStack(Material.NAME_TAG);
        ItemMeta cardMeta = card.getItemMeta();

        List<String> cardDetails = new ArrayList<>();

        cardMeta.setDisplayName(player.getName() + "'s Bank Card");

        cardDetails.add(ChatColor.GRAY + "§o" + cardCode);

        cardDetails.add(ChatColor.RED + "MAST" + ChatColor.GOLD + "ER" + ChatColor.YELLOW + "CARD");

        cardMeta.setLore(cardDetails);
        card.setItemMeta(cardMeta);

        return card;
    }

    public String cardID()
    {
        int cardNo1 = new Random().nextInt(100, 999);
        int cardNo2 = new Random().nextInt(1000, 9999);
        int cardNo3 = new Random().nextInt(1000, 9999);
        int cardNo4 = new Random().nextInt(1000, 9999);

        return "5" + cardNo1 + '-' + cardNo2 + '-' + cardNo3 + '-' + cardNo4;
    }
}
