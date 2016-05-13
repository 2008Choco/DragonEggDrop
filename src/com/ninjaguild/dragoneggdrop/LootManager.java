package com.ninjaguild.dragoneggdrop;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
import org.bukkit.craftbukkit.v1_9_R2.block.CraftChest;
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
	
	protected void placeChest(Location loc) {
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
		String chestTitle = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("loot-chest-title", "Chest"));
		((CraftChest)chest).getTileEntity().a(chestTitle);

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
	
	protected void placeChestAll(Location loc) {
		if (loot.values().isEmpty()) {
			plugin.getLogger().log(Level.SEVERE, ChatColor.RED + "No Loot Items defined! Aborting Chest Placement.");
			return;
		}
		
		Block chestBlock = loc.getWorld().getBlockAt(loc);
		chestBlock.setType(Material.CHEST);
		Chest chest = (Chest)chestBlock.getState();
		//set custom title
		String chestTitle = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("loot-chest-title", "Chest"));
		((CraftChest)chest).getTileEntity().a(chestTitle);
		
		List<ItemStack> lootItems = new ArrayList<>(loot.values());
		for (int i = 0; i < lootItems.size(); i++) {
			int slot = rand.nextInt(chest.getBlockInventory().getSize());
			ItemStack slotItem = chest.getBlockInventory().getItem(slot);
			if (slotItem != null && slotItem.getType() != Material.AIR) {
				i--;
				continue;
			}
			chest.getBlockInventory().setItem(slot, lootItems.get(i));
		}
	}

	protected boolean addItem(double weight, ItemStack item) {
		LootEntry le = new LootEntry(weight, item);
		@SuppressWarnings("unchecked")
		Set<LootEntry> lootEntries = (Set<LootEntry>)lootConfig.get("loot-items");
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
