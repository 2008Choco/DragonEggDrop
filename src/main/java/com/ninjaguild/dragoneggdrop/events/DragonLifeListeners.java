package com.ninjaguild.dragoneggdrop.events;

import java.util.Set;

import com.ninjaguild.dragoneggdrop.DragonEggDrop;
import com.ninjaguild.dragoneggdrop.api.BattleState;
import com.ninjaguild.dragoneggdrop.api.BattleStateChangeEvent;
import com.ninjaguild.dragoneggdrop.api.PortalCrystal;
import com.ninjaguild.dragoneggdrop.dragon.DragonTemplate;
import com.ninjaguild.dragoneggdrop.management.EndWorldWrapper;
import com.ninjaguild.dragoneggdrop.utils.runnables.DragonDeathRunnable;
import com.ninjaguild.dragoneggdrop.versions.DragonBattle;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class DragonLifeListeners implements Listener {
	
	private static final ItemStack END_CRYSTAL_ITEM = new ItemStack(Material.END_CRYSTAL);
	
	private final DragonEggDrop plugin;
	
	public DragonLifeListeners(DragonEggDrop plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onCreatureSpawn(CreatureSpawnEvent event) {
		if (!(event.getEntity() instanceof EnderDragon)) return;
		
		EnderDragon dragon = (EnderDragon) event.getEntity();
		if (dragon.getWorld().getEnvironment() != Environment.THE_END) return;
		
		DragonBattle dragonBattle = plugin.getNMSAbstract().getEnderDragonBattleFromDragon(dragon);
		EndWorldWrapper world = plugin.getDEDManager().getWorldWrapper(dragon.getWorld());
		
		if (plugin.getConfig().getBoolean("strict-countdown") && world.isRespawnInProgress()) {
			world.stopRespawn();
		}
		
		DragonTemplate template = plugin.getDEDManager().getRandomTemplate();
		if (template != null) {
			template.applyToBattle(plugin.getNMSAbstract(), dragon, dragonBattle);
			world.setActiveBattle(template);
			
			if (template.shouldAnnounceRespawn()) {
				Bukkit.getOnlinePlayers().forEach(p -> p.sendMessage(
						ChatColor.DARK_GRAY + "[" + ChatColor.RED.toString() + ChatColor.BOLD + "!!!" + ChatColor.DARK_GRAY + "] " 
						+ template.getName() + ChatColor.DARK_GRAY + " has respawned in the end!")
				);
			}
		}
		
		BattleStateChangeEvent bscEventCrystals = new BattleStateChangeEvent(dragonBattle, dragon, BattleState.DRAGON_RESPAWNING, BattleState.BATTLE_COMMENCED);
		Bukkit.getPluginManager().callEvent(bscEventCrystals);
	}
	
	@EventHandler
	public void onDragonDeath(EntityDeathEvent event) {
		if (!(event.getEntity() instanceof EnderDragon)) return;
		
		EnderDragon dragon = (EnderDragon) event.getEntity();
		DragonBattle dragonBattle = plugin.getNMSAbstract().getEnderDragonBattleFromDragon(dragon);
		
		World world = event.getEntity().getWorld();
		EndWorldWrapper worldWrapper = plugin.getDEDManager().getWorldWrapper(world);
		
		BattleStateChangeEvent bscEventCrystals = new BattleStateChangeEvent(dragonBattle, dragon, BattleState.BATTLE_COMMENCED, BattleState.BATTLE_END);
		Bukkit.getPluginManager().callEvent(bscEventCrystals);

		new BukkitRunnable() {
			@Override
			public void run() {
				if (plugin.getNMSAbstract().getEnderDragonDeathAnimationTime(dragon) >= 185) { // Dragon is dead at 200
					new DragonDeathRunnable(plugin, worldWrapper, dragon);
					this.cancel();
				}
			}
		}.runTaskTimer(plugin, 0L, 1L);
	}
	
	@EventHandler
	public void onAttemptRespawn(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		ItemStack item = event.getItem();
		
		if (item == null || item.getType() != Material.END_CRYSTAL) return;
		if (plugin.getConfig().getBoolean("allow-crystal-respawns")) return;
		
		World world = player.getWorld();
		EndWorldWrapper worldWrapper = plugin.getDEDManager().getWorldWrapper(world);
		if (worldWrapper.isRespawnInProgress() || !world.getEntitiesByClass(EnderDragon.class).isEmpty()) {
			Set<EnderCrystal> crystals = PortalCrystal.getAllSpawnedCrystals(world);
			
			// Check for 3 crystals because PlayerInteractEvent is fired first
			if (crystals.size() < 3) return;
			
			for (EnderCrystal crystal : crystals) {
				crystal.getLocation().getBlock().setType(Material.AIR); // Remove fire
				world.dropItem(crystal.getLocation(), END_CRYSTAL_ITEM);
				crystal.remove();
			}
			
			plugin.getNMSAbstract().sendActionBar(ChatColor.RED + "You cannot manually respawn a dragon!", player);
			player.sendMessage(ChatColor.RED + "You cannot manually respawn a dragon!");
			event.setCancelled(true);
		}
	}
	
}