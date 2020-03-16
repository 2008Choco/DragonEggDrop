package com.ninjaguild.dragoneggdrop.listeners;

import java.util.List;
import java.util.stream.Collectors;

import com.ninjaguild.dragoneggdrop.dragon.DragonTemplate;
import com.ninjaguild.dragoneggdrop.dragon.loot.elements.DragonLootElementEgg;
import com.ninjaguild.dragoneggdrop.world.EndWorldWrapper;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class LootListeners implements Listener {

    @EventHandler
    public void onItemSpawn(ItemSpawnEvent event) {
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
        String eggName = egg.getName().replace("%dragon%", dragon.getName());
        List<String> eggLore = egg.getLore().stream().map(s -> s.replace("%dragon%", dragon.getName())).collect(Collectors.toList());

        ItemMeta eggMeta = stack.getItemMeta();

        if (eggName != null && !eggName.isEmpty()) {
            eggMeta.setDisplayName(eggName);
        }
        if (eggLore != null && !eggLore.isEmpty()) {
            eggMeta.setLore(eggLore);
        }

        stack.setItemMeta(eggMeta);
    }

}
