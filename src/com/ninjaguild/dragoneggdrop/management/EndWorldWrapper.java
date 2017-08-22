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
import com.ninjaguild.dragoneggdrop.management.DEDManager.RespawnType;
import com.ninjaguild.dragoneggdrop.utils.runnables.RespawnRunnable;
import com.ninjaguild.dragoneggdrop.versions.DragonBattle;
import com.ninjaguild.dragoneggdrop.versions.NMSAbstract;

import org.bukkit.Bukkit;
import org.bukkit.Location;
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
	
	private final DragonEggDrop plugin;
	private final UUID world;
	
	/**
	 * Construct a new EndWorldWrapper around an existing world
	 * 
	 * @param plugin the plugin instance
	 * @param world the world to wrap
	 */
	public EndWorldWrapper(DragonEggDrop plugin, World world) {
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
		boolean dragonExists = !this.getWorld().getEntitiesByClasses(EnderDragon.class).isEmpty();
		if (dragonExists || respawnInProgress || respawnTask != null) return;
		
        NMSAbstract nmsAbstract = plugin.getNMSAbstract();
        DragonBattle dragonBattle = nmsAbstract.getEnderDragonBattleFromWorld(this.getWorld());
        Location portalLocation = dragonBattle.getEndPortalLocation();
        
        FileConfiguration config = plugin.getConfig();
		int respawnDelay = (type == RespawnType.JOIN ? config.getInt("join-respawn-delay", 60) : config.getInt("death-respawn-delay", 300));
		
		this.respawnTask = new RespawnRunnable(plugin, portalLocation, respawnDelay, plugin.getConfig().getBoolean("announce-respawn", true));
		this.respawnTask.runTaskTimer(plugin, 0, 20);
		this.respawnInProgress = true;
	}
	
	public void startRespawn(int respawnDelay, boolean announceRespawn) {
		boolean dragonExists = !this.getWorld().getEntitiesByClass(EnderDragon.class).isEmpty();
		if (dragonExists || respawnInProgress || respawnTask != null) return;
		
		NMSAbstract nmsAbstract = plugin.getNMSAbstract();
		DragonBattle battle = nmsAbstract.getEnderDragonBattleFromWorld(getWorld());
		Location portalLocation = battle.getEndPortalLocation();
		
		this.respawnTask = new RespawnRunnable(plugin, portalLocation, respawnDelay, announceRespawn);
		this.respawnTask.runTaskTimer(plugin, 0, 20);
		this.respawnInProgress = true;
	}
	
	/**
	 * Halt the Dragon respawning process if any are currently running
	 */
	public void stopRespawn() {
		if (respawnTask != null) {
			this.respawnTask.cancel();
			this.respawnTask = null;
			this.respawnInProgress = false;
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
}