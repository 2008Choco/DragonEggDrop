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

package com.ninjaguild.dragoneggdrop.versions;

import org.bukkit.World;
import org.bukkit.block.Chest;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;

/**
 * An abstract implementation of necessary net.minecraft.server and
 * org.bukkit.craftbukkit methods that vary between versions causing
 * version dependencies. Allows for version independency through
 * abstraction per Bukkit/Spigot release
 * 
 * @author Parker Hawke - 2008Choco
 */
public interface NMSAbstract {
	
	/**
	 * Get an EnderDragonBattle object based on the given world
	 * 
	 * @param world the world to retrieve a battle from
	 * @return the resulting dragon battle
	 */
	public DragonBattle getEnderDragonBattleFromWorld(World world);
	
	/**
	 * Get an EnderDragonBattle object based on a specific Ender Dragon
	 * 
	 * @param dragon the dragon to retrieve a battle from
	 * @return the resulting dragon battle
	 */
	public DragonBattle getEnderDragonBattleFromDragon(EnderDragon dragon);
	
	/**
	 * Check whether the dragon has been previously killed or not
	 * 
	 * @param dragon the dragon to check
	 * @return true if the dragon has been previously killed
	 */
	public boolean hasBeenPreviouslyKilled(EnderDragon dragon);
	
	/**
	 * Get the Ender Dragon's current death animation time
	 * 
	 * @param dragon the dragon to check
	 * @return the animation time
	 */
	public int getEnderDragonDeathAnimationTime(EnderDragon dragon);
	
	/**
	 * Set the custom name of a chest tile entity
	 * 
	 * @param chest the chest to set the name of
	 * @param name the name to set the chest to
	 */
	public void setChestName(Chest chest, String name);
	
	/**
	 * Send an action bar to a list of given players
	 * 
	 * @param message the message to send
	 * @param players the players to send the message to
	 */
	public void sendActionBar(String message, Player... players);
	
	/**
	 * Send an action bar to all players in a given world
	 * 
	 * @param message the message to send
	 * @param world the world to broadcast the message to
	 */
	public void broadcastActionBar(String message, World world);
	
}