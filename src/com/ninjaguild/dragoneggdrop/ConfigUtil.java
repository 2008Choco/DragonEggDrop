package com.ninjaguild.dragoneggdrop;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Set;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class ConfigUtil {

	private final DragonEggDrop plugin;

	public ConfigUtil(final DragonEggDrop plugin) {
		this.plugin = plugin;
	}

	protected void updateConfig(String configVersion) {
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
		
		try {
			defaultConfig.save(new File(plugin.getDataFolder() + "/config.yml"));
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

}
