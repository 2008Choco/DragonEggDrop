/*
    DragonEggDrop
    Copyright (C) 2016  NinjaStix
    ninjastix84@gmail.com

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.ninjaguild.dragoneggdrop.events;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.Iterables;
import com.ninjaguild.dragoneggdrop.DragonEggDrop;
import com.ninjaguild.dragoneggdrop.api.BattleState;
import com.ninjaguild.dragoneggdrop.api.BattleStateChangeEvent;
import com.ninjaguild.dragoneggdrop.dragon.DragonTemplate;
import com.ninjaguild.dragoneggdrop.management.EndWorldWrapper;
import com.ninjaguild.dragoneggdrop.utils.runnables.DragonDeathRunnable;
import com.ninjaguild.dragoneggdrop.versions.DragonBattle;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
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
		DragonBattle dragonBattle = plugin.getNMSAbstract().getEnderDragonBattleFromDragon(dragon);
		
		DragonTemplate template = plugin.getDEDManager().getRandomTemplate();
		if (template != null) {
			template.applyToBattle(plugin.getNMSAbstract(), dragon, dragonBattle);
			plugin.getDEDManager().getWorldWrapper(dragon.getWorld()).setActiveBattle(template);
			
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
			Set<EnderCrystal> crystals = this.getPortalCrystals(world);
			
			// Check for 3 crystals because PlayerInteractEvent is fired first
			if (crystals.size() < 3) return;
			
			for (EnderCrystal crystal : crystals) {
				world.dropItem(crystal.getLocation(), END_CRYSTAL_ITEM);
				crystal.remove();
			}
			
			event.setCancelled(true);
		}
	}
	
	private Set<EnderCrystal> getPortalCrystals(World world) {
		Set<EnderCrystal> crystals = new HashSet<>();
		
		if (world.getEnvironment() == Environment.THE_END) {
			Location portalLoc = plugin.getNMSAbstract().getEnderDragonBattleFromWorld(world).getEndPortalLocation();
			
			Location[] crystalLocs = {
				portalLoc.add(3, -3, 0),
				portalLoc.add(-3, -3, 0),
				portalLoc.add(0, -3, 3),
				portalLoc.add(0, -3, -3)
			};
			
			for (Location location : crystalLocs) {
				Collection<Entity> entities = world.getNearbyEntities(location, 0.5, 0.5, 0.5);
				if (entities.size() == 0) continue;
				
				Entity crystal = Iterables.find(entities, e -> e instanceof EnderCrystal);
				if (crystal == null) continue;
				
				crystals.add((EnderCrystal) crystal);
			}
		}
		
		return crystals;
	}
}