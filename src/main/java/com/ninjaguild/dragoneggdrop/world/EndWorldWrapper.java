package com.ninjaguild.dragoneggdrop.world;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.google.common.base.Preconditions;
import com.ninjaguild.dragoneggdrop.DragonEggDrop;
import com.ninjaguild.dragoneggdrop.dragon.DragonTemplate;
import com.ninjaguild.dragoneggdrop.dragon.loot.DragonLootTable;
import com.ninjaguild.dragoneggdrop.tasks.RespawnRunnable;

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

    private static final Map<UUID, EndWorldWrapper> WRAPPERS = new HashMap<>();

    private DragonTemplate activeTemplate, respawningTemplate, previousTemplate;
    private DragonLootTable lootTableOverride = null;

    private UUID previousDragonUUID;

    private RespawnRunnable respawnTask;

    private final DragonEggDrop plugin;
    private final UUID world;

    /**
     * Construct a new EndWorldWrapper around an existing world
     *
     * @param plugin the plugin instance
     * @param world the world to wrap
     */
    protected EndWorldWrapper(World world) {
        Preconditions.checkArgument(world.getEnvironment() == Environment.THE_END, "EndWorldWrapper worlds must be of environment \"THE_END\"");

        this.plugin = DragonEggDrop.getInstance();
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
     * Commence the Dragon's respawning processes in this world with a specific dragon
     * template and respawn delay. This respawn may or may not fail depending on whether
     * or not a dragon already exists or a respawn is already in progress.
     *
     * @param respawnDelay the delay (in seconds) until the dragon spawns (countdown time)
     * @param template the dragon template to spawn. Must not be null
     * @param lootTable the loot table to use on death. This overrides any already-set
     * loot table from {@link #setLootTableOverride(DragonLootTable)}. If null, the
     * override will not be set and any existing override will be used (or the template
     * loot table if one was not set prior)
     *
     * @return the result of the respawn. true if successful, false otherwise
     */
    public boolean startRespawn(int respawnDelay, DragonTemplate template, DragonLootTable lootTable) {
        Preconditions.checkArgument(respawnDelay >= 0, "Respawn delays must be greater than or equal to 0");
        Preconditions.checkArgument(template != null, "Cannot respawn null template");

        boolean dragonExists = !getWorld().getEntitiesByClasses(EnderDragon.class).isEmpty();
        if (dragonExists || respawnTask != null) {
            return false;
        }

        if (lootTable != null) { // Don't reset if it's null
            this.setLootTableOverride(lootTable);
        }

        this.setRespawningTemplate(template);
        this.respawnTask = new RespawnRunnable(plugin, getWorld(), respawnDelay);
        this.respawnTask.runTaskTimer(plugin, 0, 20);
        return true;
    }

    /**
     * Commence the Dragon's respawning processes in this world with a specific dragon
     * template and respawn delay. This respawn may or may not fail depending on whether
     * or not a dragon already exists or a respawn is already in progress.
     *
     * @param respawnDelay the delay (in seconds) until the dragon spawns (countdown time)
     * @param template the dragon template to spawn. If null, a regular dragon will be
     * respawned
     *
     * @return the result of the respawn. true if successful, false otherwise
     */
    public boolean startRespawn(int respawnDelay, DragonTemplate template) {
        return startRespawn(respawnDelay, template, null);
    }

    /**
     * Commence the Dragon's respawning processes in this world with a specific dragon
     * template. This respawn may or may not fail depending on whether or not a dragon
     * already exists or a respawn is already in progress.
     *
     * @param reason the reason that triggered this dragon respawn
     * @param template the dragon template to spawn. If null, a regular dragon will be
     * respawned
     *
     * @return the result of the respawn. true if successful, false otherwise
     */
    public boolean startRespawn(RespawnReason reason, DragonTemplate template) {
        Preconditions.checkArgument(reason != null, "Cannot respawn a dragon under a null respawn type");
        return startRespawn(reason.getRespawnTime(plugin.getConfig()), template, null);
    }

    /**
     * Commence the Dragon's respawning process in this world with a randomly selected
     * dragon template and a set respawn delay. This respawn may or may not fail depending
     * on whether or not a dragon already exists or a respawn is already in progress.
     *
     * @param respawnDelay the delay (in seconds) until the dragon spawns (countdown time)
     *
     * @return the result of the respawn. true if successful, false otherwise
     *
     * @see #startRespawn(int, DragonTemplate)
     */
    public boolean startRespawn(int respawnDelay) {
        return startRespawn(respawnDelay, DragonTemplate.randomTemplate(), null);
    }

    /**
     * Commence the Dragon's respawning processes in this world with a randomly selected
     * dragon template. This respawn may or may not fail depending on whether or not a
     * dragon already exists or a respawn is already in progress.
     *
     * @param reason the reason that triggered this dragon respawn
     *
     * @return the result of the respawn. true if successful, false otherwise
     *
     * @see #startRespawn(RespawnReason, DragonTemplate)
     * @see #startRespawn(int)
     */
    public boolean startRespawn(RespawnReason reason) {
        return startRespawn(reason.getRespawnTime(plugin.getConfig()), DragonTemplate.randomTemplate(), null);
    }

    /**
     * Halt the Dragon respawning process if any are currently running.
     */
    public void stopRespawn() {
        if (respawnTask != null) {
            this.respawnTask.cancel();
        }

        this.respawnTask = null;
    }

    /**
     * Check whether a respawn is currently in progress or not.
     *
     * @return true if actively respawning
     */
    public boolean isRespawnInProgress() {
        return respawnTask != null;
    }

    /**
     * Get the amount of time remaining until the dragon respawns.
     *
     * @return the time remaining (in seconds). -1 if a respawn is not in progress
     */
    public int getTimeUntilRespawn() {
        return (respawnTask != null ? respawnTask.getSecondsUntilRespawn() : -1);
    }

    /**
     * Set the battle that is active according to DragonEggDrop. This battle instance will
     * be used to generate names and lore for loot respectively. Additionally, the last
     * battle will be set to the current active battle (unless null)
     *
     * @param template the battle to set
     * @param updatePreviousTemplate whether to set the previous template to the current
     * active template (if not null)
     */
    public void setActiveTemplate(DragonTemplate template, boolean updatePreviousTemplate) {
        if (updatePreviousTemplate && activeTemplate != null) {
            this.previousTemplate = activeTemplate;
        }

        this.activeTemplate = template;
    }

    /**
     * Set the battle that is active according to DragonEggDrop. This battle instance will
     * be used to generate names and lore for loot respectively. Additionally, the last
     * battle will be set to the current active battle (unless null)
     *
     * @param template the battle to set
     */
    public void setActiveTemplate(DragonTemplate template) {
        this.setActiveTemplate(template, true);
    }

    /**
     * Get the template represented in the active battle.
     *
     * @return the current battle
     */
    public DragonTemplate getActiveTemplate() {
        return activeTemplate;
    }

    /**
     * Set the template to respawn during the next process. If a respawn is not in
     * process, this value will be overridden after a call to
     * {@link #startRespawn(int, DragonTemplate, DragonLootTable)} (or any of its
     * overrides).
     *
     * @param respawningTemplate the template to set
     */
    public void setRespawningTemplate(DragonTemplate respawningTemplate) {
        this.respawningTemplate = respawningTemplate;
    }

    /**
     * Get the template preparing to be respawned.
     *
     * @return the respawning template
     */
    public DragonTemplate getRespawningTemplate() {
        return respawningTemplate;
    }

    /**
     * Get the template represented in the last successful battle.
     *
     * @return the last battle
     */
    public DragonTemplate getPreviousTemplate() {
        return previousTemplate;
    }

    /**
     * Set the UUID of the dragon that was most recently slain. This is mostly for
     * internal use. Please avoid calling this.
     *
     * @param previousDragonUUID the UUID of the dragon that was slain
     */
    public void setPreviousDragonUUID(UUID previousDragonUUID) {
        this.previousDragonUUID = previousDragonUUID;
    }

    /**
     * Get the UUID of the dragon that was most recently slain.
     *
     * @return the UUID of the dragon that was slain
     */
    public UUID getPreviousDragonUUID() {
        return previousDragonUUID;
    }

    /**
     * Set the next loot table from which to generate loot. This loot table will override
     * that of the next dragon template.
     *
     * @param nextLootTable the next loot table to use
     */
    public void setLootTableOverride(DragonLootTable nextLootTable) {
        this.lootTableOverride = nextLootTable;
    }

    /**
     * Get the next loot table from which to generate loot. If null, the next dragon
     * template's loot table will be used.
     *
     * @return the next loot table
     */
    public DragonLootTable getLootTableOverride() {
        return lootTableOverride;
    }

    /**
     * Check whether or not the next loot table will be overridden by this world's loot
     * table.
     *
     * @return true if a loot table has been specified, false otherwise
     */
    public boolean hasLootTableOverride() {
        return lootTableOverride != null;
    }

    /**
     * Get the world wrapper for the specified world.
     *
     * @param world the world to get
     *
     * @return the world's respective wrapper
     */
    public static EndWorldWrapper of(World world) {
        Preconditions.checkArgument(world != null, "Cannot get wrapper for non-existent (null) world");
        return WRAPPERS.computeIfAbsent(world.getUID(), uuid -> new EndWorldWrapper(world));
    }

    /**
     * Get an unmodifiable collection of all world wrappers.
     *
     * @return all world wrappers
     */
    public static Collection<EndWorldWrapper> getAll() {
        return Collections.unmodifiableCollection(WRAPPERS.values());
    }

    /**
     * Clear all world wrapper data. This deletes all information to do with active
     * battles, as well as the state of a world according to DragonEggDrop.
     */
    public static void clear() {
        WRAPPERS.clear();
    }

}
