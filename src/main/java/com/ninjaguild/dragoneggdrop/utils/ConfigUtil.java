package com.ninjaguild.dragoneggdrop.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Set;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Represents a YAML configuration file to allow for reload functions whilst also
 * keeping Bukkit comments.
 */
@Deprecated
public final class ConfigUtil {

    private ConfigUtil() { }

    /**
     * Update the configuration file.
     *
     * @param plugin the plugin instance
     * @param currentVersion the version of the configuration file
     */
    public static void updateConfig(JavaPlugin plugin, int currentVersion) {
        InputStream in = plugin.getResource("config.yml");
        try (InputStreamReader inReader = new InputStreamReader(in)) {
            FileConfiguration defaultConfig = YamlConfiguration.loadConfiguration(inReader);

            if (defaultConfig.getInt("version") == currentVersion) {
                return;
            }

            Set<String> newKeys = defaultConfig.getKeys(false);
            for (String key : plugin.getConfig().getKeys(false)) {
                if (key.equalsIgnoreCase("version")) continue;

                if (newKeys.contains(key)) {
                    defaultConfig.set(key, plugin.getConfig().get(key));
                }
            }

            defaultConfig.save(new File(plugin.getDataFolder(), "/config.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
