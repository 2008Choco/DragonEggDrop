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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;
import com.ninjaguild.dragoneggdrop.DragonEggDrop;
import com.ninjaguild.dragoneggdrop.utils.RandomCollection;
import com.ninjaguild.dragoneggdrop.versions.DragonBattle;
import com.ninjaguild.dragoneggdrop.versions.NMSAbstract;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang3.EnumUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EnderDragon;
import org.bukkit.inventory.Inventory;
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
	
	private static final Random RANDOM = new Random();
	private static final NMSAbstract NMS_ABSTRACT = JavaPlugin.getPlugin(DragonEggDrop.class).getNMSAbstract();
	
	private final DragonTemplate template;
	private final RandomCollection<ItemStack> loot = new RandomCollection<>();
	
	private double eggSpawnChance = 100.0;
	private String eggName = "%dragon%'s Egg";
	private List<String> eggLore = new ArrayList<>();
	
	private double chestSpawnChance = 0.0;
	private String chestName = "Loot Chest";
	private int minLootGen = 3, maxLootGen = 6;
	
	/**
	 * Construct a new DragonLoot
	 * 
	 * @param template the parent template for this loot
	 */
	public DragonLoot(DragonTemplate template) {
		this.template = template;
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
	 * Add a loot item to the random loot collection
	 * 
	 * @param item the item to add
	 * @param weight the generation weight of the item
	 * @param updateFile whether to update the dragon file or not
	 */
	@SuppressWarnings("deprecation")
	public void addLootItem(ItemStack item, double weight, boolean updateFile) {
		Validate.notNull(item, "Cannot add null ItemStack to loot");
		if (weight < 0) weight = 0;
		
		this.loot.add(weight, item);
		
		if (updateFile && template.configFile != null) {
			FileConfiguration config = template.configFile;
			int itemID = loot.size();
			
			config.set("loot." + itemID + ".weight", weight);
			config.set("loot." + itemID + ".type", item.getType().name());
			if (item.getData().getData() != 0) config.set("loot." + itemID + ".data", item.getData().getData());
			if (item.getDurability() != 0) config.set("loot." + itemID + ".damage", item.getDurability());
			config.set("loot." + itemID + ".amount", item.getAmount());
			
			if (item.hasItemMeta()) {
				ItemMeta meta = item.getItemMeta();
				
				if (meta.hasDisplayName()) config.set("loot." + itemID + ".display-name", meta.getDisplayName());
				if (meta.hasLore()) config.set("loot." + itemID + ".lore", meta.getLore());
				if (meta.hasEnchants()) {
					for (Enchantment enchant : meta.getEnchants().keySet()) {
						config.set("loot." + itemID + ".enchantments." + enchant.getName(), meta.getEnchantLevel(enchant));
					}
				}
			}
			
			try {
				config.save(template.file);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Add a loot item to the random loot collection and update the dragon file
	 * (if one exists)
	 * 
	 * @param item the item to add
	 * @param weight the generation weight of the item
	 */
	public void addLootItem(ItemStack item, double weight) {
		this.addLootItem(item, weight, true);
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
	 * Set the chance that an egg will spawn
	 * 
	 * @param eggSpawnChance the new egg spawn chance
	 * @param updateFile whether to update the dragon file or not
	 */
	public void setEggSpawnChance(double eggSpawnChance, boolean updateFile) {
		if (eggSpawnChance < 0) eggSpawnChance = 0;
		else if (eggSpawnChance > 100) eggSpawnChance = 100;
		
		this.eggSpawnChance = eggSpawnChance;
		
		if (updateFile) {
			this.template.updateConfig("egg-spawn-chance", eggSpawnChance);
		}
	}
	
	/**
	 * Set the chance that an egg will spawn and update the dragon file (if one
	 * exists)
	 * 
	 * @param eggSpawnChance the new egg spawn chance
	 */
	public void setEggSpawnChance(double eggSpawnChance) {
		this.setEggSpawnChance(eggSpawnChance, true);
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
	 * Set the name to be displayed on the dragon egg
	 * 
	 * @param eggName the new name
	 * @param updateFile whether to update the dragon file or not
	 */
	public void setEggName(String eggName, boolean updateFile) {
		this.eggName = eggName;
		
		if (updateFile) {
			this.template.updateConfig("egg-name", eggName);
		}
	}
	
	/**
	 * Set the name to be displayed on the dragon egg and update the dragon
	 * file (if one exists)
	 * 
	 * @param eggName the new name
	 */
	public void setEggName(String eggName) {
		this.setEggName(eggName, true);
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
	 * Set the lore to be displayed on the dragon egg
	 * 
	 * @param eggLore the new lore
	 * @param updateFile whether to update the dragon file or not
	 */
	public void setEggLore(List<String> eggLore, boolean updateFile) {
		this.eggLore = eggLore;
		
		if (updateFile) {
			this.template.updateConfig("egg-lore", eggLore);
		}
	}
	
	/**
	 * Set the lore to be displayed on the dragon egg and update the dragon
	 * file (if one exists)
	 * 
	 * @param eggLore the new lore
	 */
	public void setEggLore(List<String> eggLore) {
		this.setEggLore(eggLore, true);
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
	 * Set the minimum amount of loot to generate in the chest
	 * 
	 * @param minLootGen the new minimum loot count
	 * @param updateFile whether to update the dragon file or not
	 */
	public void setMinLootGen(int minLootGen, boolean updateFile) {
		if (minLootGen < 0) minLootGen = 0;
		Preconditions.checkArgument(minLootGen <= maxLootGen, "Minimum loot gen cannot be greater than maximum loot gen (%s)", maxLootGen);
		
		this.minLootGen = minLootGen;
		
		if (updateFile) {
			this.template.updateConfig("min-loot", minLootGen);
		}
	}
	
	/**
	 * Set the minimum amount of loot to generate in the chest and update
	 * the dragon file (if one exists)
	 * 
	 * @param minLootGen the new minimum loot gen count
	 */
	public void setMinLootGen(int minLootGen) {
		this.setMinLootGen(minLootGen, true);
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
	 * Set the maximum amount of loot to generate in the chest
	 * 
	 * @param maxLootGen the new maximum loot count
	 * @param updateFile whether to update the dragon file or not
	 */
	public void setMaxLootGen(int maxLootGen, boolean updateFile) {
		if (maxLootGen < 0) maxLootGen = 0;
		Preconditions.checkArgument(maxLootGen >= minLootGen, "Maximum loot gen cannot be less than minimum loot gen (%s)", minLootGen);
		
		this.maxLootGen = maxLootGen;
		
		if (updateFile) {
			this.template.updateConfig("max-loot", maxLootGen);
		}
	}
	
	/**
	 * Set the maximum amount of loot to generate in the chest and update
	 * the dragon file (if one exists)
	 * 
	 * @param maxLootGen the new maximum loot count
	 */
	public void setMaxLootGen(int maxLootGen) {
		this.setMaxLootGen(maxLootGen, true);
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
	 * Set the chance that a chest will spawn in place of an egg
	 * 
	 * @param chestSpawnChance the new chest spawn chance
	 * @param updateFile whether to update the dragon file or not
	 */
	public void setChestSpawnChance(double chestSpawnChance, boolean updateFile) {
		if (chestSpawnChance < 0) chestSpawnChance = 0;
		else if (chestSpawnChance > 100) chestSpawnChance = 100;
		
		this.chestSpawnChance = chestSpawnChance;
		
		if (updateFile) {
			this.template.updateConfig("chest-spawn-chance", chestSpawnChance);
		}
	}
	
	/**
	 * Set the chance that a chest will spawn in place of an egg and
	 * update the dragon file (if one exists)
	 * 
	 * @param chestSpawnChance the new chest spawn chance
	 */
	public void setChestSpawnChance(double chestSpawnChance) {
		this.setChestSpawnChance(chestSpawnChance, true);
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
	 * Set the name that will be displayed within the Chest
	 * 
	 * @param chestName the new chest name
	 * @param updateFile whether to update the dragon file or not
	 */
	public void setChestName(String chestName, boolean updateFile) {
		this.chestName = chestName;
		
		if (updateFile) {
			this.template.updateConfig("chest-name", chestName);
		}
	}
	
	/**
	 * Set the name that will be displayed within the Chest and update
	 * the dragon file (if one exists)
	 * 
	 * @param chestName the new chest name
	 */
	public void setChestName(String chestName) {
		this.setChestName(chestName, true);
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
	
	/**
	 * Spawn loot for the specific dragon battle
	 * 
	 * @param battle the battle to spawn loot for
	 * @param dragon the dragon whose egg should be spawned
	 */
	public void spawnLootFor(DragonBattle battle, EnderDragon dragon) {
		Validate.notNull(battle, "Cannot spawn loot for null dragon battle");
		Validate.notNull(dragon, "Cannot spawn loot for null ender dragon");
		
		Location location = battle.getEndPortalLocation();
		
		boolean spawnEgg = RANDOM.nextDouble() * 100 <= eggSpawnChance;
		boolean spawnChest = RANDOM.nextDouble() * 100 <= chestSpawnChance;
		
		if (spawnChest) {
			location.getBlock().setType(Material.CHEST);
			Chest chest = (Chest) location.getBlock().getState();
			NMS_ABSTRACT.setChestName(chest, chestName);
			
			Inventory inventory = chest.getInventory();
			inventory.clear();
			
			if (spawnEgg) {
				ItemStack eggItem = new ItemStack(Material.DRAGON_EGG);
				ItemMeta eggMeta = eggItem.getItemMeta();
				eggMeta.setDisplayName(eggName.replace("%dragon%", dragon.getName()));
				eggMeta.setLore(eggLore);
				eggItem.setItemMeta(eggMeta);
				
				inventory.setItem(inventory.getSize() / 2, eggItem);
			}
			
			int itemGenCount = Math.max(RANDOM.nextInt(maxLootGen), minLootGen);
			for (int i = 0; i < itemGenCount; i++) {
				if (inventory.firstEmpty() == -1) break;
				
				int slot = RANDOM.nextInt(inventory.getSize());
				
				if (inventory.getItem(slot) != null) {
					i--;
					continue;
				}
				
				inventory.setItem(slot, loot.next());
			}
		}
		
		else if (spawnEgg) {
			location.getBlock().setType(Material.DRAGON_EGG);
		}
	}
	
	private void parseDragonLoot() {
		if (template.file == null) return; // No file to parse loot from
		
		Logger logger = JavaPlugin.getPlugin(DragonEggDrop.class).getLogger();
		FileConfiguration dragonFile = template.configFile;
		
		// Parse the basic loot rewards (i.e. spawn chances & names)
		this.eggSpawnChance = dragonFile.getDouble("egg-spawn-chance", 100.0);
		this.eggName = ChatColor.translateAlternateColorCodes('&', dragonFile.getString("egg-name", "%dragon%&r's Egg"));
		this.eggLore = dragonFile.getStringList("egg-lore").stream()
				.map(s -> ChatColor.translateAlternateColorCodes('&', s))
				.collect(Collectors.toList());
		
		this.chestSpawnChance = dragonFile.getDouble("chest-spawn-chance", 0);
		this.chestName = dragonFile.getString("chest-name", "Loot Chest");
		this.minLootGen = dragonFile.getInt("min-loot");
		this.maxLootGen = dragonFile.getInt("max-loot");
		
		// Parse loot items
		ConfigurationSection lootSection = dragonFile.getConfigurationSection("loot");
		if (lootSection == null) return;
		
		for (String itemKey : lootSection.getKeys(false)) {
			// Parse root values (type, damage, amount and weight)
			double weight = lootSection.getDouble(itemKey + ".weight");
			
			Material type = EnumUtils.getEnum(Material.class, lootSection.getString(itemKey + ".type").toUpperCase());
			byte data = (byte) lootSection.getInt(itemKey + ".data");
			short damage = (short) lootSection.getInt(itemKey + ".damage");
			int amount = lootSection.getInt(itemKey + ".amount");
			
			if (type == null) {
				logger.warning("Invalid material type \"" + lootSection.getString(itemKey + ".type") + "\". Ignoring loot value...");
				continue;
			}
			
			// Create new item stack with passed values
			@SuppressWarnings("deprecation")
			ItemStack item = new ItemStack(type, 1, damage, data);
			item.setAmount(amount);
			
			// Parse meta
			String displayName = lootSection.getString(itemKey + ".display-name");
			List<String> lore = lootSection.getStringList(itemKey + ".lore");
			Map<Enchantment, Integer> enchantments = new HashMap<>();
			
			// Enchantment parsing
			if (lootSection.contains(itemKey + ".enchantments")) {
				for (String enchant : lootSection.getConfigurationSection(itemKey + ".enchantments").getKeys(false)) {
					Enchantment enchantment = Enchantment.getByName(enchant);
					int level = lootSection.getInt(itemKey + ".enchantments." + enchant);
					
					if (enchantment == null || level == 0) {
						logger.warning("Invalid enchantment \"" + enchant + "\" with level " + level);
						continue;
					}
					
					enchantments.put(enchantment, level);
				}
			}
			
			// Meta updating
			ItemMeta meta = item.getItemMeta();
			if (displayName != null) meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));
			if (!lore.isEmpty()) meta.setLore(lore.stream().map(s -> ChatColor.translateAlternateColorCodes('&', s)).collect(Collectors.toList()));
			enchantments.forEach((e, level) -> meta.addEnchant(e, level, true));
			item.setItemMeta(meta);
			
			this.loot.add(weight, item);
		}
	}
	
}