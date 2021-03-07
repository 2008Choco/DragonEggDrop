package wtf.choco.dragoneggdrop.tasks;

import com.google.common.base.Preconditions;

import java.util.EnumSet;
import java.util.Set;

import org.bukkit.World;
import org.bukkit.boss.DragonBattle;
import org.bukkit.boss.DragonBattle.RespawnPhase;
import org.bukkit.entity.EnderDragon;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import wtf.choco.dragoneggdrop.DragonEggDrop;
import wtf.choco.dragoneggdrop.utils.DEDConstants;
import wtf.choco.dragoneggdrop.world.EndWorldWrapper;

/**
 * Represents a BukkitRunnable that checks for the presence of a dragon in an end world.
 * If the dragon is not present, any {@link EndWorldWrapper} states are reset to ensure
 * that commands or non-standard means of dragon removal are supported (i.e. /kill).
 *
 * @author Parker Hawke - Choco
 */
public final class DragonCheckRunnable extends BukkitRunnable {

    private static final Set<@NotNull RespawnPhase> RESPAWN_PHASES_TO_CHECK = EnumSet.of(RespawnPhase.NONE, RespawnPhase.END);

    private final DragonEggDrop plugin;
    private final EndWorldWrapper worldWrapper;

    /**
     * Construct a new {@link DragonCheckRunnable}.
     *
     * @param plugin the plugin instance
     * @param worldWrapper the world wrapper in which this runnable should check for a
     * dragon
     */
    public DragonCheckRunnable(@NotNull DragonEggDrop plugin, @NotNull EndWorldWrapper worldWrapper) {
        Preconditions.checkArgument(plugin != null, "plugin must not be null");
        Preconditions.checkArgument(worldWrapper != null, "worldWrapper must not be null");

        this.plugin = plugin;
        this.worldWrapper = worldWrapper;
    }

    @Override
    public void run() {
        World world = worldWrapper.getWorld();
        if (plugin.getConfig().getStringList(DEDConstants.CONFIG_DISABLED_WORLDS).contains(world.getName())) {
            return;
        }

        DragonBattle dragonBattle = world.getEnderDragonBattle();
        if (dragonBattle == null) {
            return;
        }

        RespawnPhase phase = dragonBattle.getRespawnPhase();
        if (worldWrapper.isRespawnInProgress() || worldWrapper.isDragonDying() || !RESPAWN_PHASES_TO_CHECK.contains(phase)) {
            return;
        }

        EnderDragon dragon = dragonBattle.getEnderDragon();
        if (dragon == null || dragon.isDead()) {
            this.worldWrapper.setActiveTemplate(null);
            this.worldWrapper.setLootTableOverride(null);
            this.worldWrapper.setDragonDying(false);
            this.worldWrapper.stopRespawn(); // Just in case
        }
    }

}
