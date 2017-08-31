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
import com.ninjaguild.dragoneggdrop.management.DEDManager;
import com.ninjaguild.dragoneggdrop.management.DEDManager.RespawnType;
import com.ninjaguild.dragoneggdrop.management.EndWorldWrapper;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class RespawnListeners implements Listener {
	
	private static final String RESOURCE_PAGE = "https://www.spigotmc.org/resources/dragoneggdrop-revival.35570/";
	
	private final DragonEggDrop plugin;
	private final DEDManager manager;
	
	public RespawnListeners(DragonEggDrop plugin) {
		this.plugin = plugin;
		this.manager = plugin.getDEDManager();
	}
	
	@EventHandler
	public void onPlayerSwitchWorlds(PlayerChangedWorldEvent event) {
		World world = event.getPlayer().getWorld();
		if (world.getEnvironment() != Environment.THE_END) return;
		
		EndWorldWrapper worldWrapper = manager.getWorldWrapper(world);
		
		// Start the respawn countdown if joining an empty world
		if (plugin.getConfig().getBoolean("respawn-on-join", false)) {
			if (world.getPlayers().size() > 1 || worldWrapper.isRespawnInProgress()
					|| world.getEntitiesByClass(EnderDragon.class).size() == 0) 
				return;
			
			manager.getWorldWrapper(world).startRespawn(RespawnType.JOIN);
		}
		
		// Reset end crystal states just in case something went awry
		if (!worldWrapper.isRespawnInProgress()) {
			world.getEntitiesByClass(EnderCrystal.class).forEach(e -> {
				e.setInvulnerable(false);
				e.setBeamTarget(null);
			});
		}
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		// Version notification
		Player player = event.getPlayer();
		if (player.isOp() && plugin.isNewVersionAvailable()) {
			this.plugin.sendMessage(player, ChatColor.GRAY + "A new version is available for download (Version " + this.plugin.getNewVersion() + "). " + RESOURCE_PAGE);
		}
		
		World world = player.getWorld();
		if (world.getEnvironment() != Environment.THE_END) return;
		
		EndWorldWrapper worldWrapper = manager.getWorldWrapper(world);
		
		// Reset end crystal states just in case something went awry
		if (!worldWrapper.isRespawnInProgress()) {
			world.getEntitiesByClass(EnderCrystal.class).forEach(e -> {
				e.setInvulnerable(false);
				e.setBeamTarget(null);
			});
		}
		
		// Dragon respawn logic
		if (!plugin.getConfig().getBoolean("respawn-on-join", false)) return;
		if (!world.getPlayers().isEmpty() || manager.getWorldWrapper(world).isRespawnInProgress()
				|| world.getEntitiesByClass(EnderDragon.class).size() == 0) 
			return;
		
		manager.getWorldWrapper(world).startRespawn(RespawnType.JOIN);
	}
	
}