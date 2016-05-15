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
