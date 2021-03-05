package wtf.choco.dragoneggdrop.world;

import com.google.common.base.Preconditions;

import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import wtf.choco.commons.util.MathUtil;
import wtf.choco.dragoneggdrop.utils.DEDConstants;

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

    private RespawnReason(@NotNull String configPath, int defaultSeconds) {
        Preconditions.checkArgument(configPath != null, "configPath must not be null");

        this.configPath = configPath;
        this.defaultSeconds = defaultSeconds;
    }

    public int getRespawnTime(@NotNull FileConfiguration config) {
        Preconditions.checkArgument(config != null, "config must not be null");
        return MathUtil.parseSeconds(config.getString(configPath), defaultSeconds);
    }

}
