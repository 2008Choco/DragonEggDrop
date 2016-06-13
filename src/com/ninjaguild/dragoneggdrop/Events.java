package com.ninjaguild.dragoneggdrop;

import java.util.List;
import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftEnderDragon;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import com.ninjaguild.dragoneggdrop.DEDManager.RespawnType;

import net.minecraft.server.v1_10_R1.EntityEnderDragon;

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
			plugin.getDEDManager().setRespawnInProgress(false);
			
			if (!plugin.getDEDManager().getDragonNames().isEmpty()) {
				String name = ChatColor.translateAlternateColorCodes('&', 
						plugin.getDEDManager().getDragonNames().get(
                                rand.nextInt(plugin.getDEDManager().getDragonNames().size())));
				e.getEntity().setCustomName(name);
				plugin.getDEDManager().setDragonBossBarTitle(name,
						plugin.getDEDManager().getEnderDragonBattleFromDragon((EnderDragon)e.getEntity()));
			}
		}
	}
	
	@EventHandler
	private void onDragonDeath(EntityDeathEvent e) {
		if (e.getEntityType() == EntityType.ENDER_DRAGON) {
			EntityEnderDragon nmsDragon = ((CraftEnderDragon)e.getEntity()).getHandle();
			//get if the dragon has been previously killed
			boolean prevKilled = plugin.getDEDManager().getEnderDragonBattleFromDragon((EnderDragon)e.getEntity()).d();
			World world = e.getEntity().getWorld();

			new BukkitRunnable() {
				@Override
				public void run() {
					if (nmsDragon.bH >= 185) {//dragon is dead at 200
						cancel();
						plugin.getServer().getScheduler().runTask(plugin,
                                new DragonDeathRunnable(plugin, world, prevKilled));
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
	
	@EventHandler
	public void onPlayerSwitchWorlds(PlayerChangedWorldEvent e) {
		if (!plugin.getConfig().getBoolean("respawn", false)) {
			return;
		}
		
		if (e.getFrom().getEnvironment() == Environment.THE_END) {
			if (e.getFrom().getPlayers().size() == 0) {
				//cancel respawn if scheduled
				plugin.getDEDManager().stopRespawn();
			}
		}
		else if (e.getPlayer().getWorld().getEnvironment() == Environment.THE_END) {
			if (e.getPlayer().getWorld().getPlayers().size() == 1) {
				//schedule respawn, if not dragon exists, or in progress
				int y = e.getPlayer().getWorld().getMaxHeight();
				for (; y > 0; y--) {
					Block block = e.getPlayer().getWorld().getBlockAt(new Location(e.getPlayer().getWorld(), 0D, y, 0D));
					if (block.getType() == Material.BEDROCK) {
						plugin.getDEDManager().startRespawn(block.getLocation().add(0.5D, 1D, 0.5D), RespawnType.JOIN);
						break;
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		if (!plugin.getConfig().getBoolean("respawn", false)) {
			return;
		}
		
		if (e.getPlayer().getWorld().getEnvironment() == Environment.THE_END) {
			if (e.getPlayer().getWorld().getPlayers().size() == 0) {
				int y = e.getPlayer().getWorld().getMaxHeight();
				for (; y > 0; y--) {
					Block block = e.getPlayer().getWorld().getBlockAt(new Location(e.getPlayer().getWorld(), 0D, y, 0D));
					if (block.getType() == Material.BEDROCK) {
						plugin.getDEDManager().startRespawn(block.getLocation().add(0.5D, 1D, 0.5D), RespawnType.JOIN);
						break;
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent e) {
		if (!plugin.getConfig().getBoolean("respawn", false)) {
			return;
		}
		
		if (e.getPlayer().getWorld().getEnvironment() == Environment.THE_END) {
			if (e.getPlayer().getWorld().getPlayers().size() == 1) {
				plugin.getDEDManager().stopRespawn();
			}
		}
	}
	
	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		if (e.isCancelled()) {
			return;
		}
		
		if (e.getEntityType() == EntityType.ENDER_CRYSTAL) {
			if (e.getEntity().isInvulnerable() &&
					e.getEntity().getWorld().getEnvironment() == Environment.THE_END) {
				e.setCancelled(true);
			}
		}
	}
}
