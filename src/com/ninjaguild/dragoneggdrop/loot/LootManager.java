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

package com.ninjaguild.dragoneggdrop.loot;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;

import com.ninjaguild.dragoneggdrop.DragonEggDrop;
import com.ninjaguild.dragoneggdrop.management.DEDManager;
import com.ninjaguild.dragoneggdrop.utils.RandomCollection;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * The core manager of loot distribution. Loads, creates and randomizes
 * loot generation upon the death of an Ender Dragon based on configuration
 * options
 */
public class LootManager {

	private final DragonEggDrop plugin;
	
	private final Random rand;
	private RandomCollection<ItemStack> loot = null;
	
	private File lootConfigFile = null;
	private FileConfiguration lootConfig = null;
	
	/**
	 * Construct a new LootManager instance. This object should mainly be
	 * managed by the {@link DEDManager} class
	 * 
	 * @param plugin an instance of the DragonEggDrop plugin
	 */
	public LootManager(final DragonEggDrop plugin) {
		this.plugin = plugin;
		this.rand = new Random();
		this.loadLootItems();
	}
	
	/**
	 * Load all configured loot items from the loot.yml
	 */
	private void loadLootItems() {
		loot = new RandomCollection<>();
	    
		plugin.saveResource("loot.yml", false);
		lootConfigFile = new File(plugin.getDataFolder() + "/loot.yml");
		lootConfig = YamlConfiguration.loadConfiguration(lootConfigFile);

		@SuppressWarnings("unchecked")
		Set<LootEntry> lootEntries = (Set<LootEntry>)lootConfig.get("loot-items");
		if (lootEntries != null && !lootEntries.isEmpty()) {
			for (LootEntry entry : lootEntries) {
				double weight = entry.getWeight();
				ItemStack item = entry.getItem();

				loot.add(weight, item);
			}
		}
	}
	
	/**
	 * Get all loot loaded into the loot manager
	 * 
	 * @return all loaded loot
	 */
	public RandomCollection<ItemStack> getLoot() {
		return loot;
	}
	
	/**
	 * Place a chest with randomized loot at a specific location
	 * 
	 * @param loc the location to place the chest
	 */
	public void placeChest(Location loc) {
		if (loot.values().isEmpty()) {
			plugin.getLogger().log(Level.SEVERE, ChatColor.RED + "No Loot Items defined! Aborting Chest Placement.");
			return;
		}
		
		int minLoot = plugin.getConfig().getInt("min-loot", 2);
		minLoot = Math.max(minLoot, 1);
		int maxLoot = plugin.getConfig().getInt("max-loot", 6);
		maxLoot = Math.max(maxLoot, 1);
		int numItems = Math.max(rand.nextInt(maxLoot), minLoot);
		
		Block chestBlock = loc.getWorld().getBlockAt(loc);
		chestBlock.setType(Material.CHEST);
		Chest chest = (Chest)chestBlock.getState();
		//set custom title
		String chestTitle = ChatColor.translateAlternateColorCodes('&',
				plugin.getConfig().getString("loot-chest-title", "Chest"));
		this.plugin.getNMSAbstract().setChestName(chest, chestTitle);

		for (int i = 0; i < numItems; i++) {
			int slot = rand.nextInt(chest.getBlockInventory().getSize());
			ItemStack slotItem = chest.getBlockInventory().getItem(slot);
			if (slotItem != null && slotItem.getType() != Material.AIR) {
				i--;
				continue;
			}
			chest.getBlockInventory().setItem(slot, loot.next());
		}
	}
	
	/**
	 * Place a chest with all loot randomly distributed throughout
	 * the chests' inventory
	 * 
	 * @param loc the location to place the chest
	 */
	public void placeChestAll(Location loc) {
		if (loot.values().isEmpty()) {
			plugin.getLogger().log(Level.SEVERE, ChatColor.RED + "No Loot Items defined! Aborting Chest Placement.");
			return;
		}
		
		Block chestBlock = loc.getWorld().getBlockAt(loc);
		chestBlock.setType(Material.CHEST);
		Chest chest = (Chest)chestBlock.getState();
		Inventory inv = chest.getBlockInventory();
		//set custom title
		String chestTitle = ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("loot-chest-title", "Chest"));
		this.plugin.getNMSAbstract().setChestName(chest, chestTitle);
		
		List<ItemStack> lootItems = new ArrayList<>(loot.values());
		for (int i = 0; i < lootItems.size(); i++) {
			if (inv.firstEmpty() == -1) break;
			
			int slot = rand.nextInt(inv.getSize());
			ItemStack slotItem = chest.getBlockInventory().getItem(slot);
			if (slotItem != null && slotItem.getType() != Material.AIR) {
				i--;
				continue;
			}
			chest.getBlockInventory().setItem(slot, lootItems.get(i));
		}
	}

	/**
	 * Create and add a LootEntry to the LootManager. The loot passed into
	 * this method will be randomly generated in future loot chests
	 * <br><b>NOTE:</b> This method will also save loot information to the
	 * loot.yml file
	 * 
	 * @param weight the weighted randomness of the item
	 * @param item the item to add
	 * 
	 * @return true if the item was successfully added
	 */
	public boolean addItem(double weight, ItemStack item) {
		LootEntry le = new LootEntry(weight, item);
		@SuppressWarnings("unchecked")
		Set<LootEntry> lootEntries = (Set<LootEntry>) lootConfig.get("loot-items");
		if (lootEntries == null) {
			lootEntries = new HashSet<>();
		}
		boolean result = lootEntries.add(le);
		if (result) {
			result = loot.add(weight, item);
			lootConfig.set("loot-items", lootEntries);
			try {
				lootConfig.save(lootConfigFile);
			} catch (IOException e) {
				e.printStackTrace();
				result = false;
			}
		}

		return result;
	}

}