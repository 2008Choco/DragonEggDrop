package com.ninjaguild.dragoneggdrop.dragon.loot.elements;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.ninjaguild.dragoneggdrop.nms.DragonBattle;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Chest;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class DragonLootElementItem implements IDragonLootElement {

    private final ItemStack item;
    private final double weight;
    private final int min, max;

    public DragonLootElementItem(ItemStack item, double weight, int min, int max) {
        this.item = item;
        this.weight = weight;
        this.min = min;
        this.max = max;
    }

    @Override
    public double getWeight() {
        return weight;
    }

    @Override
    public void generate(DragonBattle battle, EnderDragon dragon, Player killer, Random random, Chest chest) {
        if (chest == null) {
            return;
        }

        Inventory inventory = chest.getInventory();
        if (inventory.firstEmpty() == -1) {
            return;
        }

        int slot;
        do {
            slot = random.nextInt(inventory.getSize());
        } while (inventory.getItem(slot) != null);

        ItemStack generated = item.clone();
        generated.setAmount(Math.max(random.nextInt(max), min));
        inventory.setItem(slot, generated);
    }

    public static DragonLootElementItem fromJson(JsonObject root) {
        double weight = root.has("weight") ? Math.max(root.get("weight").getAsDouble(), 0.0) : 1.0;
        String name = root.has("name") ? ChatColor.translateAlternateColorCodes('&', root.get("name").getAsString()) : null;
        int minAmount = 1, maxAmount = 1;

        if (!root.has("type")) {
            throw new JsonParseException("Could not find \"type\" for item in loot pool");
        }

        Material type = Material.matchMaterial(root.get("type").getAsString());
        if (type == null) {
            throw new IllegalStateException("Could not create item of type \"" + root.get("type").getAsString() + "\" for item loot pool. Does it exist?");
        }

        ItemStack item = new ItemStack(type);
        ItemMeta meta = item.getItemMeta();

        if (root.has("amount")) {
            JsonElement amount = root.get("amount");
            if (amount.isJsonPrimitive()) {
                minAmount = maxAmount = Math.max(amount.getAsInt(), 0);
            }

            else if (amount.isJsonObject()) {
                JsonObject amountObject = amount.getAsJsonObject();
                minAmount = amountObject.has("min") ? Math.max(amountObject.get("min").getAsInt(), 0) : 0;
                maxAmount = amountObject.has("max") ? Math.max(amountObject.get("max").getAsInt(), 0) : minAmount;
            }
        }

        if (root.has("lore") && root.get("lore").isJsonArray()) {
            List<String> lore = new ArrayList<>();

            JsonArray loreObject = root.getAsJsonArray("lore");
            for (JsonElement element : loreObject) {
                if (!element.isJsonPrimitive()) {
                    throw new JsonParseException("Malformated lore in item loot pool. Expected String, got " + element.getClass().getSimpleName());
                }

                lore.add(element.getAsString());
            }

            if (!lore.isEmpty()) {
                meta.setLore(lore);
            }
        }

        if (root.has("enchantments") && root.get("enchantments").isJsonObject()) {
            Map<Enchantment, Integer> enchantments = new IdentityHashMap<>();

            JsonObject enchantmentsRoot = root.getAsJsonObject("enchantments");
            for (Entry<String, JsonElement> enchantmentElement : enchantmentsRoot.entrySet()) {
                Enchantment enchantment = Enchantment.getByKey(toNamespacedKey(enchantmentElement.getKey()));
                if (enchantment == null) {
                    throw new IllegalStateException("Could not find enchantment with id \"" + enchantmentElement.getKey() + "\" for item loot pool. Does it exist?");
                }

                int level = Math.max(enchantmentElement.getValue().getAsInt(), 0);
                if (level != 0) {
                    enchantments.put(enchantment, level);
                }
            }

            if (!enchantments.isEmpty()) {
                enchantments.forEach((enchantment, level) -> meta.addEnchant(enchantment, level, true));
            }
        }

        if (name != null) {
            meta.setDisplayName(name);
        }

        item.setItemMeta(meta);
        return new DragonLootElementItem(item, weight, minAmount, maxAmount);
    }

    @SuppressWarnings("deprecation")
    private static NamespacedKey toNamespacedKey(String key) {
        if (key == null) {
            return null;
        }

        if (!key.contains(":")) {
            return NamespacedKey.minecraft(key);
        }

        String[] parts = key.split(":", 2);
        if (parts.length != 2 || parts[1].contains(":")) {
            throw new IllegalStateException("Malformed namespaced key: \"" + key + "\"");
        }

        // This constructor really shouldn't be deprecated
        return new NamespacedKey(parts[0], parts[1]);
    }

}
