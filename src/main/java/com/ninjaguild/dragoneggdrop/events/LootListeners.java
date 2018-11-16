package com.ninjaguild.dragoneggdrop.events;

import java.util.List;
import java.util.stream.Collectors;

import com.ninjaguild.dragoneggdrop.DragonEggDrop;
import com.ninjaguild.dragoneggdrop.dragon.DragonLoot;
import com.ninjaguild.dragoneggdrop.dragon.DragonTemplate;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class LootListeners implements Listener {
	
	private final DragonEggDrop plugin;
	
	public LootListeners(final DragonEggDrop plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onItemSpawn(ItemSpawnEvent event) {
		Item item = event.getEntity();
		ItemStack stack = item.getItemStack();
		World world = item.getWorld();
		
		if (world.getEnvironment() != Environment.THE_END || stack.getType() != Material.DRAGON_EGG
				|| stack.hasItemMeta()) return;
		
		DragonTemplate dragon = plugin.getDEDManager().getWorldWrapper(world).getActiveBattle();
		if (dragon == null) return;
		
		DragonLoot loot = dragon.getLoot();
		
		String eggName = loot.getEggName().replace("%dragon%", dragon.getName());
		List<String> eggLore = loot.getEggLore().stream()
				.map(s -> s.replace("%dragon%", dragon.getName()))
				.collect(Collectors.toList());

		ItemMeta eggMeta = stack.getItemMeta();
		
		if (eggName != null && !eggName.isEmpty()) {
			eggMeta.setDisplayName(eggName);
		}
		if (eggLore != null && !eggLore.isEmpty()) {
			eggMeta.setLore(eggLore);
		}
		
		stack.setItemMeta(eggMeta);
	}
	
	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		Entity entity = event.getEntity();
		
		if (!(entity instanceof EnderCrystal) || event.getEntity().getWorld().getEnvironment() != Environment.THE_END
				|| !entity.isInvulnerable()) return;
		
		event.setCancelled(true);
	}
}