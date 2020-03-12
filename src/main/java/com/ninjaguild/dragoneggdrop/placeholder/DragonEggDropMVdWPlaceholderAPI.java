package com.ninjaguild.dragoneggdrop.placeholder;

import com.ninjaguild.dragoneggdrop.DragonEggDrop;
import com.ninjaguild.dragoneggdrop.dragon.DragonTemplate;
import com.ninjaguild.dragoneggdrop.management.EndWorldWrapper;

import org.bukkit.World;
import org.bukkit.World.Environment;

import be.maximvdw.placeholderapi.PlaceholderAPI;

final class DragonEggDropMVdWPlaceholderAPI {

    private DragonEggDropMVdWPlaceholderAPI() { }

    static void registerPlaceholders(DragonEggDrop plugin) {
        String id = plugin.getName().toLowerCase() + "_"; // Prefix all MVdW placeholders with "dragoneggdrop_"

        PlaceholderAPI.registerPlaceholder(plugin, id + "dragon", event -> { // {dragoneggdrop_dragon}
            if (!event.isOnline()) {
                return null;
            }

            World world = event.getPlayer().getWorld();
            if (world.getEnvironment() != Environment.THE_END) {
                return null;
            }

            EndWorldWrapper endWorld = plugin.getDEDManager().getWorldWrapper(world);
            DragonTemplate template = endWorld.getActiveBattle();
            return (template != null) ? template.getName() : null;
        });
    }

}
