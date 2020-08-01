package com.ninjaguild.dragoneggdrop.world;

import com.ninjaguild.dragoneggdrop.utils.DEDConstants;
import com.ninjaguild.dragoneggdrop.utils.math.MathUtils;

import org.bukkit.configuration.file.FileConfiguration;

/**
 * The trigger reason that allowed an Ender Dragon to start its respawning process.
 */
public enum RespawnReason {

    /**
     * A player joined the world
     */
    JOIN(DEDConstants.CONFIG_JOIN_RESPAWN_DELAY, 60), // 60 second default

    /**
     * The ender dragon was killed
     */
    DEATH(DEDConstants.CONFIG_DEATH_RESPAWN_DELAY, 300); // 5 minute default


    private final String configPath;
    private final int defaultSeconds;

    private RespawnReason(String configPath, int defaultSeconds) {
        this.configPath = configPath;
        this.defaultSeconds = defaultSeconds;
    }

    public int getRespawnTime(FileConfiguration config) {
        return MathUtils.parseRespawnSeconds(config.getString(configPath), defaultSeconds);
    }

}
