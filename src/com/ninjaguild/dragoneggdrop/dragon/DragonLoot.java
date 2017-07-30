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

package com.ninjaguild.dragoneggdrop.dragon;

import com.ninjaguild.dragoneggdrop.utils.RandomCollection;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

/**
 * Represents a dragon's loot information (similar to a loot table) which may 
 * randomly spawn after the dragon's death
 * 
 * @author Parker Hawke - 2008Choco
 */
public class DragonLoot {
	
	private final DragonTemplate template;
	private final ConfigurationSection lootSection;
	
	private final RandomCollection<ItemStack> loot = new RandomCollection<>();
	
	private double eggSpawnChance = 100.0;
	private String eggName = "%dragon%'s &rEgg";
	
	private int minLootGen = 0, maxLootGen = 0;
	private double chestSpawnChance = 0.0;
	private String chestName = "Loot Chest";
	
	/**
	 * Construct a new DragonLoot
	 * 
	 * @param template the parent dragon template
	 * @param lootSection the loot section to parse
	 */
	public DragonLoot(DragonTemplate template, ConfigurationSection lootSection) {
		this.template = template;
		this.lootSection = lootSection;
		
		this.parseDragonLoot();
	}
	
	/**
	 * Get the template that holds this dragon loot
	 * 
	 * @return the parent dragon template
	 */
	public DragonTemplate getTemplate() {
		return template;
	}
	
	/**
	 * Get a copy of the loot to be generated in a chest
	 * 
	 * @return the random loot collection
	 */
	public RandomCollection<ItemStack> getLoot() {
		return RandomCollection.copyOf(loot);
	}
	
	/**
	 * Check whether a dragon's egg may be spawned or not
	 * 
	 * @return true if an egg may be spawned, false otherwise
	 */
	public boolean canSpawnEgg() {
		return eggSpawnChance > 0;
	}
	
	/**
	 * Get the chance that an egg will spawn. If {@link #getChestSpawnChance()} is
	 * greater than 0.0%, then the egg will spawn within the chest given that the
	 * egg spawn percentage has been met.
	 * 
	 * @return the egg spawn chance
	 */
	public double getEggSpawnChance() {
		return eggSpawnChance;
	}
	
	/**
	 * Get the name to be displayed on the dragon egg
	 * 
	 * @return the name to be displayed on the dragon egg
	 */
	public String getEggName() {
		return eggName;
	}
	
	/**
	 * Get the minimum amount of loot to generate in the chest
	 * 
	 * @return the minimum loot count
	 */
	public int getMinLootGen() {
		return minLootGen;
	}
	
	/**
	 * Get the maximum amount of loot to generate in the chest
	 * 
	 * @return the maximum loot count
	 */
	public int getMaxLootGen() {
		return maxLootGen;
	}
	
	/**
	 * Get the chance that a chest will spawn in place of an egg
	 * 
	 * @return the chest spawn chance
	 */
	public double getChestSpawnChance() {
		return chestSpawnChance;
	}
	
	/**
	 * Get the name that will be displayed within the Chest
	 * 
	 * @return the chest's name
	 */
	public String getChestName() {
		return chestName;
	}
	
	/**
	 * Check whether a chest may be spawned or not
	 * 
	 * @return true if a chest may be spawned, false otherwise
	 */
	public boolean canSpawnChest() {
		return chestSpawnChance > 0;
	}
	
	private void parseDragonLoot() {
		this.lootSection.get(""); // TODO
	}
	
}