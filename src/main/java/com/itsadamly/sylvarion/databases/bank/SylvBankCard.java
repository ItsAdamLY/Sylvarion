package com.itsadamly.sylvarion.databases.bank;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SylvBankCard {

    private static final Random RANDOM = new Random();
    private static final MiniMessage MM = MiniMessage.miniMessage();

    public ItemStack createCard(String playerName, String cardCode) {
        ItemStack card = new ItemStack(Material.NAME_TAG);
        ItemMeta meta = card.getItemMeta();

        if (meta != null) {
            meta.displayName(MM.deserialize(playerName + "'s Bank Card"));

            List<Component> lore = new ArrayList<>();
            lore.add("Sylvarion");
            lore.add(MM.deserialize("<gray><i>" + cardCode));
            lore.add(MM.deserialize("<gradient:#EB001B:yellow>MASTERCARD</gradient>"));

            meta.lore(lore);
            card.setItemMeta(meta);
        }

        return card;
    }

    public String cardID() {
        return String.format(
                "5%02d-%04d-%04d-%04d",
                RANDOM.nextInt(900) + 100,
                RANDOM.nextInt(10000),
                RANDOM.nextInt(10000),
                RANDOM.nextInt(10000)
        );
    }
}
