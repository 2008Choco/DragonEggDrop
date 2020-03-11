package com.ninjaguild.dragoneggdrop.dragon.loot.elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import com.ninjaguild.dragoneggdrop.nms.DragonBattle;

import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class DragonLootElementEgg implements IDragonLootElement {

    private final String name;
    private final List<String> lore;
    private final double chance;
    private final boolean centered;

    public DragonLootElementEgg(String name, boolean centered, double chance, List<String> lore) {
        this.name = name;
        this.lore = new ArrayList<>(lore);
        this.chance = chance;
        this.centered = centered;
    }

    public DragonLootElementEgg(String name, double chance, List<String> lore) {
        this(name, true, chance, lore);
    }

    public DragonLootElementEgg(double chance) {
        this(null, true, chance, Collections.EMPTY_LIST);
    }

    public DragonLootElementEgg() {
        this(null, true, 100.0, Collections.EMPTY_LIST);
    }

    public String getName() {
        return name;
    }

    public List<String> getLore() {
        return Collections.unmodifiableList(lore);
    }

    public double getChance() {
        return chance;
    }

    public boolean isCentered() {
        return centered;
    }

    @Override
    public double getWeight() {
        return 0.0; // Hardcode a weight of 0 here. The egg is not meant to be generated
    }

    @Override
    public void generate(DragonBattle battle, EnderDragon dragon, Player killer, Random random, Chest chest) {
        if (random.nextDouble() * 100 >= chance) {
            return;
        }

        if (chest == null) { // If no chest is present, just set the egg on the portal
            battle.getEndPortalLocation().getBlock().setType(Material.DRAGON_EGG);
            return;
        }

        ItemStack egg = new ItemStack(Material.DRAGON_EGG);
        ItemMeta eggMeta = egg.getItemMeta();

        if (name != null) {
            eggMeta.setDisplayName(name.replace("%dragon%", dragon.getName()));
        }

        if (lore != null && !lore.isEmpty()) {
            List<String> contextualLore = lore.stream().map(s -> s.replace("%dragon%", dragon.getName())).collect(Collectors.toList());
            eggMeta.setLore(contextualLore);
        }

        egg.setItemMeta(eggMeta);

        Inventory inventory = chest.getInventory();
        if (centered) {
            inventory.setItem(inventory.getSize() / 2, egg);
        }
        else if (inventory.firstEmpty() != -1) {
            boolean success = false;

            do {
                int slot = random.nextInt(inventory.getSize());
                if (inventory.getItem(slot) == null) {
                    inventory.setItem(slot, egg);
                    success = true;
                }
            } while (!success);
        }
    }

}
