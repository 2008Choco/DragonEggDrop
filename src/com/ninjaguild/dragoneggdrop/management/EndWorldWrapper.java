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

package com.ninjaguild.dragoneggdrop.management;

import java.util.UUID;

import com.ninjaguild.dragoneggdrop.DragonEggDrop;
import com.ninjaguild.dragoneggdrop.dragon.DragonTemplate;
import com.ninjaguild.dragoneggdrop.management.DEDManager.RespawnType;
import com.ninjaguild.dragoneggdrop.utils.runnables.RespawnRunnable;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EnderDragon;

/**
 * Represents a wrapped {@link World} object with {@link Environment#THE_END} to separate
 * the runnables present in each independent world. Allows for separation of DED respawns
 * 
 * @author Parker Hawke - 2008Choco
 */
public class EndWorldWrapper {
	
	private RespawnRunnable respawnTask;
	
	private boolean respawnInProgress = false;
	private DragonTemplate activeBattle;
	
	private final DragonEggDrop plugin;
	private final UUID world;
	
	/**
	 * Construct a new EndWorldWrapper around an existing world
	 * 
	 * @param plugin the plugin instance
	 * @param world the world to wrap
	 */
	protected EndWorldWrapper(DragonEggDrop plugin, World world) {
		this.plugin = plugin;
		this.world = world.getUID();
		
		if (world.getEnvironment() != Environment.THE_END)
			throw new IllegalArgumentException("EndWorldWrapper worlds must be of environment \"THE_END\"");
	}
	
	/**
	 * Get the world represented by this wrapper
	 * 
	 * @return the represented world
	 */
	public World getWorld() {
		return Bukkit.getWorld(world);
	}
	
	/**
	 * Commence the Dragon's respawning processes in this world
	 * 
	 * @param type the type that triggered this dragon respawn
	 */
	public void startRespawn(RespawnType type) {
		Validate.notNull(type, "Cannot respawn a dragon under a null respawn type");
		
		boolean dragonExists = !this.getWorld().getEntitiesByClasses(EnderDragon.class).isEmpty();
		if (dragonExists || respawnInProgress || respawnTask != null) return;
		
        FileConfiguration config = plugin.getConfig();
		int respawnDelay = (type == RespawnType.JOIN ? config.getInt("join-respawn-delay", 60) : config.getInt("death-respawn-delay", 300));
		
		this.respawnTask = new RespawnRunnable(plugin, getWorld(), respawnDelay);
		this.respawnTask.runTaskTimer(plugin, 0, 20);
		this.respawnInProgress = true;
	}
	
	/**
	 * Commence the Dragon's respawning processes in this world based 
	 * on provided values rather than configured ones.
	 * 
	 * @param respawnDelay the time until the dragon respawns
	 */
	public void startRespawn(int respawnDelay) {
		if (respawnDelay < 0) respawnDelay = 0;
		
		boolean dragonExists = !this.getWorld().getEntitiesByClass(EnderDragon.class).isEmpty();
		if (dragonExists || respawnInProgress || respawnTask != null) return;
		
		this.respawnTask = new RespawnRunnable(plugin, getWorld(), respawnDelay);
		this.respawnTask.runTaskTimer(plugin, 0, 20);
		this.respawnInProgress = true;
	}
	
	/**
	 * Halt the Dragon respawning process if any are currently running
	 */
	public void stopRespawn() {
		this.respawnInProgress = false;
		
		if (respawnTask != null) {
			this.respawnTask.cancel();
			this.respawnTask = null;	
		}
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
	 * Get the amount of time remaining until the dragon respawns
	 * 
	 * @return the time remaining (in seconds), or -1 if no time remaining at all
	 */
	public int getTimeUntilRespawn() {
		return (this.respawnTask != null ? this.respawnTask.getSecondsUntilRespawn() : -1);
	}
	

	/**
	 * Set the battle that is active according to DragonEggDrop. This battle
	 * instance will be used to generate names and lore for loot respectively
	 * 
	 * @param activeBattle the battle to set
	 */
	public void setActiveBattle(DragonTemplate activeBattle) {
		this.activeBattle = activeBattle;
	}
	
	/**
	 * Get the template represented in the active battle
	 * 
	 * @return the current battle
	 */
	public DragonTemplate getActiveBattle() {
		return activeBattle;
	}
	
}