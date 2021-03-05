package wtf.choco.dragoneggdrop.tasks;

import com.google.common.base.Preconditions;

import java.util.Collection;

import org.bukkit.World;
import org.bukkit.boss.DragonBattle;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import wtf.choco.dragoneggdrop.DragonEggDrop;
import wtf.choco.dragoneggdrop.world.EndWorldWrapper;

/**
 * Represents a BukkitRunnable that ensures the respawn state of the Ender Dragon due to
 * issues in vanilla dragon respawning mechanics. If a dragon has not respawned within 40
 * seconds (800 ticks), the respawning process will restart.
 */
final class RespawnSafeguardRunnable extends BukkitRunnable {

    // Respawn takes about 30 seconds. Timeout at 35 seconds
    private static final long TIMEOUT_PERIOD_TICKS = 700L;

    private final DragonEggDrop plugin;
    private final EndWorldWrapper world;
    private final DragonBattle battle;

    private RespawnSafeguardRunnable(DragonEggDrop plugin, World world, DragonBattle battle) {
        this.plugin = plugin;
        this.world = EndWorldWrapper.of(world);
        this.battle = battle;

        this.runTaskLater(plugin, TIMEOUT_PERIOD_TICKS);
    }

    @Override
    public void run() {
        World bukkitWorld = world.getWorld();
        Collection<@NotNull EnderCrystal> crystals = bukkitWorld.getEntitiesByClass(EnderCrystal.class);

        // Ender dragon was not found. Forcibly respawn it
        if (bukkitWorld.getEntitiesByClass(EnderDragon.class).size() == 0) {
            this.plugin.getLogger().warning("Something went wrong! Had to forcibly reset dragon battle...");

            this.battle.resetCrystals();
            crystals.forEach(Entity::remove); // Remove pre-existing crystals

            this.world.startRespawn(0);
            return;
        }

        // Ensure all crystals are not invulnerable
        crystals.forEach(c -> {
            c.setInvulnerable(false);
            c.setBeamTarget(null);
        });
    }

    /**
     * Commence a new RespawnSafeguardRunnable. This should only be invoked in a
     * RespawnRunnable.
     *
     * @param plugin the plugin instance
     * @param world the battle's world
     * @param battle the battle to check
     *
     * @return the running RespawnSafeguardRunnable instance
     */
    @NotNull
    protected static RespawnSafeguardRunnable newTimeout(@NotNull DragonEggDrop plugin, @NotNull World world, @NotNull DragonBattle battle) {
        Preconditions.checkArgument(plugin != null, "plugin must not be null");
        Preconditions.checkArgument(world != null, "world must not be null");
        Preconditions.checkArgument(battle != null, "battle must not be null");

        return new RespawnSafeguardRunnable(plugin, world, battle);
    }

}
