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

package com.ninjaguild.dragoneggdrop.utils.runnables;

import com.ninjaguild.dragoneggdrop.DragonEggDrop;
import com.ninjaguild.dragoneggdrop.utils.ActionBar;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Represents a BukkitRunnable that permits action bar announcements
 * to be displayed to the player with an alternating colour
 */
public class AnnounceRunnable extends BukkitRunnable {

	private final DragonEggDrop plugin;
	private final World world;
	private int delay;

	private String color1;
	private String color2;
	
	/**
	 * Construct a new AnnouncementRunnable object
	 * 
	 * @param plugin - An instance of the DragonEggDrop plugin
	 * @param world - The world to broadcast the announcements to
	 * @param delay - The time it will take the Ender Dragon to respawn (and terminate this runnable)
	 */
	public AnnounceRunnable(final DragonEggDrop plugin, final World world, final int delay) {
		this.plugin = plugin;
		this.world = world;
		this.delay = delay;
		
		color1 = ChatColor.translateAlternateColorCodes('&',
				plugin.getConfig().getString("announce-color-one", ChatColor.GOLD.toString()));
		color2 = ChatColor.translateAlternateColorCodes('&',
				plugin.getConfig().getString("announce-color-two", ChatColor.YELLOW.toString()));
	}
	
	@Override
	public void run() {
		String temp = color1;
		color1 = color2;
		color2 = temp;
		
		String message = color1 + "Dragon Respawn In " + color2 + (delay--) + color1 + " Seconds";
		ActionBar.sendToSome(world.getPlayers(), message);
		
		if (delay == 0) {
			plugin.getDEDManager().cancelAnnounce();
		}
	}

}
