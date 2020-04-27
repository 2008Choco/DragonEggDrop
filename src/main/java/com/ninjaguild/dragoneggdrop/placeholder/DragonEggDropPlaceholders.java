package com.ninjaguild.dragoneggdrop.placeholder;

import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.ninjaguild.dragoneggdrop.DragonEggDrop;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;

public final class DragonEggDropPlaceholders {

    protected static final Pattern PATTERN_TOP_DAMAGER = Pattern.compile("top_damager(?:_(\\d+))?(?:_([\\w\\d]+))?");
    protected static final Pattern PATTERN_TOP_DAMAGE = Pattern.compile("top_damage(?:_(\\d+))?(?:_([\\w\\d]+))?");

    private DragonEggDropPlaceholders() { }

    public static void registerPlaceholders(DragonEggDrop plugin, PluginManager pluginManager) {
        // Register the DragonEggDrop PlaceholderExpansion
        if (pluginManager.isPluginEnabled("PlaceholderAPI")) {
            new DragonEggDropPlaceholderAPIExpansion(plugin).register();
        }
    }

    public static String inject(OfflinePlayer player, String string) {
        PluginManager pluginManager = Bukkit.getPluginManager();

        if (pluginManager.isPluginEnabled("PlaceholderAPI")) {
            string = DragonEggDropPlaceholderAPIExpansion.inject(player, string);
        }

        return string;
    }

    public static void inject(OfflinePlayer player, ItemStack item) {
        // Placeholder injection
        ItemMeta meta = item.getItemMeta();
        if (meta.hasDisplayName()) {
            meta.setDisplayName(DragonEggDropPlaceholders.inject(player, meta.getDisplayName()));
        }

        if (meta.hasLore()) {
            meta.setLore(meta.getLore().stream().map(s -> DragonEggDropPlaceholders.inject(player, s)).collect(Collectors.toList()));
        }

        item.setItemMeta(meta);
    }

}
