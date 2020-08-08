package com.ninjaguild.dragoneggdrop.listeners;

import java.util.List;

import com.ninjaguild.dragoneggdrop.dragon.DragonTemplate;
import com.ninjaguild.dragoneggdrop.dragon.loot.elements.DragonLootElementEgg;
import com.ninjaguild.dragoneggdrop.placeholder.DragonEggDropPlaceholders;
import com.ninjaguild.dragoneggdrop.world.EndWorldWrapper;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class LootListeners implements Listener {

    @EventHandler
    private void onItemSpawn(ItemSpawnEvent event) {
        Item item = event.getEntity();
        ItemStack stack = item.getItemStack();
        World world = item.getWorld();

        if (world.getEnvironment() != Environment.THE_END || stack.getType() != Material.DRAGON_EGG || stack.hasItemMeta()) {
            return;
        }

        DragonTemplate dragon = EndWorldWrapper.of(world).getPreviousTemplate();
        if (dragon == null) {
            return;
        }

        DragonLootElementEgg egg = dragon.getLootTable().getEgg();
        if (egg == null) {
            return;
        }

        ItemMeta meta = stack.getItemMeta();
        String name = egg.getName();
        if (name != null) {
            meta.setDisplayName(name);
        }

        List<String> lore = egg.getLore();
        if (lore != null && !lore.isEmpty()) {
            meta.setLore(lore);
        }

        stack.setItemMeta(meta);

        List<Player> players = world.getPlayers();
        if (players.size() >= 1) { // Only need it for the world anyways
            DragonEggDropPlaceholders.inject(players.get(0), stack);
        }
    }

}
