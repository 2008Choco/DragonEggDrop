package com.ninjaguild.dragoneggdrop;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

public class LootManager {

	private DragonEggDrop plugin = null;
	private RandomCollection<ItemStack> loot = null;
	private Random rand = null;
	
	File lootConfigFile = null;
	FileConfiguration lootConfig = null;
	
	public LootManager(DragonEggDrop plugin) {
		this.plugin = plugin;
		rand = new Random();
		loadLootItems();
	}
	
	private void loadLootItems() {
		loot = new RandomCollection<>();
	    
		lootConfigFile = new File(plugin.getDataFolder() + "/loot.yml");
		lootConfig = YamlConfiguration.loadConfiguration(lootConfigFile);

		@SuppressWarnings("unchecked")
		Set<LootEntry> lootEntries = (Set<LootEntry>)lootConfig.get("loot-items");
		if (!lootEntries.isEmpty()) {
			for (LootEntry entry : lootEntries) {
				double weight = entry.getWeight();
				ItemStack item = entry.getItem();

				loot.add(weight, item);
			}
		}
	}
	
	protected void placeChest(Location loc) {
		if (loot.values().isEmpty()) {
			plugin.getLogger().log(Level.SEVERE, ChatColor.RED + "No Loot Items defined! Aborting Chest Placement.");
			return;
		}
		
		int maxLoot = plugin.getConfig().getInt("max-loot", 6);
		maxLoot = Math.max(maxLoot, 1);
		
		Block chestBlock = loc.getWorld().getBlockAt(loc);
		chestBlock.setType(Material.CHEST);
		
		Chest chest = (Chest)chestBlock.getState();
		for (int i = 0; i < maxLoot; i++) {
			int slot = rand.nextInt(chest.getBlockInventory().getSize());
			if (chest.getBlockInventory().getItem(slot).getType() != Material.AIR) {
				i--;
				continue;
			}
			chest.getBlockInventory().setItem(slot, loot.next());
		}
	}
	
	protected boolean addItem(double weight, ItemStack item) {
		boolean result = loot.add(weight, item);
		if (result) {
			LootEntry le = new LootEntry(weight, item);
			@SuppressWarnings("unchecked")
			Set<LootEntry> lootEntries = (Set<LootEntry>)lootConfig.get("loot-items");
			if (lootEntries == null) {
				lootEntries = new HashSet<>();
			}
			result = lootEntries.add(le);
			if (result) {
				lootConfig.set("loot-items", lootEntries);
				try {
					lootConfig.save(lootConfigFile);
				} catch (IOException e) {
					e.printStackTrace();
					result = false;
				}
			}
		}
		
		return result;
	}
	
}
