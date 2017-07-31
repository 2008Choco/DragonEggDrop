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
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Represents a template for a custom dragon to be spawned containing
 * information about its name, the style of its boss bar, as well as
 * the loot it will drop after it is killed
 * 
 * @author Parker Hawke - 2008Choco
 */
public class DragonTemplate {
	
	private static final File DRAGONS_FOLDER = new File(JavaPlugin.getPlugin(DragonEggDrop.class).getDataFolder(), "dragons/");
	private static final DragonLoot DEFAULT_DRAGON_LOOT = null;
	
	private final String name;
	private final BarStyle barStyle;
	private final BarColor barColour;
	
	private final DragonLoot loot;
	
	/**
	 * Construct a new DragonTemplate object
	 * 
	 * @param name the name of the dragon
	 * @param barStyle the style of the bar
	 * @param barColour the colour of the bar
	 * @param loot the loot to drop after the dragon has been killed
	 */
	public DragonTemplate(String name, BarStyle barStyle, BarColor barColour, DragonLoot loot) {
		this.name = (name != null ? ChatColor.translateAlternateColorCodes('&', name) : null);
		this.barStyle = (barStyle != null ? barStyle : BarStyle.SOLID);
		this.barColour = (barColour != null ? barColour : BarColor.PINK);
		this.loot = loot;
	}
	
	/**
	 * Construct a new DragonTemplate object with the default dragon loot
	 * 
	 * @param name the name of the dragon
	 * @param barStyle the style of the bar
	 * @param barcolour the colour of the bar
	 */
	public DragonTemplate(String name, BarStyle barStyle, BarColor barColour) {
		this(name, barStyle, barColour, DEFAULT_DRAGON_LOOT);
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
	 * Get the style of the boss bar
	 * 
	 * @return the boss bar style
	 */
	public BarStyle getBarStyle() {
		return barStyle;
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
	 * Apply this templates data to an EnderDragonBattle object
	 * 
	 * @param nmsAbstract an instance of the NMSAbstract interface
	 * @param dragon the dragon to modify
	 * @param battle the battle to modify
	 */
	public void applyToBattle(NMSAbstract nmsAbstract, DragonBattle battle) {
		if (name != null) {
			battle.getEnderDragon().setCustomName(name);
			battle.setBossBarTitle(name);
		}
		
		battle.setBossBarStyle(barStyle, barColour);
	}
	
	/**
	 * Load and parse all DragonTemplate objects (if possible) from
	 * a List of String data from a configuration file
	 * 
	 * @param data the data to parse
	 * @return all parsed DragonTemplate objects
	 */
	public static List<DragonTemplate> loadTemplates(List<String> data) {
		List<DragonTemplate> templates = new ArrayList<>();
		
		for (String dragonData : data) {
			String[] splitData = dragonData.split("\\|");
			
			String name = null;
			BarStyle style = null;
			BarColor colour = null;
			
			// Here for legacy purposes
			if (splitData.length == 1) {
				name = dragonData;
				
				// In case name=whatever
				String[] tempData = name.split("=");
				if (tempData.length == 2)
					name = tempData[1];
				
				templates.add(new DragonTemplate(name, null, null));
				continue;
			}
			
			for (String dragonInfo : splitData) {
				String[] splitInfo = dragonInfo.split("=");
				if (splitInfo.length != 2) continue;
				
				String dataType = splitInfo[0].trim();
				String value = splitInfo[1].trim();
				
				if (dataType.equalsIgnoreCase("name")) {
					name = value;
				}
				else if (dataType.equalsIgnoreCase("style")) {
					value = value.toUpperCase();
					
					if (!EnumUtils.isValidEnum(BarStyle.class, value)) continue;
					style = BarStyle.valueOf(value);
				}
				else if (dataType.equalsIgnoreCase("color") || dataType.equalsIgnoreCase("colour")) {
					value = value.toUpperCase();
					
					if (!EnumUtils.isValidEnum(BarColor.class, value)) continue;
					colour = BarColor.valueOf(value);
				}
			}
			
			templates.add(new DragonTemplate(name, style, colour));
		}
		
		return templates;
	}
	
	public static List<DragonTemplate> loadTemplates() {
		List<DragonTemplate> templates = new ArrayList<>();
		
		// Return empty list if the folder was just created
		if (DRAGONS_FOLDER.mkdir()) return templates;
		
		for (File file : DRAGONS_FOLDER.listFiles((file, name) -> name.endsWith(".yml"))) {
			FileConfiguration dragonFile = YamlConfiguration.loadConfiguration(file);
			
			String name = dragonFile.getString("dragon-name");
			BarStyle style = EnumUtils.getEnum(BarStyle.class, dragonFile.getString("bar-style"));
			BarColor color = EnumUtils.getEnum(BarColor.class, dragonFile.getString("bar-color"));
			DragonLoot loot = new DragonLoot(dragonFile.getConfigurationSection("loot"));
			
			DragonTemplate template = new DragonTemplate(name, style, color, loot);
			
			if (templates.contains(template)) {
				JavaPlugin.getPlugin(DragonEggDrop.class).getLogger().warning("Duplicate dragon template with file name " + file.getName() + ". Ignoring");
				continue;
			}
			
			templates.add(template);
		}
		
		return templates;
	}
}