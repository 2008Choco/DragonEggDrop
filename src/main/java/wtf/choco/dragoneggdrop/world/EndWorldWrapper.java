package wtf.choco.dragoneggdrop.world;

import com.google.common.base.Preconditions;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.EnderDragon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import wtf.choco.dragoneggdrop.DragonEggDrop;
import wtf.choco.dragoneggdrop.dragon.DragonTemplate;
import wtf.choco.dragoneggdrop.dragon.loot.DragonLootTable;
import wtf.choco.dragoneggdrop.tasks.RespawnRunnable;

/**
 * Represents a wrapped {@link World} object with {@link Environment#THE_END} to separate
 * the runnables present in each independent world. Allows for separation of DED respawns.
 *
 * @author Parker Hawke - Choco
 */
public class EndWorldWrapper {

    private static final Map<@NotNull UUID, @NotNull EndWorldWrapper> WRAPPERS = new HashMap<>();

    private DragonTemplate activeTemplate, respawningTemplate, previousTemplate;
    private DragonLootTable lootTableOverride = null;

    private UUID previousDragonUUID;

    private RespawnRunnable respawnTask;
    private DragonRespawnData dragonRespawnData;

    private final DragonEggDrop plugin;
    private final UUID world;

    /**
     * Construct a new EndWorldWrapper around an existing world
     *
     * @param world the world to wrap
     */
    protected EndWorldWrapper(@NotNull World world) {
        Preconditions.checkArgument(world != null, "world must not be null");
        Preconditions.checkArgument(world.getEnvironment() == Environment.THE_END, "EndWorldWrapper worlds must be of environment \"THE_END\"");

        this.plugin = DragonEggDrop.getInstance();
        this.world = world.getUID();
    }

    /**
     * Get the world represented by this wrapper.
     *
     * @return the represented world
     */
    @NotNull
    public World getWorld() {
        World world = Bukkit.getWorld(this.world);

        if (world == null) {
            throw new IllegalStateException("The world doesn't exist?");
        }

        return world;
    }

    /**
     * Commence the Dragon's respawning processes in this world with a specific dragon
     * template and respawn delay. This respawn may or may not fail depending on whether
     * or not a dragon already exists or a respawn is already in progress.
     *
     * @param respawnData the respawn data
     * @param template the dragon template to spawn. Must not be null
     * @param lootTable the loot table to use on death. This overrides any already-set
     * loot table from {@link #setLootTableOverride(DragonLootTable)}. If null, the
     * override will not be set and any existing override will be used (or the template
     * loot table if one was not set prior)
     *
     * @return the result of the respawn. true if successful, false otherwise
     */
    public boolean startRespawn(@NotNull DragonRespawnData respawnData, @NotNull DragonTemplate template, @Nullable DragonLootTable lootTable) {
        Preconditions.checkArgument(respawnData != null, "respawnData must not be null");
        Preconditions.checkArgument(template != null, "template must not be null");

        boolean dragonExists = !getWorld().getEntitiesByClasses(EnderDragon.class).isEmpty();
        if (dragonExists || respawnTask != null || dragonRespawnData != null) {
            return false;
        }

        if (lootTable != null) { // Don't reset if it's null
            this.setLootTableOverride(lootTable);
        }

        this.dragonRespawnData = respawnData;
        this.setRespawningTemplate(template);
        this.respawnTask = new RespawnRunnable(plugin, this);
        this.respawnTask.runTaskTimer(plugin, 0, 20);
        return true;
    }

    /**
     * Commence the Dragon's respawning processes in this world with a specific dragon
     * template and respawn delay. This respawn may or may not fail depending on whether
     * or not a dragon already exists or a respawn is already in progress.
     *
     * @param time the time (in seconds) until the dragon spawns
     * @param template the dragon template to spawn. Must not be null
     * @param lootTable the loot table to use on death. This overrides any already-set
     * loot table from {@link #setLootTableOverride(DragonLootTable)}. If null, the
     * override will not be set and any existing override will be used (or the template
     * loot table if one was not set prior)
     *
     * @return the result of the respawn. true if successful, false otherwise
     */
    public boolean startRespawn(long time, @NotNull DragonTemplate template, @Nullable DragonLootTable lootTable) {
        return startRespawn(new DragonRespawnData(this, System.currentTimeMillis(), time * 1000), template, lootTable);
    }

    /**
     * Commence the Dragon's respawning processes in this world with a specific dragon
     * template and respawn delay. This respawn may or may not fail depending on whether
     * or not a dragon already exists or a respawn is already in progress.
     *
     * @param respawnData the respawn data
     * @param template the dragon template to spawn. If null, a regular dragon will be
     * respawned
     *
     * @return the result of the respawn. true if successful, false otherwise
     */
    public boolean startRespawn(@NotNull DragonRespawnData respawnData, @NotNull DragonTemplate template) {
        return startRespawn(respawnData, template, null);
    }

    /**
     * Commence the Dragon's respawning processes in this world with a specific dragon
     * template and respawn delay. This respawn may or may not fail depending on whether
     * or not a dragon already exists or a respawn is already in progress.
     *
     * @param time the time (in seconds) until the dragon spawns
     * @param template the dragon template to spawn. If null, a regular dragon will be
     * respawned
     *
     * @return the result of the respawn. true if successful, false otherwise
     */
    public boolean startRespawn(long time, @NotNull DragonTemplate template) {
        return startRespawn(time, template, null);
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
    public boolean startRespawn(@NotNull RespawnReason reason, @NotNull DragonTemplate template) {
        Preconditions.checkArgument(reason != null, "Cannot respawn a dragon under a null respawn type");
        return startRespawn(reason.getRespawnTime(plugin.getConfig()), template, null);
    }

    /**
     * Commence the Dragon's respawning process in this world with a randomly selected
     * dragon template and a set respawn delay. This respawn may or may not fail depending
     * on whether or not a dragon already exists or a respawn is already in progress.
     *
     * @param respawnData the respawn data
     *
     * @return the result of the respawn. true if successful, false otherwise
     *
     * @see #startRespawn(DragonRespawnData, DragonTemplate)
     */
    public boolean startRespawn(@NotNull DragonRespawnData respawnData) {
        DragonTemplate template = DragonEggDrop.getInstance().getDragonTemplateRegistry().getRandomTemplate();
        return template != null && startRespawn(respawnData, template, null);
    }

    /**
     * Commence the Dragon's respawning process in this world with a randomly selected
     * dragon template and a set respawn delay. This respawn may or may not fail depending
     * on whether or not a dragon already exists or a respawn is already in progress.
     *
     * @param time the time (in seconds) until the dragon spawns
     *
     * @return the result of the respawn. true if successful, false otherwise
     *
     * @see #startRespawn(long, DragonTemplate)
     */
    public boolean startRespawn(long time) {
        DragonTemplate template = DragonEggDrop.getInstance().getDragonTemplateRegistry().getRandomTemplate();
        return template != null && startRespawn(time, template, null);
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
     * @see #startRespawn(long)
     */
    public boolean startRespawn(@NotNull RespawnReason reason) {
        DragonTemplate template = DragonEggDrop.getInstance().getDragonTemplateRegistry().getRandomTemplate();
        return template != null && startRespawn(reason.getRespawnTime(plugin.getConfig()), template, null);
    }

    /**
     * Halt the Dragon respawning process if any are currently running.
     */
    public void stopRespawn() {
        // Cancels automatically but I want to be absolutely certain
        if (respawnTask != null) {
            this.respawnTask.cancel();
        }

        this.respawnTask = null;
        this.dragonRespawnData = null;
    }

    /**
     * Check whether or not a respawn is currently in progress.
     *
     * @return true if a respawn is in progress, false otherwise
     */
    public boolean isRespawnInProgress() {
        return dragonRespawnData != null && respawnTask != null;
    }

    /**
     * Set the dragon respawn data. Note that if a respawn has not yet started, this method
     * is effectively useless as it will be overridden by
     * {@link #startRespawn(DragonRespawnData, DragonTemplate, DragonLootTable)} or any of
     * its overloads.
     * <p>
     * This method is best used such that {@link #isRespawnInProgress()} is true in order
     * to change the respawn timings.
     *
     * @param dragonRespawnData the respawn data to set
     */
    public void setDragonRespawnData(@Nullable DragonRespawnData dragonRespawnData) {
        this.dragonRespawnData = dragonRespawnData;
    }

    /**
     * Get the current respawn data and timings. This method will only return a value such
     * that {@link #isRespawnInProgress()} is true.
     *
     * @return the respawn data. null if no respawn is in progress
     */
    @Nullable
    public DragonRespawnData getDragonRespawnData() {
        return dragonRespawnData;
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
    public void setActiveTemplate(@Nullable DragonTemplate template, boolean updatePreviousTemplate) {
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
    public void setActiveTemplate(@Nullable DragonTemplate template) {
        this.setActiveTemplate(template, true);
    }

    /**
     * Get the template represented in the active battle.
     *
     * @return the current battle
     */
    @Nullable
    public DragonTemplate getActiveTemplate() {
        return activeTemplate;
    }

    /**
     * Set the template to respawn during the next process. If a respawn is not in
     * process, this value will be overridden after a call to
     * {@link #startRespawn(long, DragonTemplate, DragonLootTable)} (or any of its
     * overrides).
     *
     * @param respawningTemplate the template to set
     */
    public void setRespawningTemplate(@Nullable DragonTemplate respawningTemplate) {
        this.respawningTemplate = respawningTemplate;
    }

    /**
     * Get the template preparing to be respawned.
     *
     * @return the respawning template
     */
    @Nullable
    public DragonTemplate getRespawningTemplate() {
        return respawningTemplate;
    }

    /**
     * Set the template represented in the last successful battle.
     *
     * @param previousTemplate the previous template
     */
    public void setPreviousTemplate(@Nullable DragonTemplate previousTemplate) {
        this.previousTemplate = previousTemplate;
    }

    /**
     * Get the template represented in the last successful battle.
     *
     * @return the last battle
     */
    @Nullable
    public DragonTemplate getPreviousTemplate() {
        return previousTemplate;
    }

    /**
     * Set the UUID of the dragon that was most recently slain. This is mostly for
     * internal use. Please avoid calling this.
     *
     * @param previousDragonUUID the UUID of the dragon that was slain
     */
    public void setPreviousDragonUUID(@Nullable UUID previousDragonUUID) {
        this.previousDragonUUID = previousDragonUUID;
    }

    /**
     * Get the UUID of the dragon that was most recently slain.
     *
     * @return the UUID of the dragon that was slain
     */
    @Nullable
    public UUID getPreviousDragonUUID() {
        return previousDragonUUID;
    }

    /**
     * Set the next loot table from which to generate loot. This loot table will override
     * that of the next dragon template.
     *
     * @param nextLootTable the next loot table to use
     */
    public void setLootTableOverride(@Nullable DragonLootTable nextLootTable) {
        this.lootTableOverride = nextLootTable;
    }

    /**
     * Get the next loot table from which to generate loot. If null, the next dragon
     * template's loot table will be used.
     *
     * @return the next loot table
     */
    @Nullable
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
    @NotNull
    public static EndWorldWrapper of(@NotNull World world) {
        Preconditions.checkArgument(world != null, "Cannot get wrapper for non-existent (null) world");
        return WRAPPERS.computeIfAbsent(world.getUID(), uuid -> new EndWorldWrapper(world));
    }

    /**
     * Get an unmodifiable collection of all world wrappers.
     *
     * @return all world wrappers
     */
    @NotNull
    public static Collection<@NotNull EndWorldWrapper> getAll() {
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
