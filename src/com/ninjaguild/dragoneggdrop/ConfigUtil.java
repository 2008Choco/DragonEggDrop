package com.ninjaguild.dragoneggdrop;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

public class ConfigUtil {

	private DragonEggDrop plugin = null;
	
	public ConfigUtil(DragonEggDrop plugin) {
	    this.plugin = plugin;
	}
	
	public void updateConfig(String configVersion) {
		Map<String, Object> newConfig = getConfigVals();
		
        for (String var : plugin.getConfig().getKeys(false)) {
            newConfig.remove(var);
        }
        
        if (newConfig.size() != 0) {
            for (String key : newConfig.keySet()) {
            	plugin.getConfig().set(key, newConfig.get(key));
            }
            
            try {
            	plugin.getConfig().set("version", configVersion);
            	plugin.getConfig().save(new File(plugin.getDataFolder(), "config.yml"));
            }
            catch (IOException ex) {
            	ex.printStackTrace();
            }
        }
	}

    private Map<String, Object> getConfigVals() {
        Map<String, Object> var = new HashMap<>();
        YamlConfiguration config = new YamlConfiguration();
        
        try {
            config.loadFromString(stringFromInputStream(plugin.getClass().getResourceAsStream("/config.yml")));
        }
        catch (InvalidConfigurationException ex) {
        	ex.printStackTrace();
        }
        
        for (String key : config.getKeys(false)) {
            var.put(key, config.get(key));
        }
        
        return var;
    }
    
    private String stringFromInputStream(InputStream in) {
    	Scanner scanner = new Scanner(in);
    	scanner.useDelimiter("\\A");
    	String confString = scanner.next();
    	scanner.close();
    	return confString;
    }
	
}
