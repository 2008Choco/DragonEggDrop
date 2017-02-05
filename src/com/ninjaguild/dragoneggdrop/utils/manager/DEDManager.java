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

package com.ninjaguild.dragoneggdrop.utils.manager;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.Iterables;
import com.ninjaguild.dragoneggdrop.DragonEggDrop;
import com.ninjaguild.dragoneggdrop.loot.LootManager;
import com.ninjaguild.dragoneggdrop.utils.DragonTemplate;
import com.ninjaguild.dragoneggdrop.utils.runnables.AnnounceRunnable;
import com.ninjaguild.dragoneggdrop.utils.runnables.RespawnRunnable;
import com.ninjaguild.dragoneggdrop.utils.versions.NMSAbstract;

import org.bukkit.Location;
import org.bukkit.World.Environment;
import org.bukkit.entity.EnderDragon;
import org.bukkit.scheduler.BukkitTask;

/**
 * The core Dragon Boss Battle manager. Boss battle manipulation and
 * loot generation mechanics are managed in this class
 */
public class DEDManager {

	private final DragonEggDrop plugin;
	
	private List<DragonTemplate> dragonTemplates;
	private LootManager lootMan;
	
	private BukkitTask respawnTask;
	private BukkitTask announceTask;
	
	private final int joinDelay;
	private final int deathDelay;
	
	private boolean respawnInProgress = false;
	
	/**
	 * Construct a new DEDManager object. This object should mainly be
	 * managed by the {@link DragonEggDrop} class
	 * 
	 * @param plugin - An instance of the DragonEggDrop plugin
	 */
	public DEDManager(final DragonEggDrop plugin) {
		this.plugin = plugin;
		
		this.dragonTemplates = DragonTemplate.loadTemplates(plugin.getConfig().getStringList("dragon-names"));
        this.setDragonBossBarTitle();
        
        this.lootMan = new LootManager(plugin);
        
        this.joinDelay = plugin.getConfig().getInt("join-respawn-delay", 60); // Seconds
        this.deathDelay = plugin.getConfig().getInt("death-respawn-delay", 300); // Seconds
	}
	
	/**
	 * Set the title of the boss bar to that of the dragon
	 */
	private void setDragonBossBarTitle() {
		NMSAbstract nmsAbstract = plugin.getNMSAbstract();
		
		plugin.getServer().getWorlds().stream()
			.filter(w -> w.getEnvironment() == Environment.THE_END)
			.forEach(w -> {
				Collection<EnderDragon> dragons = w.getEntitiesByClass(EnderDragon.class);
				if (!dragons.isEmpty()) {
					String dragonName = Iterables.get(dragons, 0).getCustomName();
					if (dragonName != null && !dragonName.isEmpty()) {
						nmsAbstract.setDragonBossBarTitle(dragonName, nmsAbstract.getEnderDragonBattleFromWorld(w));
					}
				}
			}
		);
	}
	
	public List<DragonTemplate> getDragonTemplates() {
		return dragonTemplates;
	}
	
	/**
	 * Get the main LootManager instance used to distribute loot
	 * 
	 * @return the LootManager instance
	 */
	public LootManager getLootManager() {
		return lootMan;
	}
	
	/**
	 * Commence the Dragon's respawning processes
	 * 
	 * @param eggLoc - The location in which the egg will spawn
	 * @param type - The type that triggered this dragon respawn
	 */
	public void startRespawn(Location eggLoc, RespawnType type) {
		boolean dragonExists = !eggLoc.getWorld().getEntitiesByClasses(EnderDragon.class).isEmpty();
		if (dragonExists || respawnInProgress) {
			return;
		}
		
		if (respawnTask == null || 
				(!plugin.getServer().getScheduler().isCurrentlyRunning(respawnTask.getTaskId()) && 
				!plugin.getServer().getScheduler().isQueued(respawnTask.getTaskId()))) {
			int respawnDelay = (type == RespawnType.JOIN ? joinDelay : deathDelay) * 20;
			this.respawnTask = new RespawnRunnable(plugin, eggLoc).runTaskLater(plugin, respawnDelay);
			
			if (plugin.getConfig().getBoolean("announce-respawn", true)) {
				this.announceTask = new AnnounceRunnable(plugin, eggLoc.getWorld(), respawnDelay / 20).runTaskTimer(plugin, 0, 20);
			}
		}
	}
	
	/**
	 * Halt the Dragon respawning process, if any are currently running
	 */
	public void stopRespawn() {
		if (respawnTask != null) {
			respawnTask.cancel();
			respawnTask = null;
			
			if (plugin.getConfig().getBoolean("announce-respawn", true)) {
				cancelAnnounce();
			}
		}
	}
	
	/**
	 * Cancel the action bar announcement task
	 */
	public void cancelAnnounce() {
		if (announceTask != null) {
		    announceTask.cancel();
		    announceTask = null;
		}
	}
	
	/**
	 * Set whether a respawn is currently in progress or not
	 * 
	 * @param value - the respawn progress state
	 */
	public void setRespawnInProgress(boolean value) {
		respawnInProgress = value;
	}
	
	/**
	 * Check whether a respawn is currently in progress or not
	 * 
	 * @return true if actively respawning
	 */
	public boolean isRespawnInProgress() {
		return respawnInProgress;
	}
	
	/**
	 * The type of trigger that allowed an Ender Dragon to 
	 * commence its respawning process
	 */
	public enum RespawnType {
		
		/** A player joined the world */
		JOIN,
		
		/** The ender dragon has died */
		DEATH
	}
}