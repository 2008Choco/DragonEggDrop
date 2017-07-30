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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.common.collect.Iterables;
import com.ninjaguild.dragoneggdrop.DragonEggDrop;
import com.ninjaguild.dragoneggdrop.dragon.DragonTemplate;
import com.ninjaguild.dragoneggdrop.loot.LootManager;
import com.ninjaguild.dragoneggdrop.versions.NMSAbstract;

import org.bukkit.World;
import org.bukkit.entity.EnderDragon;

/**
 * The core Dragon Boss Battle manager. Boss battle manipulation and
 * loot generation mechanics are managed in this class
 */
public class DEDManager {

	private final DragonEggDrop plugin;
	
	private List<DragonTemplate> dragonTemplates;
	private LootManager lootMan;
	
	private final Map<UUID, EndWorldWrapper> worldWrappers = new HashMap<>();
	
	/**
	 * Construct a new DEDManager object. This object should mainly be
	 * managed by the {@link DragonEggDrop} class
	 * 
	 * @param plugin an instance of the DragonEggDrop plugin
	 */
	public DEDManager(final DragonEggDrop plugin) {
		this.plugin = plugin;
		
		this.dragonTemplates = DragonTemplate.loadTemplates(plugin.getConfig().getStringList("dragon-names"));
        this.setDragonBossBarTitle();
        
        this.lootMan = new LootManager(plugin);
	}
	
	/**
	 * Set the title of the boss bar to that of the dragon
	 */
	private void setDragonBossBarTitle() {
		NMSAbstract nmsAbstract = plugin.getNMSAbstract();
		
		this.worldWrappers.values().stream()
			.map(EndWorldWrapper::getWorld)
			.forEach(w -> {
				Collection<EnderDragon> dragons = w.getEntitiesByClass(EnderDragon.class);
				if (!dragons.isEmpty()) {
					String dragonName = Iterables.get(dragons, 0).getCustomName();
					if (dragonName != null && !dragonName.isEmpty()) {
						nmsAbstract.getEnderDragonBattleFromWorld(w).setBossBarTitle(dragonName);
					}
				}
			}
		);
	}
	
	/**
	 * Get a list of all dragon templates
	 * 
	 * @return all dragon templates
	 */
	public List<DragonTemplate> getDragonTemplates() {
		return dragonTemplates;
	}
	
	/**
	 * Get the main LootManager instance used to distribute loot
	 * 
	 * @return the LootManager instance
	 */
	public LootManager getLootManager() {
		return lootMan;
	}
	
	/**
	 * Get the world wrapper for the specified world
	 * 
	 * @param world the world to get
	 * @return the world's respective wrapper
	 */
	public EndWorldWrapper getWorldWrapper(World world) {
		UUID worldId = world.getUID();
		if (!worldWrappers.containsKey(worldId))
			this.worldWrappers.put(worldId, new EndWorldWrapper(plugin, world));
		return this.worldWrappers.get(worldId);
	}
	
	/**
	 * Get the map containing all world wrappers
	 * 
	 * @return all world wrappers
	 */
	public Map<UUID, EndWorldWrapper> getWorldWrappers() {
		return worldWrappers;
	}
	
	/**
	 * The type of trigger that allowed an Ender Dragon to 
	 * commence its respawning process
	 */
	public enum RespawnType {
		
		/** 
		 * A player joined the world
		 */
		JOIN,
		
		/** 
		 * The ender dragon was killed
		 */
		DEATH
	}
}