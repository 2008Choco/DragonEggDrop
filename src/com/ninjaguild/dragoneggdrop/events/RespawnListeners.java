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

package com.ninjaguild.dragoneggdrop.events;

import com.ninjaguild.dragoneggdrop.DragonEggDrop;
import com.ninjaguild.dragoneggdrop.utils.manager.DEDManager.RespawnType;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class RespawnListeners implements Listener {
	
	private static final String RESOURCE_PAGE = "https://www.spigotmc.org/resources/dragoneggdrop-revival.35570/";
	
	private final DragonEggDrop plugin;
	
	public RespawnListeners(DragonEggDrop plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onPlayerSwitchWorlds(PlayerChangedWorldEvent event) {
		if (!plugin.getConfig().getBoolean("respawn-on-join", false)
				&& !plugin.getConfig().getBoolean("respawn-on-death", true)) return;
		
		World fromWorld = event.getFrom(), toWorld = event.getPlayer().getWorld();
		
		if (fromWorld.getEnvironment() == Environment.THE_END) {
			if (fromWorld.getPlayers().isEmpty()) {
				// Cancel respawn if scheduled
				plugin.getDEDManager().getWorldWrapper(fromWorld).stopRespawn();
			}
		}
		
		if (plugin.getConfig().getBoolean("respawn-on-join", false)) {
			if (toWorld.getEnvironment() == Environment.THE_END && toWorld.getPlayers().size() == 1) {
				// Schedule respawn, if not dragon exists, or in progress
				for (int y = toWorld.getMaxHeight(); y > 0; y--) {
					Block block = toWorld.getBlockAt(0, y, 0);
					if (block.getType() == Material.BEDROCK) {
						plugin.getDEDManager().getWorldWrapper(toWorld).startRespawn(block.getLocation().add(0.5D, 1D, 0.5D), RespawnType.JOIN);
						break;
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		// Version notification
		Player player = event.getPlayer();
		if (player.isOp() && plugin.isNewVersionAvailable()) {
			this.plugin.sendMessage(player, ChatColor.GRAY + "A new version is available for download (Version " + this.plugin.getNewVersion() + "). " + RESOURCE_PAGE);
		}
		
		// Dragon respawn logic
		if (!plugin.getConfig().getBoolean("respawn-on-join", false)) return;
		
		World world = player.getWorld();
		if (world.getEnvironment() != Environment.THE_END || !world.getPlayers().isEmpty()) return;
		
		for (int y = world.getMaxHeight(); y > 0; y--) {
			Block block = world.getBlockAt(0, y, 0);
			if (block.getType() == Material.BEDROCK) {
				plugin.getDEDManager().getWorldWrapper(world).startRespawn(block.getLocation().add(0.5D, 1D, 0.5D), RespawnType.JOIN);
				break;
			}
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent event) {
		if (!plugin.getConfig().getBoolean("respawn-on-join", false)
				&& !plugin.getConfig().getBoolean("respawn-on-death", true)) return;
		
		World world = event.getPlayer().getWorld();
		if (world.getEnvironment() != Environment.THE_END || world.getPlayers().size() != 1) return;
		
		plugin.getDEDManager().getWorldWrapper(world).stopRespawn();
	}
}