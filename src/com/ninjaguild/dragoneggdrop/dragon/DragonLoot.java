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
import com.google.common.collect.ImmutableList;
import com.ninjaguild.dragoneggdrop.DragonEggDrop;
import com.ninjaguild.dragoneggdrop.utils.RandomCollection;
import com.ninjaguild.dragoneggdrop.versions.DragonBattle;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Chest;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.projectiles.ProjectileSource;

/**
 * Represents a dragon's loot information (similar to a loot table) which may randomly
 * spawn after the dragon's death.
 * 
 * @author Parker Hawke - 2008Choco
 */
public class DragonLoot {
	
	private static final Random RANDOM = new Random();
	
	private final DragonTemplate template;
	private final RandomCollection<ItemStack> loot = new RandomCollection<>();
	
	private double eggSpawnChance = 100.0;
	private String eggName = "%dragon%'s Egg";
	private List<String> eggLore = new ArrayList<>();
	
	private double chestSpawnChance = 0.0;
	private String chestName = "Loot Chest";
	private int minLootGen = 3, maxLootGen = 6;
	
	private List<String> commands = new ArrayList<>();
	
	protected DragonLoot(DragonTemplate template) {
		this.template = template;
		this.parseDragonLoot();
	}
	
	/**
	 * Get a copy of the loot to be generated in a chest.
	 * 
	 * @return the random loot collection
	 */
	public RandomCollection<ItemStack> getLoot() {
		return RandomCollection.copyOf(loot);
	}
	
	/**
	 * Add a loot item to the random loot collection.
	 * 
	 * @param item the item to add
	 * @param weight the generation weight of the item
	 * @param updateFile whether to update the dragon file or not
	 */
	public void addLootItem(ItemStack item, double weight, boolean updateFile) {
		Validate.notNull(item, "Cannot add null ItemStack to loot");
		if (weight < 0) weight = 0;
		
		this.loot.add(weight, item);
		
		if (updateFile && template.configFile != null) {
			FileConfiguration config = template.configFile;
			int itemID = loot.size();
			
			config.set("loot." + itemID + ".weight", weight);
			config.set("loot." + itemID + ".type", item.getType().name());
			config.set("loot." + itemID + ".amount", item.getAmount());
			
			if (item.hasItemMeta()) {
				ItemMeta meta = item.getItemMeta();
				
				if (((Damageable) meta).getDamage() != 0) config.set("loot." + itemID + ".damage", ((Damageable) meta).getDamage());
				if (meta.hasDisplayName()) config.set("loot." + itemID + ".display-name", meta.getDisplayName());
				if (meta.hasLore()) config.set("loot." + itemID + ".lore", meta.getLore());
				if (meta.hasEnchants()) {
					for (Enchantment enchant : meta.getEnchants().keySet()) {
						config.set("loot." + itemID + ".enchantments." + enchant.getKey().getKey(), meta.getEnchantLevel(enchant));
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
	 * (if one exists).
	 * 
	 * @param item the item to add
	 * @param weight the generation weight of the item
	 */
	public void addLootItem(ItemStack item, double weight) {
		this.addLootItem(item, weight, true);
	}
	
	/**
	 * Check whether a dragon's egg may be spawned or not.
	 * 
	 * @return true if an egg may be spawned, false otherwise
	 */
	public boolean canSpawnEgg() {
		return eggSpawnChance > 0;
	}
	
	/**
	 * Set the chance that an egg will spawn.
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
	 * exists).
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
	 * Set the name to be displayed on the dragon egg.
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
	 * file (if one exists).
	 * 
	 * @param eggName the new name
	 */
	public void setEggName(String eggName) {
		this.setEggName(eggName, true);
	}
	
	/**
	 * Get the name to be displayed on the dragon egg.
	 * 
	 * @return the name display
	 */
	public String getEggName() {
		return eggName;
	}
	
	/**
	 * Set the lore to be displayed on the dragon egg.
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
	 * file (if one exists).
	 * 
	 * @param eggLore the new lore
	 */
	public void setEggLore(List<String> eggLore) {
		this.setEggLore(eggLore, true);
	}
	
	/**
	 * Get the lore to be displayed on the dragon egg.
	 * 
	 * @return the lore to display
	 */
	public List<String> getEggLore() {
		return eggLore;
	}
	
	/**
	 * Set the minimum amount of loot to generate in the chest.
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
	 * the dragon file (if one exists).
	 * 
	 * @param minLootGen the new minimum loot gen count
	 */
	public void setMinLootGen(int minLootGen) {
		this.setMinLootGen(minLootGen, true);
	}
	
	/**
	 * Get the minimum amount of loot to generate in the chest.
	 * 
	 * @return the minimum loot count
	 */
	public int getMinLootGen() {
		return minLootGen;
	}
	
	/**
	 * Set the maximum amount of loot to generate in the chest.
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
	 * the dragon file (if one exists).
	 * 
	 * @param maxLootGen the new maximum loot count
	 */
	public void setMaxLootGen(int maxLootGen) {
		this.setMaxLootGen(maxLootGen, true);
	}
	
	/**
	 * Get the maximum amount of loot to generate in the chest.
	 * 
	 * @return the maximum loot count
	 */
	public int getMaxLootGen() {
		return maxLootGen;
	}
	
	/**
	 * Set the chance that a chest will spawn in place of an egg.
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
	 * update the dragon file (if one exists).
	 * 
	 * @param chestSpawnChance the new chest spawn chance
	 */
	public void setChestSpawnChance(double chestSpawnChance) {
		this.setChestSpawnChance(chestSpawnChance, true);
	}
	
	/**
	 * Get the chance that a chest will spawn in place of an egg.
	 * 
	 * @return the chest spawn chance
	 */
	public double getChestSpawnChance() {
		return chestSpawnChance;
	}
	
	/**
	 * Set the name that will be displayed within the Chest.
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
	 * the dragon file (if one exists).
	 * 
	 * @param chestName the new chest name
	 */
	public void setChestName(String chestName) {
		this.setChestName(chestName, true);
	}
	
	/**
	 * Get the name that will be displayed within the Chest.
	 * 
	 * @return the chest's name
	 */
	public String getChestName() {
		return chestName;
	}
	
	/**
	 * Check whether a chest may be spawned or not.
	 * 
	 * @return true if a chest may be spawned, false otherwise
	 */
	public boolean canSpawnChest() {
		return chestSpawnChance > 0;
	}
	
	/**
	 * Add a command to the list of commands to be executed upon the death
	 * of the dragon.
	 * 
	 * @param command the command to add
	 * @param updateFile whether to update the dragon file or not
	 */
	public void addCommand(String command, boolean updateFile) {
		this.commands.add(command);
		
		if (updateFile) {
			List<String> commands = template.configFile.getStringList("death-commands");
			commands.add(command);
			this.template.updateConfig("death-commands", commands);
		}
	}
	
	/**
	 * Add a command to the list of commands to be executed upon the death
	 * of the dragon and update the dragon file (if one exists).
	 * 
	 * @param command the command to add
	 */
	public void addCommand(String command) {
		this.addCommand(command, true);
	}
	
	/**
	 * Remove a command from the list of commands to be executed upon the
	 * death of the dragon.
	 * 
	 * @param command the command to remove
	 * @param updateFile whether to update the dragon file or not
	 */
	public void removeCommand(String command, boolean updateFile) {
		this.commands.remove(command);
		
		if (updateFile) {
			List<String> commands = template.configFile.getStringList("death-commands");
			commands.remove(command);
			this.template.updateConfig("death-commands", commands);
		}
	}
	
	/**
	 * Remove a command from the list of commands to be executed upon the
	 * death of the dragon and update the dragon file (if one exists).
	 * 
	 * @param command the command to remove
	 */
	public void removeCommand(String command) {
		this.commands.remove(command);
	}
	
	/**
	 * Check whether the provided command will be executed after the dragon
	 * has been killed.
	 * 
	 * @param command the command to check
	 * @return true if to be executed, false otherwise
	 */
	public boolean hasCommand(String command) {
		return commands.contains(command);
	}
	
	/**
	 * Get an immutable list of all commands to be executed upon the death
	 * of the dragon.
	 * 
	 * @return all commands to be executed
	 */
	public List<String> getCommands() {
		return ImmutableList.copyOf(commands);
	}
	
	/**
	 * Spawn loot for the specific dragon battle.
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
		
		// Spawn a chest
		if (spawnChest) {
			location.getBlock().setType(Material.CHEST);
			Chest chest = (Chest) location.getBlock().getState();
			chest.setCustomName(chestName);
			
			Inventory inventory = chest.getInventory();
			inventory.clear();
			
			// Spawn an egg within the chest
			if (spawnEgg) {
				ItemStack eggItem = new ItemStack(Material.DRAGON_EGG);
				ItemMeta eggMeta = eggItem.getItemMeta();
				eggMeta.setDisplayName(eggName.replace("%dragon%", dragon.getName()));
				eggMeta.setLore(eggLore);
				eggItem.setItemMeta(eggMeta);
				
				inventory.setItem(inventory.getSize() / 2, eggItem);
			}
			
			// Generate loot within the chest
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
		
		// Spawn the egg
		else if (spawnEgg) {
			location.getBlock().setType(Material.DRAGON_EGG);
		}
		
		// Execute commands
		Player commandTarget = null;
		EntityDamageEvent lastDamageCause = dragon.getLastDamageCause();
		if (lastDamageCause instanceof EntityDamageByEntityEvent) {
			Entity damager = ((EntityDamageByEntityEvent) lastDamageCause).getDamager();
			
			if (damager instanceof Player) {
				commandTarget = (Player) damager;
			} else if (damager instanceof Projectile) {
				ProjectileSource projectileSource = ((Projectile) damager).getShooter();
				if (!(projectileSource instanceof Player)) return; // Give up
				
				commandTarget = (Player) projectileSource;
			}
		}
		
		for (String command : commands) {
			if (command.contains("%player%") && commandTarget == null) continue;
			
			String commandToExecute = command.replace("%dragon%", dragon.getCustomName());
			if (commandTarget != null) {
				commandToExecute = commandToExecute.replace("%player%", commandTarget.getName());
			}
			
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), commandToExecute);
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
		
		this.commands = dragonFile.getStringList("death-commands");
		
		// Parse loot items
		ConfigurationSection lootSection = dragonFile.getConfigurationSection("loot");
		if (lootSection == null) return;
		
		for (String itemKey : lootSection.getKeys(false)) {
			// Parse root values (type, damage, amount and weight)
			double weight = lootSection.getDouble(itemKey + ".weight");
			
			Material type = Material.matchMaterial(lootSection.getString(itemKey + ".type", "minecraft:stone"));
			short damage = (short) lootSection.getInt(itemKey + ".damage");
			int amount = lootSection.getInt(itemKey + ".amount");
			
			if (type == null) {
				logger.warning("Invalid material type \"" + lootSection.getString(itemKey + ".type") + "\". Ignoring loot value...");
				continue;
			}
			
			// Create new item stack with passed values
			ItemStack item = new ItemStack(type, amount);
			
			// Parse meta
			String displayName = lootSection.getString(itemKey + ".display-name");
			List<String> lore = lootSection.getStringList(itemKey + ".lore");
			Map<Enchantment, Integer> enchantments = new HashMap<>();
			
			// Enchantment parsing
			if (lootSection.contains(itemKey + ".enchantments")) {
				for (String enchant : lootSection.getConfigurationSection(itemKey + ".enchantments").getKeys(false)) {
					enchant = enchant.toLowerCase();
					Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft(enchant.startsWith("minecraft:") ? enchant.substring(10) : enchant));
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
			((Damageable) meta).setDamage(damage);
			if (displayName != null) meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));
			if (!lore.isEmpty()) meta.setLore(lore.stream().map(s -> ChatColor.translateAlternateColorCodes('&', s)).collect(Collectors.toList()));
			enchantments.forEach((e, level) -> meta.addEnchant(e, level, true));
			item.setItemMeta(meta);
			
			this.loot.add(weight, item);
		}
	}
	
}