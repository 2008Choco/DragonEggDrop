package com.ninjaguild.dragoneggdrop.management;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ninjaguild.dragoneggdrop.DragonEggDrop;
import com.ninjaguild.dragoneggdrop.dragon.DragonTemplate;
import com.ninjaguild.dragoneggdrop.management.DEDManager.RespawnType;
import com.ninjaguild.dragoneggdrop.utils.runnables.RespawnRunnable;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EnderDragon;

/**
 * Represents a wrapped {@link World} object with {@link Environment#THE_END} to separate
 * the runnables present in each independent world. Allows for separation of DED respawns.
 * 
 * @author Parker Hawke - 2008Choco
 */
public class EndWorldWrapper {
	
	private static final Pattern TIME_PATTERN = Pattern.compile("(\\d+)([wdhms])");
	
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
	 * Get the world represented by this wrapper.
	 * 
	 * @return the represented world
	 */
	public World getWorld() {
		return Bukkit.getWorld(world);
	}
	
	/**
	 * Commence the Dragon's respawning processes in this world.
	 * 
	 * @param type the type that triggered this dragon respawn
	 */
	public void startRespawn(RespawnType type) {
		Validate.notNull(type, "Cannot respawn a dragon under a null respawn type");
		
		boolean dragonExists = !this.getWorld().getEntitiesByClasses(EnderDragon.class).isEmpty();
		if (dragonExists || respawnInProgress || respawnTask != null) return;
		
        FileConfiguration config = plugin.getConfig();
        
        int respawnDelay = 0;
        
        switch (type)
        {
            case JOIN:
                respawnDelay = parseRespawnSeconds(config.getString("join-respawn-delay", "1m"));
                break;
            case DEATH:
                respawnDelay = parseRespawnSeconds(config.getString("death-respawn-delay", "5m"));
                break;
            case COMMAND:
                respawnDelay = parseRespawnSeconds(config.getString("command-respawn-delay", "1m"));
                break;
        }
        
		this.respawnTask = new RespawnRunnable(plugin, getWorld(), respawnDelay);
		this.respawnTask.runTaskTimer(plugin, 0, 20);
		this.respawnInProgress = true;
	}
	
	/**
	 * Commence the Dragon's respawning processes in this world based on provided
	 * values rather than configured ones.
	 * 
	 * @param respawnDelay the time until the dragon respawns
	 */
	public void startRespawn(int respawnDelay) {
		respawnDelay = Math.max(respawnDelay, 0);
		
		boolean dragonExists = !this.getWorld().getEntitiesByClass(EnderDragon.class).isEmpty();
		if (dragonExists || respawnInProgress || respawnTask != null) return;
		
		this.respawnTask = new RespawnRunnable(plugin, getWorld(), respawnDelay);
		this.respawnTask.runTaskTimer(plugin, 0, 20);
		this.respawnInProgress = true;
	}
	
	/**
	 * Halt the Dragon respawning process if any are currently running.
	 */
	public void stopRespawn() {
		this.respawnInProgress = false;
		
		if (respawnTask != null) {
			this.respawnTask.cancel();
			this.respawnTask = null;
		}
	}
	
	/**
	 * Check whether a respawn is currently in progress or not.
	 * 
	 * @return true if actively respawning
	 */
	public boolean isRespawnInProgress() {
		return respawnInProgress;
	}

	/**
	 * Get the amount of time remaining until the dragon respawns.
	 * 
	 * @return the time remaining (in seconds), or -1 if no time remaining at all
	 */
	public int getTimeUntilRespawn() {
		return (respawnTask != null ? respawnTask.getSecondsUntilRespawn() : -1);
	}
	

	/**
	 * Set the battle that is active according to DragonEggDrop. This battle
	 * instance will be used to generate names and lore for loot respectively.
	 * 
	 * @param activeBattle the battle to set
	 */
	public void setActiveBattle(DragonTemplate activeBattle) {
		this.activeBattle = activeBattle;
	}
	
	/**
	 * Get the template represented in the active battle.
	 * 
	 * @return the current battle
	 */
	public DragonTemplate getActiveBattle() {
		return activeBattle;
	}
	
	private int parseRespawnSeconds(String value) {
		// Handle legacy (i.e. no timestamps... for example, just "600")
		int legacyTime = NumberUtils.toInt(value, -1);
		if (legacyTime != -1) {
			return legacyTime;
		}
		
		int seconds = 0;
		
		Matcher matcher = TIME_PATTERN.matcher(value);
		while (matcher.find()) {
			int amount = NumberUtils.toInt(matcher.group(1));
			
			switch (matcher.group(2)) {
				case "w":
					seconds += TimeUnit.DAYS.toSeconds(amount * 7);
					break;
				case "d":
					seconds += TimeUnit.DAYS.toSeconds(amount);
					break;
				case "h":
					seconds += TimeUnit.HOURS.toSeconds(amount);
					break;
				case "m":
					seconds += TimeUnit.MINUTES.toSeconds(amount);
					break;
				case "s":
					seconds += amount;
					break;
			}
		}
		
		return seconds;
	}
	
}