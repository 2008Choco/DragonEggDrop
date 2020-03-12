package com.ninjaguild.dragoneggdrop.management;

import java.util.UUID;

import com.google.common.base.Preconditions;
import com.ninjaguild.dragoneggdrop.DragonEggDrop;
import com.ninjaguild.dragoneggdrop.dragon.DragonTemplate;
import com.ninjaguild.dragoneggdrop.management.DEDManager.RespawnReason;
import com.ninjaguild.dragoneggdrop.utils.runnables.RespawnRunnable;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.EnderDragon;

/**
 * Represents a wrapped {@link World} object with {@link Environment#THE_END} to separate
 * the runnables present in each independent world. Allows for separation of DED respawns.
 *
 * @author Parker Hawke - Choco
 */
public class EndWorldWrapper {

    private boolean respawnInProgress = false;
    private DragonTemplate activeBattle, lastBattle;
    private RespawnRunnable respawnTask;

    private final DragonEggDrop plugin;
    private final UUID world;

    /**
     * Construct a new EndWorldWrapper around an existing world
     *
     * @param plugin the plugin instance
     * @param world the world to wrap
     */
    protected EndWorldWrapper(DragonEggDrop plugin, World world) {
        Preconditions.checkArgument(world.getEnvironment() == Environment.THE_END, "EndWorldWrapper worlds must be of environment \"THE_END\"");

        this.plugin = plugin;
        this.world = world.getUID();
    }

    /**
     * Get the world represented by this wrapper.
     *
     * @return the represented world
     */
    public World getWorld() {
        return Bukkit.getWorld(world);
    }

    /**
     * Commence the Dragon's respawning processes in this world with a specific
     * dragon template and respawn delay. This respawn may or may not fail depending
     * on whether or not a dragon already exists or a respawn is already in progress.
     *
     * @param respawnDelay the delay (in seconds) until the dragon spawns (countdown time)
     * @param template the dragon template to spawn. If null, a regular dragon will
     * be respawned
     *
     * @return the result of the respawn. true if successful, false otherwise
     */
    public boolean startRespawn(int respawnDelay, DragonTemplate template) {
        Preconditions.checkArgument(respawnDelay >= 0, "Respawn delays must be greater than or equal to 0");

        boolean dragonExists = !getWorld().getEntitiesByClasses(EnderDragon.class).isEmpty();
        if (dragonExists || respawnInProgress || respawnTask != null) {
            return false;
        }

        this.setActiveBattle(template);
        this.respawnTask = new RespawnRunnable(plugin, getWorld(), respawnDelay);
        this.respawnTask.runTaskTimer(plugin, 0, 20);
        this.respawnInProgress = true;
        return true;
    }

    /**
     * Commence the Dragon's respawning processes in this world with a specific
     * dragon template. This respawn may or may not fail depending on whether or
     * not a dragon already exists or a respawn is already in progress.
     *
     * @param type the type that triggered this dragon respawn
     * @param template the dragon template to spawn. If null, a regular dragon will
     * be respawned
     *
     * @return the result of the respawn. true if successful, false otherwise
     */
    public boolean startRespawn(RespawnReason type, DragonTemplate template) {
        Preconditions.checkArgument(type != null, "Cannot respawn a dragon under a null respawn type");
        return startRespawn(type.getRespawnTime(plugin.getConfig()), template);
    }

    /**
     * Commence the Dragon's respawning process in this world with a randomly
     * selected dragon template and a set respawn delay. This respawn may or
     * may not fail depending on whether or not a dragon already exists or a
     * respawn is already in progress.
     *
     * @param respawnDelay the delay (in seconds) until the dragon spawns (countdown time)
     *
     * @return the result of the respawn. true if successful, false otherwise
     *
     * @see #startRespawn(int, DragonTemplate)
     */
    public boolean startRespawn(int respawnDelay) {
        return startRespawn(respawnDelay, plugin.getDEDManager().getRandomTemplate());
    }

    /**
     * Commence the Dragon's respawning processes in this world with a randomly
     * selected dragon template. This respawn may or may not fail depending on
     * whether or not a dragon already exists or a respawn is already in progress.
     *
     * @param type the type that triggered this dragon respawn
     *
     * @return the result of the respawn. true if successful, false otherwise
     *
     * @see #startRespawn(RespawnReason, DragonTemplate)
     * @see #startRespawn(int)
     */
    public boolean startRespawn(RespawnReason type) {
        return startRespawn(type, plugin.getDEDManager().getRandomTemplate());
    }

    /**
     * Halt the Dragon respawning process if any are currently running.
     */
    public void stopRespawn() {
        this.respawnInProgress = false;

        if (respawnTask != null) {
            this.respawnTask.cancel();
            this.respawnTask = null;
        }
    }

    /**
     * Check whether a respawn is currently in progress or not.
     *
     * @return true if actively respawning
     */
    public boolean isRespawnInProgress() {
        return respawnInProgress;
    }

    /**
     * Get the amount of time remaining until the dragon respawns.
     *
     * @return the time remaining (in seconds), or -1 if no time remaining at all
     */
    public int getTimeUntilRespawn() {
        return (respawnTask != null ? respawnTask.getSecondsUntilRespawn() : -1);
    }

    /**
     * Set the battle that is active according to DragonEggDrop. This battle
     * instance will be used to generate names and lore for loot respectively.
     * Additionally, the last battle will be set to the current active battle
     * (unless null)
     *
     * @param activeBattle the battle to set
     */
    public void setActiveBattle(DragonTemplate activeBattle) {
        if (this.activeBattle != null) {
            this.lastBattle = this.activeBattle;
        }

        this.activeBattle = activeBattle;
    }

    /**
     * Get the template represented in the active battle.
     *
     * @return the current battle
     */
    public DragonTemplate getActiveBattle() {
        return activeBattle;
    }

    /**
     * Get the template represented in the last successful battle.
     *
     * @return the last battle
     */
    public DragonTemplate getLastBattle() {
        return lastBattle;
    }

}
