package com.ninjaguild.dragoneggdrop.versions;

import org.bukkit.World;
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

}