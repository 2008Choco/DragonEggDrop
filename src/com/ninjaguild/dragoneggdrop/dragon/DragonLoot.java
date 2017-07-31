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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.ninjaguild.dragoneggdrop.DragonEggDrop;
import com.ninjaguild.dragoneggdrop.utils.RandomCollection;

import org.apache.commons.lang3.EnumUtils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Represents a dragon's loot information (similar to a loot table) which may 
 * randomly spawn after the dragon's death
 * 
 * @author Parker Hawke - 2008Choco
 */
public class DragonLoot {
	
	private final FileConfiguration dragonFile;
	private final RandomCollection<ItemStack> loot = new RandomCollection<>();
	
	private double eggSpawnChance;
	private String eggName;
	private List<String> eggLore;
	
	private double chestSpawnChance;
	private String chestName;
	private int minLootGen, maxLootGen;
	
	/**
	 * Construct a new DragonLoot
	 * 
	 * @param lootSection the loot section to parse
	 */
	public DragonLoot(FileConfiguration dragonFile) {
		this.dragonFile = dragonFile;
		this.parseDragonLoot();
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
	 * @return the name display
	 */
	public String getEggName() {
		return eggName;
	}
	
	/**
	 * Get the lore to be displayed on the dragon egg
	 * 
	 * @return the lore to display
	 */
	public List<String> getEggLore() {
		return eggLore;
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
		Logger logger = JavaPlugin.getPlugin(DragonEggDrop.class).getLogger();
		
		// Parse the basic loot rewards (i.e. spawn chances & names)
		this.eggSpawnChance = dragonFile.getDouble("egg-spawn-chance", 100.0);
		this.eggName = dragonFile.getString("egg-name", "%dragon%&r's Egg");
		this.eggLore = dragonFile.getStringList("egg-lore");
		
		this.chestSpawnChance = dragonFile.getDouble("chest-spawn-chance", 0);
		this.chestName = dragonFile.getString("chest-name", "Loot Chest");
		this.minLootGen = dragonFile.getInt("min-loot");
		this.maxLootGen = dragonFile.getInt("max-loot");
		
		// Parse loot items
		ConfigurationSection lootSection = dragonFile.getConfigurationSection("loot");
		for (String itemKey : lootSection.getKeys(false)) {
			// Parse root values (type, damage, amount and weight)
			double weight = lootSection.getDouble(itemKey + ".weight");
			
			Material type = EnumUtils.getEnum(Material.class, lootSection.getString(itemKey + ".type").toUpperCase());
			short damage = (short) lootSection.getInt(itemKey + ".damage");
			int amount = lootSection.getInt(itemKey + ".amount");
			
			if (type == null) {
				logger.warning("Invalid material type \"" + lootSection.getString(itemKey + ".type") + "\". Ignoring loot value...");
				continue;
			}
			
			// Create new item stack with passed values
			ItemStack item = new ItemStack(type, damage);
			item.setAmount(amount);
			
			// Parse meta
			String displayName = lootSection.getString(itemKey + ".display-name");
			List<String> lore = lootSection.getStringList(itemKey + ".lore");
			Map<Enchantment, Integer> enchantments = new HashMap<>();
			
			// Enchantment parsing
			for (String enchant : lootSection.getConfigurationSection("enchantments").getKeys(false)) {
				Enchantment enchantment = Enchantment.getByName(enchant);
				int level = lootSection.getInt(itemKey + ".enchants." + enchant);
				
				if (enchantment == null || level == 0) {
					logger.warning("Invalid enchantment \"" + enchant + "\" with level " + level);
					continue;
				}
				
				enchantments.put(enchantment, level);
			}
			
			// Meta updating
			ItemMeta meta = item.getItemMeta();
			if (displayName != null) meta.setDisplayName(displayName);
			if (!lore.isEmpty()) meta.setLore(lore);
			enchantments.forEach((e, level) -> meta.addEnchant(e, level, true));
			item.setItemMeta(meta);
			
			this.loot.add(weight, item);
		}
	}
	
}