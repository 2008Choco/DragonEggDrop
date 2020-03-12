package com.ninjaguild.dragoneggdrop.placeholder;

import com.ninjaguild.dragoneggdrop.DragonEggDrop;

import org.bukkit.plugin.PluginManager;

public final class DragonEggDropPlaceholders {

    private DragonEggDropPlaceholders() { }

    public static void registerPlaceholders(DragonEggDrop plugin, PluginManager pluginManager) {
        // Register placeholders to MVdWPlaceholderAPI if available
        if (pluginManager.isPluginEnabled("MVdWPlaceholderAPI")) {
            DragonEggDropMVdWPlaceholderAPI.registerPlaceholders(plugin);
        }

        // Register the DragonEggDrop PlaceholderExpansion
        if (pluginManager.isPluginEnabled("PlaceholderAPI")) {
            new DragonEggDropPlaceholderAPIExpansion(plugin).register();
        }
    }

}
