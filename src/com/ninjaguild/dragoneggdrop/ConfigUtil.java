package com.ninjaguild.dragoneggdrop;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class ConfigUtil {

	private DragonEggDrop plugin = null;

	public ConfigUtil(DragonEggDrop plugin) {
		this.plugin = plugin;
	}

	public void updateConfig(String configVersion) {
		FileConfiguration newConfig = getNewConfig();
		Set<String> newKeys = newConfig.getKeys(false);

		for (String key : plugin.getConfig().getKeys(false)) {
			if (key.equalsIgnoreCase("version")) {
				continue;
			}
			if (newKeys.contains(key)) {
				newConfig.set(key, plugin.getConfig().get(key));
			}
		}

		mergeCommentsAndSave(newConfig.saveToString());
	}

	private FileConfiguration getNewConfig() {
		File newConfig = null;

		try {
			InputStream in = plugin.getResource("config.yml");
			byte[] buffer = new byte[in.available()];
			in.read(buffer);
			in.close();

			newConfig = new File(plugin.getDataFolder() + "/config.temp.yml");
			OutputStream os = new FileOutputStream(newConfig);
			os.write(buffer);
			os.close();
		}
		catch (IOException ex) {
			ex.printStackTrace();
		}

		FileConfiguration config = YamlConfiguration.loadConfiguration(newConfig);
		newConfig.delete();

		return config;
	}

	private Map<Integer, String> getConfigMap() {
		Map<Integer, String> comments = new LinkedHashMap<>();
		InputStream in = plugin.getResource("config.yml");
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			int lineNum = 0;
			String current = null;
			while ((current = br.readLine()) != null) {
				if (current.trim().isEmpty() || current.trim().startsWith("#")) {
					comments.put(lineNum, current);
				}
				else {
					int spaces = 0;
					for (int i = 0; i < current.length(); i++) {
						if (current.charAt(i) == ' ') {
							spaces++;
						}
					}
					comments.put(lineNum, "<DATA-" + spaces + ">");
				}
				lineNum++;
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		return comments;
	}

	private void mergeCommentsAndSave(String configAsString) {
		File configFile = new File(plugin.getDataFolder() + "/config.yml");
		Map<Integer, String> configMap = getConfigMap();
		
		//strip header
		String[] tmp = configAsString.split("\r?\n");
		List<String> configValues = new ArrayList<>();
		for (String s : tmp) {
			if (s.trim().startsWith("#")) {
				continue;
			}

			configValues.add(s);
		}

		try {
			FileWriter fw = new FileWriter(configFile);

			int configIndex = 0;
			Iterator<Integer> itr = configMap.keySet().iterator();
			while (itr.hasNext()) {
				int lineNum = itr.next();
				String comment = configMap.get(lineNum);
				if (comment.startsWith("<DATA-")) {
					String value = configValues.get(configIndex++);
					fw.write(value + "\n");
				}
				else {
					fw.write(comment + "\n");
				}
			}

			fw.flush();
			fw.close();
		}
		catch (IOException ex) {
			ex.printStackTrace();
		}

	}

}
