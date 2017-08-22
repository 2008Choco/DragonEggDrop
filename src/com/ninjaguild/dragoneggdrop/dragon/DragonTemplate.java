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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.ninjaguild.dragoneggdrop.DragonEggDrop;
import com.ninjaguild.dragoneggdrop.versions.DragonBattle;
import com.ninjaguild.dragoneggdrop.versions.NMSAbstract;

import org.apache.commons.lang3.EnumUtils;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EnderDragon;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Represents a template for a custom dragon to be spawned containing
 * information about its name, the style of its boss bar, as well as
 * the loot it will drop after it is killed
 * 
 * @author Parker Hawke - 2008Choco
 */
public class DragonTemplate {
	
	public static final File DRAGONS_FOLDER = new File(JavaPlugin.getPlugin(DragonEggDrop.class).getDataFolder(), "dragons/");
	
	protected final File file;
	protected final FileConfiguration configFile;
	
	private final DragonLoot loot;
	
	private String name;
	private BarStyle barStyle;
	private BarColor barColour;
	
	private double spawnWeight;
	private boolean announceRespawn;
	
	/**
	 * Construct a new DragonTemplate object with the default dragon loot
	 * 
	 * @param file the file holding this template data. Can be null
	 * @param name the name of the dragon
	 * @param barStyle the style of the bar
	 * @param barColour the colour of the bar
	 */
	public DragonTemplate(File file, String name, BarStyle barStyle, BarColor barColour) {
		this.file = file;
		this.configFile = (file != null ? YamlConfiguration.loadConfiguration(file) : null);
		this.name = (name != null ? ChatColor.translateAlternateColorCodes('&', name) : null);
		this.barStyle = (barStyle != null ? barStyle : BarStyle.SOLID);
		this.barColour = (barColour != null ? barColour : BarColor.PINK);
		this.loot = new DragonLoot(this);
	}
	
	/**
	 * Construct a new DragonTemplate object
	 * 
	 * @param name the name of the dragon
	 * @param barStyle the style of the bar
	 * @param barColour the colour of the bar
	 */
	public DragonTemplate(String name, BarStyle barStyle, BarColor barColour) {
		this(null, name, barStyle, barColour);
	}
	
	/**
	 * Get the file in the "dragons" folder that holds information for
	 * this dragon template
	 * 
	 * @return the dragon template file
	 */
	public File getFile() {
		return file;
	}
	
	/**
	 * Set the name of the dragon
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
	 * Set the name of the dragon and update the dragon file (if one exists)
	 * 
	 * @param name the dragon's new name
	 */
	public void setName(String name) {
		this.setName(name, true);
	}
	
	/**
	 * Get the name of the dragon
	 * 
	 * @return the dragon's name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Set the style of the boss bar
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
	 * Set the style of the boss bar and update the dragon file (if one exists)
	 * 
	 * @param barStyle the new boss bar style
	 */
	public void setBarStyle(BarStyle barStyle) {
		this.setBarStyle(barStyle);
	}
	
	/**
	 * Get the style of the boss bar
	 * 
	 * @return the boss bar style
	 */
	public BarStyle getBarStyle() {
		return barStyle;
	}
	
	/**
	 * Set the colour of the boss bar
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
	 * Set the colour of the boss bar and update the dragon file (if one exists)
	 * 
	 * @param barColour the new boss bar colour
	 */
	public void setBarColor(BarColor barColour) {
		this.setBarColor(barColour, true);
	}
	
	/**
	 * Get the colour of the boss bar
	 * 
	 * @return the boss bar colour
	 */
	public BarColor getBarColor() {
		return barColour;
	}
	
	/**
	 * Get the loot to be dropped after the dragon is killed
	 * 
	 * @return the dragon loot
	 */
	public DragonLoot getLoot() {
		return loot;
	}
	
	/**
	 * Set the weight of this dragon's spawn percentage
	 * 
	 * @param spawnWeight the new spawn weight
	 * @param updateFile whether to update the dragon file or not
	 */
	public void setSpawnWeight(double spawnWeight, boolean updateFile) {
		this.spawnWeight = spawnWeight;
		
		if (updateFile) {
			this.updateConfig("spawn-weight", spawnWeight);
		}
	}
	
	/**
	 * Set the weight of this dragon's spawn percentage and update the dragon file
	 * (if one exists)
	 * 
	 * @param spawnWeight the new spawn weight
	 */
	public void setSoawnWeight(double spawnWeight) {
		this.setSpawnWeight(spawnWeight, true);
	}
	
	/**
	 * Get the weight of this dragon's spawn percentage
	 * 
	 * @return the spawn weight
	 */
	public double getSpawnWeight() {
		return spawnWeight;
	}
	
	/**
	 * Set whether this dragon's name should be announced as it respawns
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
	 * update the dragon file (if one exists)
	 * 
	 * @param announceRespawn the new announcement state
	 */
	public void setAnnounceRespawn(boolean announceRespawn) {
		this.setAnnounceRespawn(announceRespawn, true);
	}
	
	/**
	 * Check whether this dragon's name should be announced as it respawns
	 * 
	 * @return true if announce name, false otherwise
	 */
	public boolean shouldAnnounceRespawn() {
		return announceRespawn;
	}
	
	/**
	 * Apply this templates data to an EnderDragonBattle object
	 * 
	 * @param nmsAbstract an instance of the NMSAbstract interface
	 * @param dragon the dragon to modify
	 * @param battle the battle to modify
	 */
	public void applyToBattle(NMSAbstract nmsAbstract, EnderDragon dragon, DragonBattle battle) {
		if (name != null) {
			dragon.setCustomName(name);
			battle.setBossBarTitle(name);
		}
		battle.setBossBarStyle(barStyle, barColour);
	}
	
	/**
	 * Update a configuration value in this template's file (if one exists).
	 * If the file for this dragon template does not exist (i.e. a synthetically
	 * created template from an extension plugin), this method will fail silently
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
	 * Load and parse all DragonTemplate objects from the dragons folder
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
			
			String name = dragonFile.getString("dragon-name", "Ender Dragon");
			BarStyle style = EnumUtils.getEnum(BarStyle.class, dragonFile.getString("bar-style", "SOLID").toUpperCase());
			BarColor color = EnumUtils.getEnum(BarColor.class, dragonFile.getString("bar-color", "PINK").toUpperCase());
			
			DragonTemplate template = new DragonTemplate(file, name, style, color);
			template.spawnWeight = dragonFile.getDouble("spawn-weight", 1);
			template.announceRespawn = dragonFile.getBoolean("announce-respawn", false);
			
			if (templates.contains(template)) {
				JavaPlugin.getPlugin(DragonEggDrop.class).getLogger().warning("Duplicate dragon template with file name " + file.getName() + ". Ignoring");
				continue;
			}
			
			templates.add(template);
		}
		
		return templates;
	}
}