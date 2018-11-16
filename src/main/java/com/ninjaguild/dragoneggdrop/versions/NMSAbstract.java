package com.ninjaguild.dragoneggdrop.versions;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Chest;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;

/**
 * An abstract implementation of necessary net.minecraft.server and
 * org.bukkit.craftbukkit methods that vary between versions causing
 * version dependencies. Allows for version independency through
 * abstraction per Bukkit/Spigot release.
 * 
 * @author Parker Hawke - 2008Choco
 */
public interface NMSAbstract {
	
	/**
	 * Get an EnderDragonBattle object based on the given world.
	 * 
	 * @param world the world to retrieve a battle from
	 * 
	 * @return the resulting dragon battle
	 */
	public DragonBattle getEnderDragonBattleFromWorld(World world);
	
	/**
	 * Get an EnderDragonBattle object based on a specific Ender Dragon.
	 * 
	 * @param dragon the dragon to retrieve a battle from
	 * 
	 * @return the resulting dragon battle
	 */
	public DragonBattle getEnderDragonBattleFromDragon(EnderDragon dragon);
	
	/**
	 * Check whether the dragon has been previously killed or not.
	 * 
	 * @param dragon the dragon to check
	 * 
	 * @return true if the dragon has been previously killed
	 */
	public boolean hasBeenPreviouslyKilled(EnderDragon dragon);
	
	/**
	 * Get the Ender Dragon's current death animation time.
	 * 
	 * @param dragon the dragon to check
	 * 
	 * @return the animation time
	 */
	public int getEnderDragonDeathAnimationTime(EnderDragon dragon);
	
	/**
	 * Set the custom name of a chest tile entity
	 * 
	 * @param chest the chest to set the name of
	 * @param name the name to set the chest to
	 * 
	 * @deprecated replaced by Bukkit's {@link Chest#setCustomName(String)}
	 */
	@Deprecated
	public void setChestName(Chest chest, String name);
	
	/**
	 * Send an action bar to a list of given players.
	 * 
	 * @param message the message to send
	 * @param players the players to send the message to
	 */
	public void sendActionBar(String message, Player... players);
	
	/**
	 * Send an action bar to all players in a given world.
	 * 
	 * @param message the message to send
	 * @param world the world to broadcast the message to
	 */
	public void broadcastActionBar(String message, World world);
	
	/**
	 * Spawn particles in the world for all players. This method exists primarily to support a
	 * non-existent method in Spigot 1.13.0
	 * 
	 * @param particle the particle to spawn
	 * @param location the location at which to spawn the particle
	 * @param count the amount of particles to spawn
	 * @param xOffset the x offset to apply
	 * @param yOffset the y offset to apply
	 * @param zOffset the z offset to apply
	 * @param speed the particle's speed
	 * 
	 * @see World#spawnParticle(Particle, Location, int, double, double, double, double)
	 */
	public void spawnParticle(Particle particle, Location location, int count, double xOffset, double yOffset, double zOffset, double speed);
	
}