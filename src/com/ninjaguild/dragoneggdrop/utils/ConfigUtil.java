package com.ninjaguild.dragoneggdrop.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Set;

import com.ninjaguild.dragoneggdrop.DragonEggDrop;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * Represents a YAML configuration file to allow for reload functions
 * whilst also keeping Bukkit comments.
 */
public class ConfigUtil {

	private final DragonEggDrop plugin;

	/**
	 * Constructs a new ConfigUtils object.
	 * 
	 * @param plugin an instance of the DragonEggDrop plugin
	 */
	public ConfigUtil(final DragonEggDrop plugin) {
		this.plugin = plugin;
	}

	/**
	 * Update the configuration file.
	 * 
	 * @param currentVersion the version of the configuration file
	 */
	public void updateConfig(int currentVersion) {
		InputStream in = plugin.getResource("config.yml");
		try (InputStreamReader inReader = new InputStreamReader(in)) {
			FileConfiguration defaultConfig = YamlConfiguration.loadConfiguration(inReader);
			
			if (defaultConfig.getInt("version") == currentVersion) {
				return;
			}
			
			Set<String> newKeys = defaultConfig.getKeys(false);
			for (String key : plugin.getConfig().getKeys(false)) {
				if (key.equalsIgnoreCase("version")) {
					continue;
				}
				if (newKeys.contains(key)) {
					defaultConfig.set(key, plugin.getConfig().get(key));
				}
			}
			
			defaultConfig.save(new File(plugin.getDataFolder() + "/config.yml"));
		} catch (IOException e) { e.printStackTrace(); }
	}

}