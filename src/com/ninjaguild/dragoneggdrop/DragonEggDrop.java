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

package com.ninjaguild.dragoneggdrop;

import java.util.logging.Level;

import org.bukkit.Particle;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import org.bukkit.ChatColor;

public class DragonEggDrop extends JavaPlugin {

	private PluginDescriptionFile pdf = null;
	private String chatPrefix = null;
	private DEDManager dedMan = null;

	public void onEnable() {
		saveDefaultConfig();
		pdf = getDescription();

		//update config version
		String currentVersion = getConfig().getString("version").trim();
		ConfigUtil cu = new ConfigUtil(this);
		cu.updateConfig(currentVersion);

		try {
			Particle.valueOf(getConfig().getString("particle-type", "FLAME").toUpperCase());
		} catch (IllegalArgumentException ex) {
			getLogger().log(Level.WARNING, "INVALID PARTICLE TYPE SPECIFIED! DISABLING...");
			getServer().getPluginManager().disablePlugin(this);
			getLogger().log(Level.INFO, "PLUGIN DISABLED");
			return;
		}

		ConfigurationSerialization.registerClass(LootEntry.class);
		
		getServer().getPluginManager().registerEvents(new Events(this), this);
		getCommand("dragoneggdrop").setExecutor(new Commands(this));
		
		dedMan = new DEDManager(this);
        
        chatPrefix = ChatColor.DARK_GRAY + "[" + ChatColor.GRAY + "DED" + ChatColor.DARK_GRAY + "] ";
	}

	public void onDisable() {
		//
	}
	
	protected String getChatPrefix() {
		return chatPrefix;
	}
	
	protected PluginDescriptionFile getDescriptionFile() {
		return pdf;
	}
	
	protected DEDManager getDEDManager() {
		return dedMan;
	}

}
