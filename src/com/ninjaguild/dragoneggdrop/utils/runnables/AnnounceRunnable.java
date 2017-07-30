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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.ninjaguild.dragoneggdrop.DragonEggDrop;
import com.ninjaguild.dragoneggdrop.management.EndWorldWrapper;

import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Represents a BukkitRunnable that permits action bar announcements
 * to be displayed to the player with an alternating colour
 */
public class AnnounceRunnable extends BukkitRunnable {
	
	private final DragonEggDrop plugin;
	private final EndWorldWrapper worldWrapper;
	private int delay;

	private int currentMessage = 0;
	private List<String> messages = new ArrayList<>();
	
	/**
	 * Construct a new AnnouncementRunnable object
	 * 
	 * @param plugin an instance of the DragonEggDrop plugin
	 * @param worldWrapper the world to broadcast the announcements to
	 * @param delay the time it will take the Ender Dragon to respawn (and terminate this runnable)
	 */
	public AnnounceRunnable(final DragonEggDrop plugin, final EndWorldWrapper worldWrapper, final int delay) {
		this.plugin = plugin;
		this.worldWrapper = worldWrapper;
		this.delay = delay + 1;
		this.messages = plugin.getConfig().getStringList("announce-messages").stream()
				.map(s -> ChatColor.translateAlternateColorCodes('&', s))
				.collect(Collectors.toList());
	}
	
	@Override
	public void run() {
		if (messages.size() == 0) return;
		if (this.currentMessage >= messages.size()) this.currentMessage = 0;
		
		if (--delay == 0) {
			this.worldWrapper.cancelAnnounce();
			return;
		}
		
		String message = messages.get(currentMessage++).replace("%time%", String.valueOf(delay)).replace("%formatted-time%", this.getFormattedTime(delay));
		plugin.getNMSAbstract().broadcastActionBar(message, worldWrapper.getWorld());
	}
	
	private String getFormattedTime(int timeInSeconds) {
		StringBuilder resultTime = new StringBuilder();
		
		if (timeInSeconds >= 3600) { // Hours
			int hours = (int) (Math.floor(timeInSeconds / 3600));
			resultTime.append(hours + " hours, ");
			timeInSeconds -= hours * 3600;
		}
		if (timeInSeconds >= 60) { // Minutes
			int minutes = (int) (Math.floor(timeInSeconds / 60));
			resultTime.append(minutes + " minutes, ");
			timeInSeconds -= minutes * 60;
		}
		if (timeInSeconds >= 1) resultTime.append(timeInSeconds + " seconds, ");
		
		return resultTime.substring(0, resultTime.length() - (resultTime.length() < 2 ? 0 : 2));
	}

}