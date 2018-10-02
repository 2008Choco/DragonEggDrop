package com.ninjaguild.dragoneggdrop.dragon;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.ninjaguild.dragoneggdrop.DragonEggDrop;
import com.ninjaguild.dragoneggdrop.versions.DragonBattle;
import com.ninjaguild.dragoneggdrop.versions.NMSAbstract;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang3.EnumUtils;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EnderDragon;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Represents a template for a custom dragon to be spawned containing information about
 * its name, the style of its boss bar, as well as the loot it will drop after it is killed.
 * 
 * @author Parker Hawke - 2008Choco
 */
public class DragonTemplate {
	
	public static final File DRAGONS_FOLDER = new File(JavaPlugin.getPlugin(DragonEggDrop.class).getDataFolder(), "dragons/");
	
	protected final File file;
	protected final FileConfiguration configFile;
	
	private final DragonLoot loot;
	private final String identifier;
	
	private String name;
	private BarStyle barStyle;
	private BarColor barColour;
	
	private double spawnWeight;
	private boolean announceRespawn;
	
	private final Map<Attribute, Double> attributes = new HashMap<>();
	
	/**
	 * Construct a new DragonTemplate object.
	 * 
	 * @param file the file holding this template data
	 * @param name the name of the dragon. Can be null
	 * @param barStyle the style of the bar. Can be null
	 * @param barColour the colour of the bar. Can be null
	 */
	public DragonTemplate(File file, String name, BarStyle barStyle, BarColor barColour) {
		Validate.notNull(file, "File cannot be null. See DragonTemplate(String, String, BarStyle, BarColor) for null files");
		
		this.file = file;
		this.configFile = YamlConfiguration.loadConfiguration(file);
		this.identifier = file.getName().substring(0, file.getName().lastIndexOf('.'));
		
		this.name = (name != null ? ChatColor.translateAlternateColorCodes('&', name) : null);
		this.barStyle = (barStyle != null ? barStyle : BarStyle.SOLID);
		this.barColour = (barColour != null ? barColour : BarColor.PINK);
		this.loot = new DragonLoot(this);
	}
	
	/**
	 * Construct a new DragonTemplate object.
	 * 
	 * @param identifier the name to identify this template
	 * @param name the name of the dragon. Can be null
	 * @param barStyle the style of the bar. Can be null
	 * @param barColour the colour of the bar. Can be null
	 */
	public DragonTemplate(String identifier, String name, BarStyle barStyle, BarColor barColour) {
		Validate.notEmpty(identifier, "Idenfitier must not be empty or null");
		
		this.file = null;
		this.configFile = null;
		this.identifier = identifier;
		
		this.name = (name != null ? ChatColor.translateAlternateColorCodes('&', name) : null);
		this.barStyle = (barStyle != null ? barStyle : BarStyle.SOLID);
		this.barColour = (barColour != null ? barColour : BarColor.PINK);
		this.loot = new DragonLoot(this);
	}
	
	/**
	 * Get the string that identifies this template. If a file was passed
	 * as a parameter in the creation of this template, the file's name
	 * will be used as the identifier.
	 * 
	 * @return the unique template identifier
	 */
	public String getIdentifier() {
		return identifier;
	}
	
	/**
	 * Get the file in the "dragons" folder that holds information for
	 * this dragon template.
	 * 
	 * @return the dragon template file
	 */
	public File getFile() {
		return file;
	}
	
	/**
	 * Set the name of the dragon.
	 * 
	 * @param name the dragon's new name
	 * @param updateFile whether to update the dragon file or not
	 */
	public void setName(String name, boolean updateFile) {
		this.name = name;
		
		if (updateFile) {
			this.updateConfig("dragon-name", name);
		}
	}
	
	/**
	 * Set the name of the dragon and update the dragon file (if one exists).
	 * 
	 * @param name the dragon's new name
	 */
	public void setName(String name) {
		this.setName(name, true);
	}
	
	/**
	 * Get the name of the dragon.
	 * 
	 * @return the dragon's name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Set the style of the boss bar.
	 * 
	 * @param barStyle the new boss bar style
	 * @param updateFile whether to update the dragon file or not
	 */
	public void setBarStyle(BarStyle barStyle, boolean updateFile) {
		this.barStyle = barStyle;
		
		if (updateFile) {
			this.updateConfig("bar-style", barStyle);
		}
	}
	
	/**
	 * Set the style of the boss bar and update the dragon file (if one exists).
	 * 
	 * @param barStyle the new boss bar style
	 */
	public void setBarStyle(BarStyle barStyle) {
		this.setBarStyle(barStyle);
	}
	
	/**
	 * Get the style of the boss bar.
	 * 
	 * @return the boss bar style
	 */
	public BarStyle getBarStyle() {
		return barStyle;
	}
	
	/**
	 * Set the colour of the boss bar.
	 * 
	 * @param barColour the new boss bar colour
	 * @param updateFile whether to update the dragon file or not
	 */
	public void setBarColor(BarColor barColour, boolean updateFile) {
		this.barColour = barColour;
		
		if (updateFile) {
			this.updateConfig("bar-color", barColour);
		}
	}
	
	/**
	 * Set the colour of the boss bar and update the dragon file (if one exists).
	 * 
	 * @param barColour the new boss bar colour
	 */
	public void setBarColor(BarColor barColour) {
		this.setBarColor(barColour, true);
	}
	
	/**
	 * Get the colour of the boss bar.
	 * 
	 * @return the boss bar colour
	 */
	public BarColor getBarColor() {
		return barColour;
	}
	
	/**
	 * Get the loot to be dropped after the dragon is killed.
	 * 
	 * @return the dragon loot
	 */
	public DragonLoot getLoot() {
		return loot;
	}
	
	/**
	 * Set the weight of this dragon's spawn percentage.
	 * 
	 * @param spawnWeight the new spawn weight
	 * @param updateFile whether to update the dragon file or not
	 */
	public void setSpawnWeight(double spawnWeight, boolean updateFile) {
		if (spawnWeight < 0) spawnWeight = 0;
		
		this.spawnWeight = spawnWeight;
		
		if (updateFile) {
			this.updateConfig("spawn-weight", spawnWeight);
		}
	}
	
	/**
	 * Set the weight of this dragon's spawn percentage and update the dragon file
	 * (if one exists).
	 * 
	 * @param spawnWeight the new spawn weight
	 */
	public void setSpawnWeight(double spawnWeight) {
		this.setSpawnWeight(spawnWeight, true);
	}
	
	/**
	 * Get the weight of this dragon's spawn percentage.
	 * 
	 * @return the spawn weight
	 */
	public double getSpawnWeight() {
		return spawnWeight;
	}
	
	/**
	 * Set whether this dragon's name should be announced as it respawns.
	 * 
	 * @param announceRespawn the new announcement state
	 * @param updateFile whether to update the dragon file or not
	 */
	public void setAnnounceRespawn(boolean announceRespawn, boolean updateFile) {
		this.announceRespawn = announceRespawn;
		
		if (updateFile) {
			this.updateConfig("announce-respawn", announceRespawn);
		}
	}
	
	/**
	 * Set whether this dragon's name should be announced as it respawns and
	 * update the dragon file (if one exists).
	 * 
	 * @param announceRespawn the new announcement state
	 */
	public void setAnnounceRespawn(boolean announceRespawn) {
		this.setAnnounceRespawn(announceRespawn, true);
	}
	
	/**
	 * Check whether this dragon's name should be announced as it respawns.
	 * 
	 * @return true if announce name, false otherwise
	 */
	public boolean shouldAnnounceRespawn() {
		return announceRespawn;
	}
	
	/**
	 * Get an immutable Map of all attributes and their values according to
	 * this template.
	 * 
	 * @return the mapped attributes and values
	 */
	public Map<Attribute, Double> getAttributes() {
		return ImmutableMap.copyOf(attributes);
	}
	
	/**
	 * Get the value to be applied for a specific attribute. If the provided
	 * attribute value is not specified, -1 will be returned.
	 * 
	 * @param attribute the attribute to check
	 * 
	 * @return the value of the attribute, or -1 if non existent
	 */
	public double getAttributeValue(Attribute attribute) {
		return attributes.getOrDefault(attribute, -1.0);
	}
	
	/**
	 * Check whether this template has specified an attribute's value.
	 * 
	 * @param attribute the attribute to check
	 * 
	 * @return true if defined. false otherwise
	 */
	public boolean hasAttribute(Attribute attribute) {
		return attributes.containsKey(attribute);
	}
	
	/**
	 * Add an attribute to this template.
	 * 
	 * @param attribute the attribute to add
	 * @param value the new value of the attribute
	 * @param updateFile whether to update the dragon file or not
	 */
	public void addAttribute(Attribute attribute, double value, boolean updateFile) {
		Validate.notNull(attribute, "Cannot add a null attribute");
		if (value < 0.0) value = 0.0;
		
		this.attributes.put(attribute, value);
		
		if (updateFile) {
			this.updateConfig("attributes." + attribute.name(), value);
		}
	}
	
	/**
	 * Add an attribute to this template and update the dragon file (if one
	 * exists).
	 * 
	 * @param attribute the attribute to add
	 * @param value the new value of the attribute
	 */
	public void addAttribute(Attribute attribute, double value) {
		this.addAttribute(attribute, value, true);
	}
	
	/**
	 * Remove an attribute from this template and set its value back to default.
	 * 
	 * @param attribute the attribute to remove
	 * @param updateFile whether to update the dragon file or not
	 */
	public void removeAttribute(Attribute attribute, boolean updateFile) {
		Validate.notNull(attribute, "Cannot remove a null attribute");
		
		this.attributes.remove(attribute);
		
		if (updateFile) {
			this.updateConfig("attributes." + attribute.name(), null);
		}
	}
	
	/**
	 * Remove an attribute from this template, set its value back to default and
	 * update the dragon file (if one exists).
	 * 
	 * @param attribute the attribute to remove
	 */
	public void removeAttribute(Attribute attribute) {
		this.removeAttribute(attribute, true);
	}
	
	/**
	 * Clear all loaded attribute mappings.
	 * 
	 * @param updateFile whether to update the dragon file or not
	 */
	public void clearAttributes(boolean updateFile) {
		this.attributes.clear();
		
		if (updateFile) {
			this.updateConfig("attributes", null);
		}
	}
	
	/**
	 * Clear all loaded attribute mappings and update the dragon file (if one exists).
	 */
	public void clearAttributes() {
		this.clearAttributes(true);
	}
	
	/**
	 * Apply this templates data to an EnderDragonBattle object.
	 * 
	 * @param nmsAbstract an instance of the NMSAbstract interface
	 * @param dragon the dragon to modify
	 * @param battle the battle to modify
	 */
	public void applyToBattle(NMSAbstract nmsAbstract, EnderDragon dragon, DragonBattle battle) {
		Validate.notNull(nmsAbstract, "Instance of NMSAbstract cannot be null. See DragonEggDrop#getNMSAbstract()");
		Validate.notNull(dragon, "Ender Dragon cannot be null");
		Validate.notNull(battle, "Instance of DragonBattle cannot be null");
		
		if (name != null) {
			dragon.setCustomName(name);
			battle.setBossBarTitle(name);
		}
		
		battle.setBossBarStyle(barStyle, barColour);
		this.attributes.forEach((a, v) -> {
			AttributeInstance attribute = dragon.getAttribute(a);
			if (attribute != null) {
				attribute.setBaseValue(v);
			}
		});
		
		// Set health... max health attribute doesn't do that for me. -,-
		if (attributes.containsKey(Attribute.GENERIC_MAX_HEALTH)) {
			dragon.setHealth(attributes.get(Attribute.GENERIC_MAX_HEALTH));
		}
	}
	
	/**
	 * Update a configuration value in this template's file (if one exists). If the file
	 * for this dragon template does not exist (i.e. a synthetically created template from
	 * an extension plugin), this method will fail silently.
	 * 
	 * @param path the configuration path to update
	 * @param value the value to set
	 */
	protected void updateConfig(String path, Object value) {
		if (configFile == null) return;
		
		configFile.set(path, value);
		try {
			configFile.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Load and parse all DragonTemplate objects from the dragons folder.
	 * 
	 * @return all parsed DragonTemplate objects
	 */
	public static List<DragonTemplate> loadTemplates() {
		DragonEggDrop plugin = JavaPlugin.getPlugin(DragonEggDrop.class);
		List<DragonTemplate> templates = new ArrayList<>();
		
		// Return empty list if the folder was just created
		if (DRAGONS_FOLDER.mkdir()) return templates;
		
		for (File file : DRAGONS_FOLDER.listFiles((file, name) -> name.endsWith(".yml"))) {
			if (file.getName().contains(" ")) {
				plugin.getLogger().warning("Dragon template files must not contain spaces (File=\"" + file.getName() + "\")! Ignoring...");
				continue;
			}
			
			FileConfiguration dragonFile = YamlConfiguration.loadConfiguration(file);
			
			// Load name, bar style & colour, weight and announcement status
			String name = dragonFile.getString("dragon-name", "Ender Dragon");
			BarStyle style = EnumUtils.getEnum(BarStyle.class, dragonFile.getString("bar-style", "SOLID").toUpperCase());
			BarColor color = EnumUtils.getEnum(BarColor.class, dragonFile.getString("bar-color", "PINK").toUpperCase());
			
			DragonTemplate template = new DragonTemplate(file, name, style, color);
			template.spawnWeight = dragonFile.getDouble("spawn-weight", 1);
			template.announceRespawn = dragonFile.getBoolean("announce-respawn", false);
			
			// Attribute modifier loading
			if (dragonFile.contains("attributes")) {
				for (String attributeKey : dragonFile.getConfigurationSection("attributes").getValues(false).keySet()) {
					Attribute attribute = EnumUtils.getEnum(Attribute.class, attributeKey);
					if (attribute == null) {
						plugin.getLogger().warning("Unknown attribute \"" + attributeKey + "\" for template \"" + file.getName() + "\". Ignoring...");
						continue;
					}
					
					double value = dragonFile.getDouble("attributes." + attributeKey, -1);
					if (value == -1) {
						plugin.getLogger().warning("Invalid double value specified at attribute \"" + attributeKey + "\" for template \"" + file.getName() + "\". Ignoring...");
						continue;
					}
					
					template.attributes.put(attribute, value);
				}
			}
			
			// Checking for existing templates
			if (templates.contains(template)) {
				JavaPlugin.getPlugin(DragonEggDrop.class).getLogger().warning("Duplicate dragon template with file name " + file.getName() + ". Ignoring");
				continue;
			}
			
			templates.add(template);
		}
		
		return templates;
	}
}