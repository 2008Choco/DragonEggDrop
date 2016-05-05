package com.ninjaguild.dragoneggdrop;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Set;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class ConfigUtil {

	private DragonEggDrop plugin = null;

	public ConfigUtil(DragonEggDrop plugin) {
		this.plugin = plugin;
	}

	public void updateConfig(String configVersion) {
		InputStream in = plugin.getResource("config.yml");
		InputStreamReader inReader = new InputStreamReader(in);
		FileConfiguration defaultConfig =
				YamlConfiguration.loadConfiguration(inReader);
		Set<String> newKeys = defaultConfig.getKeys(false);

		for (String key : plugin.getConfig().getKeys(false)) {
			if (key.equalsIgnoreCase("version")) {
				continue;
			}
			if (newKeys.contains(key)) {
				defaultConfig.set(key, plugin.getConfig().get(key));
			}
		}
		
		File oldConfigFile = new File(plugin.getDataFolder() + "/config.yml");
		if (oldConfigFile.exists()) {
			oldConfigFile.delete();
		}
		
		plugin.getConfig().setDefaults(defaultConfig);
		plugin.getConfig().options().copyDefaults(true);
		plugin.saveConfig();
	}

}
