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

package com.ninjaguild.dragoneggdrop.utils.versions;

import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.EnderDragon;

/**
 * Represents a dragon battle in the End worlds
 * 
 * @author Parker Hawke - 2008Choco
 */
public interface DragonBattle {
	
	/**
	 * Set the title of the boss bar to a specific name
	 * 
	 * @param title the title to set
	 * @param battle the battle to modify
	 */
	public void setBossBarTitle(String title);
	
	/**
	 * Set the style and colour of the boss bar
	 * 
	 * @param style the style of the boss bar
	 * @param colour the colour of the boss bar
	 * 
	 * @return true if the style change was successful
	 */
	public boolean setBossBarStyle(BarStyle style, BarColor colour);
	
	/**
	 * Get the associated EnderDragon participating in this battle
	 * 
	 * @return the dragon
	 */
	public EnderDragon getEnderDragon();
	
	/**
	 * Set the state of the battle to its respawn state, and restart the battle 
	 * once again
	 */
	public void respawnEnderDragon();
	
	/**
	 * Get the Location of the end portal to the overworld
	 * 
	 * @return the portal location
	 */
	public Location getEndPortalLocation();
	
}