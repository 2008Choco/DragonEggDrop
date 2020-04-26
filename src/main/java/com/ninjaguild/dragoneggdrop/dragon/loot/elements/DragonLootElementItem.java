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
import com.ninjaguild.dragoneggdrop.placeholder.DragonEggDropPlaceholders;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Chest;
import org.bukkit.boss.DragonBattle;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * An implementation of {@link IDragonLootElement} to represent an item.
 *
 * @author Parker Hawke - Choco
 */
public class DragonLootElementItem implements IDragonLootElement {

    private final ItemStack item;
    private final int min, max;
    private final double weight;

    /**
     * Create a {@link DragonLootElementCommand}.
     *
     * @param item the item to generate
     * @param min the minimum amount of this item to generate (inclusive)
     * @param max the maximum amount of this item to generate (inclusive)
     * @param weight this element's weight in the loot pool
     */
    public DragonLootElementItem(ItemStack item, int min, int max, double weight) {
        this.item = item;
        this.min = min;
        this.max = max;
        this.weight = weight;
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
        DragonEggDropPlaceholders.inject(generated);
        generated.setAmount(Math.max(random.nextInt(max), min));
        inventory.setItem(slot, generated);
    }

    /**
     * Parse a {@link DragonLootElementItem} instance from a {@link JsonObject}.
     *
     * @param root the root element that represents this element
     *
     * @return the created instance
     *
     * @throws JsonParseException if parsing the object has failed
     */
    public static DragonLootElementItem fromJson(JsonObject root) {
        double weight = root.has("weight") ? Math.max(root.get("weight").getAsDouble(), 0.0) : 1.0;
        String name = root.has("name") ? ChatColor.translateAlternateColorCodes('&', root.get("name").getAsString()) : null;
        int minAmount = 1, maxAmount = 1;

        if (!root.has("type")) {
            throw new JsonParseException("Could not find \"type\" for item in loot pool");
        }

        Material type = Material.matchMaterial(root.get("type").getAsString());
        if (type == null) {
            throw new JsonParseException("Could not create item of type \"" + root.get("type").getAsString() + "\" for item loot pool. Does it exist?");
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
                    throw new JsonParseException("Could not find enchantment with id \"" + enchantmentElement.getKey() + "\" for item loot pool. Does it exist?");
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
        return new DragonLootElementItem(item, minAmount, maxAmount, weight);
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
            throw new JsonParseException("Malformed namespaced key: \"" + key + "\"");
        }

        // This constructor really shouldn't be deprecated
        return new NamespacedKey(parts[0], parts[1]);
    }

}
