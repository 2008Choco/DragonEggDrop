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

import com.ninjaguild.dragoneggdrop.DragonEggDrop;
import com.ninjaguild.dragoneggdrop.utils.manager.DEDManager.RespawnType;
import com.ninjaguild.dragoneggdrop.utils.runnables.AnnounceRunnable;
import com.ninjaguild.dragoneggdrop.utils.runnables.RespawnRunnable;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.EnderDragon;
import org.bukkit.scheduler.BukkitTask;

/**
 * Represents a wrapped {@link World} object with {@link Environment#THE_END} to separate
 * the runnables present in each independent world. Allows for separation of DED respawns
 * 
 * @author Parker Hawke - 2008Choco
 */
public class EndWorldWrapper {
	
	private BukkitTask respawnTask;
	private BukkitTask announceTask;
	
	private boolean respawnInProgress = false;
	
	private final DragonEggDrop plugin;
	private final World world;
	
	public EndWorldWrapper(DragonEggDrop plugin, World world) {
		this.plugin = plugin;
		this.world = world;
		
		if (world.getEnvironment() != Environment.THE_END)
			throw new IllegalArgumentException("EndWorldWrapper worlds must be of environment \"THE_END\"");
	}
	
	/**
	 * Get the world represented by this wrapper
	 * 
	 * @return the represented world
	 */
	public World getWorld() {
		return world;
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
		
        int joinDelay = plugin.getConfig().getInt("join-respawn-delay", 60); // Seconds
        int deathDelay = plugin.getConfig().getInt("death-respawn-delay", 300); // Seconds
        
		if (respawnTask == null || 
				(!plugin.getServer().getScheduler().isCurrentlyRunning(respawnTask.getTaskId()) && 
				!plugin.getServer().getScheduler().isQueued(respawnTask.getTaskId()))) {
			int respawnDelay = (type == RespawnType.JOIN ? joinDelay : deathDelay) * 20;
			this.respawnTask = new RespawnRunnable(plugin, eggLoc).runTaskTimer(plugin, respawnDelay, 20);
			
			if (plugin.getConfig().getBoolean("announce-respawn", true)) {
				this.announceTask = new AnnounceRunnable(plugin, this, respawnDelay / 20).runTaskTimer(plugin, 0, 20);
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
}