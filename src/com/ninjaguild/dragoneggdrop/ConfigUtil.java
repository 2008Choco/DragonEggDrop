package com.ninjaguild.dragoneggdrop;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

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
		
		saveConfig(newConfig);
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
    
    private void saveConfig(FileConfiguration config) {
    	try {
			config.save(new File(plugin.getDataFolder() + "/config.yml"));
		}
    	catch (IOException ex) {
			ex.printStackTrace();
		}
    }
	
}
