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

// *The goal was to make this update configs while keeping comments

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

	protected void updateConfig(String currentVersion) {
		InputStream in = plugin.getResource("config.yml");
		InputStreamReader inReader = new InputStreamReader(in);
		FileConfiguration defaultConfig =
				YamlConfiguration.loadConfiguration(inReader);
		if (defaultConfig.getString("version").equals(currentVersion)) {
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
		
		try {
			defaultConfig.save(new File(plugin.getDataFolder() + "/config.yml"));
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

}
