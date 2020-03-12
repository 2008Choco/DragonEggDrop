package com.ninjaguild.dragoneggdrop.placeholder;

import com.ninjaguild.dragoneggdrop.DragonEggDrop;
import com.ninjaguild.dragoneggdrop.dragon.DragonTemplate;
import com.ninjaguild.dragoneggdrop.management.EndWorldWrapper;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;

final class DragonEggDropPlaceholderAPIExpansion extends PlaceholderExpansion {

    private final DragonEggDrop plugin;

    DragonEggDropPlaceholderAPIExpansion(DragonEggDrop plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public String getIdentifier() {
        return plugin.getDescription().getName().toLowerCase(); // dragoneggdrop
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, String placeholder) {
        if (placeholder.equalsIgnoreCase("dragon")) { // %dragoneggdrop_dragon%
            if (player == null) {
                return null;
            }

            World world = player.getWorld();
            if (world.getEnvironment() != Environment.THE_END) {
                return null;
            }

            EndWorldWrapper endWorld = plugin.getDEDManager().getWorldWrapper(world);
            DragonTemplate template = endWorld.getActiveBattle();
            return (template != null) ? template.getName() : null;
        }

        else if (placeholder.startsWith("dragon_")) { // %dragoneggdrop_<world>%
            World world = Bukkit.getWorld(placeholder.substring("dragon_".length()));
            if (world == null || world.getEnvironment() != Environment.THE_END) {
                return null;
            }

            EndWorldWrapper endWorld = plugin.getDEDManager().getWorldWrapper(world);
            DragonTemplate template = endWorld.getActiveBattle();
            return (template != null) ? template.getName() : null;
        }

        return null;
    }

}
