package com.ninjaguild.dragoneggdrop;

import java.util.List;
import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftEnderDragon;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import net.minecraft.server.v1_9_R2.EntityEnderDragon;

public class Events implements Listener {
	
	private final DragonEggDrop plugin;
	private final Random rand;
	
	public Events(final DragonEggDrop plugin) {
		this.plugin = plugin;
		this.rand = new Random();
	}

	@EventHandler
	public void onCreatureSpawn(CreatureSpawnEvent e) {
		if (e.isCancelled()) {
			return;
		}
		
		if (e.getEntityType() == EntityType.ENDER_DRAGON) {
			if (!plugin.getDragonNames().isEmpty()) {
				String name = ChatColor.translateAlternateColorCodes('&', 
						plugin.getDragonNames().get(rand.nextInt(plugin.getDragonNames().size())));
				e.getEntity().setCustomName(name);
				plugin.setDragonBossBarTitle(name, plugin.getEnderDragonBattleFromDragon((EnderDragon)e.getEntity()));
			}
		}
	}
	
	@EventHandler
	private void onDragonDeath(EntityDeathEvent e) {
		if (e.getEntityType() == EntityType.ENDER_DRAGON) {
			EntityEnderDragon nmsDragon = ((CraftEnderDragon)e.getEntity()).getHandle();
			//get if the dragon has been previously killed
			boolean prevKilled = plugin.getEnderDragonBattleFromDragon((EnderDragon)e.getEntity()).d();
			World world = e.getEntity().getWorld();

			new BukkitRunnable() {
				@Override
				public void run() {
					if (nmsDragon.bG >= 185) {//dragon is dead at 200
						cancel();
						plugin.getServer().getScheduler().runTask(plugin, new DragonDeathRunnable(plugin, world, prevKilled));
					}
				}

			}.runTaskTimer(plugin, 0L, 1L);
		}
	}
	
	@EventHandler
	public void onItemSpawn(ItemSpawnEvent e) {
		Item item = e.getEntity();
		ItemStack stack = item.getItemStack();
		if (item.getItemStack().getType() == Material.DRAGON_EGG) {
			if (item.getWorld().getEnvironment() == Environment.THE_END) {
				if (!stack.hasItemMeta()) {
					ItemMeta eggMeta = stack.getItemMeta();
					
					String eggName = plugin.getConfig().getString("egg-name");
					List<String> eggLore = plugin.getConfig().getStringList("egg-lore");

					if (eggName != null && !eggName.isEmpty()) {
						eggMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', eggName));
					}
					if (eggLore != null && !eggLore.isEmpty()) {
						for (int i = 0; i < eggLore.size(); i++) {
							eggLore.set(i, ChatColor.translateAlternateColorCodes('&', eggLore.get(i)));
						}
						eggMeta.setLore(eggLore);
					}
					
					stack.setItemMeta(eggMeta);
				}
			}
		}
	}
	
}
