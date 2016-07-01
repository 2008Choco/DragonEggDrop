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

package com.ninjaguild.dragoneggdrop;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.craftbukkit.v1_10_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftEnderDragon;
import org.bukkit.entity.EnderDragon;
import org.bukkit.scheduler.BukkitTask;

import net.minecraft.server.v1_10_R1.BossBattleServer;
import net.minecraft.server.v1_10_R1.ChatMessage;
import net.minecraft.server.v1_10_R1.EnderDragonBattle;
import net.minecraft.server.v1_10_R1.PacketPlayOutBoss;
import net.minecraft.server.v1_10_R1.WorldProviderTheEnd;

public class DEDManager {

	private final DragonEggDrop plugin;
	
	private List<String> dragonNames = null;
	private LootManager lootMan = null;
	
	private BukkitTask respawnTask = null;
	private BukkitTask announceTask = null;
	
	private final int joinDelay;
	private final int deathDelay;
	
	private boolean respawnInProgress = false;
	
	public DEDManager(final DragonEggDrop plugin) {
		this.plugin = plugin;
		
		dragonNames = plugin.getConfig().getStringList("dragon-names");
        setDragonBossBarTitle();
        
        lootMan = new LootManager(plugin);
        
		joinDelay = plugin.getConfig().getInt("join-respawn-delay", 60);//seconds
		deathDelay = plugin.getConfig().getInt("death-respawn-delay", 300);//seconds
	}
	
	private void setDragonBossBarTitle() {
		for (World world : plugin.getServer().getWorlds()) {
			if (world.getEnvironment() == Environment.THE_END) {
				Collection<EnderDragon> dragons = world.getEntitiesByClass(EnderDragon.class);
				if (!dragons.isEmpty()) {
					String dragonName = dragons.iterator().next().getCustomName();
					if (dragonName != null && !dragonName.isEmpty()) {
						setDragonBossBarTitle(dragonName, getEnderDragonBattleFromWorld(world));
					}
				}
			}
		}
	}

	protected void setDragonBossBarTitle(String title, EnderDragonBattle battle) {
		try {
			Field f = EnderDragonBattle.class.getDeclaredField("c");
			f.setAccessible(true);
			BossBattleServer battleServer = (BossBattleServer)f.get(battle);
			battleServer.title = new ChatMessage(title, new Object[0]);
			battleServer.sendUpdate(PacketPlayOutBoss.Action.UPDATE_NAME);
			f.setAccessible(false);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	protected EnderDragonBattle getEnderDragonBattleFromWorld(World world) {
		return ((WorldProviderTheEnd)((CraftWorld)world).getHandle().worldProvider).s();
	}

	protected EnderDragonBattle getEnderDragonBattleFromDragon(EnderDragon dragon) {
		return ((CraftEnderDragon)dragon).getHandle().cZ();
	}
	
	protected List<String> getDragonNames() {
		return dragonNames;
	}
	
	protected LootManager getLootManager() {
		return lootMan;
	}
	
	protected void startRespawn(Location eggLoc, RespawnType type) {
		boolean dragonExists = !eggLoc.getWorld().getEntitiesByClasses(EnderDragon.class).isEmpty();
		if (dragonExists || respawnInProgress) {
			return;
		}
		
		if (respawnTask == null || 
				(!plugin.getServer().getScheduler().isCurrentlyRunning(respawnTask.getTaskId()) && 
				!plugin.getServer().getScheduler().isQueued(respawnTask.getTaskId()))) {
			int respawnDelay = ((type == RespawnType.JOIN) ? joinDelay : deathDelay) * 20;
			respawnTask = Bukkit.getScheduler().runTaskLater(plugin, new RespawnRunnable(plugin, eggLoc), respawnDelay);
			
			if (plugin.getConfig().getBoolean("announce-respawn", true)) {
				announceTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin,
						new AnnounceRunnable(plugin, eggLoc.getWorld(), respawnDelay / 20), 0L, 20L);
			}
		}
	}
	
	protected void stopRespawn() {
		if (respawnTask != null) {
			respawnTask.cancel();
			respawnTask = null;
			
			if (plugin.getConfig().getBoolean("announce-respawn", true)) {
				cancelAnnounce();
			}
		}
	}
	
	protected void cancelAnnounce() {
		if (announceTask != null) {
		    announceTask.cancel();
		    announceTask = null;
		}
	}
	
	protected void setRespawnInProgress(boolean value) {
		respawnInProgress = value;
	}
	
	protected boolean isRespawnInProgress() {
		return respawnInProgress;
	}
	
	protected enum RespawnType {
		JOIN,
		DEATH
	}
	
}
