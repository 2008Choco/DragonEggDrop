package wtf.choco.dragoneggdrop.world;

import com.google.common.base.Preconditions;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EnderDragon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import wtf.choco.dragoneggdrop.DragonEggDrop;
import wtf.choco.dragoneggdrop.dragon.DragonTemplate;
import wtf.choco.dragoneggdrop.dragon.loot.DragonLootTable;
import wtf.choco.dragoneggdrop.tasks.DragonCheckRunnable;
import wtf.choco.dragoneggdrop.tasks.RespawnRunnable;
import wtf.choco.dragoneggdrop.utils.DEDConstants;

/**
 * Represents a wrapped {@link World} object with {@link Environment#THE_END} to separate
 * the runnables present in each independent world. Allows for separation of DED respawns.
 *
 * @author Parker Hawke - Choco
 */
public class EndWorldWrapper {

    private static final Map<@NotNull UUID, @NotNull EndWorldWrapper> WRAPPERS = new HashMap<>();

    private DragonTemplate activeTemplate, respawningTemplate;
    private DragonLootTable lootTableOverride = null;

    private final LinkedList<DragonBattleRecord> battleHistory; // Intentionally not annotated
    private final int maxBattleHistorySize;

    private RespawnRunnable respawnTask;
    private DragonRespawnData dragonRespawnData;

    private DragonCheckRunnable dragonCheckRunnable;
    private boolean dragonDying = false;

    private final DragonEggDrop plugin;
    private final UUID world;

    /**
     * Construct a new EndWorldWrapper around an existing world
     *
     * @param world the world to wrap
     * @param maxHistorySize the maximum amount of history to store
     */
    protected EndWorldWrapper(@NotNull World world, int maxHistorySize) {
        Preconditions.checkArgument(world != null, "world must not be null");
        Preconditions.checkArgument(world.getEnvironment() == Environment.THE_END, "EndWorldWrapper worlds must be of environment \"THE_END\"");

        this.plugin = DragonEggDrop.getInstance();
        this.world = world.getUID();
        this.battleHistory = new LinkedList<>();
        this.maxBattleHistorySize = maxHistorySize;

        this.dragonCheckRunnable = new DragonCheckRunnable(plugin, this);
        this.dragonCheckRunnable.runTaskTimer(plugin, 0L, 20L);
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
     * Set whether or not the dragon is dying.
     *
     * @param dragonDying true if dying
     */
    public void setDragonDying(boolean dragonDying) {
        this.dragonDying = dragonDying;
    }

    /**
     * Check whether or not the dragon is dying and its death animation is playing.
     *
     * @return true if dying, false otherwise
     */
    public boolean isDragonDying() {
        return dragonDying;
    }

    /**
     * Set the battle that is active according to DragonEggDrop. This battle instance will
     * be used to generate names and lore for loot respectively.
     *
     * @param template the battle to set
     */
    public void setActiveTemplate(@Nullable DragonTemplate template) {
        this.activeTemplate = template;
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
     * Record a {@link DragonBattleRecord} to this world at the top of the history. If there are
     * more recorded dragon battles than {@link #getMaxBattleHistorySize()}, the oldest entry will
     * be pushed out and returned. Note that this method will push out any records that exceed the
     * maximum battle history size but will only return the oldest.
     *
     * @param record the record to record
     *
     * @return the oldest record that was pushed out. null if none
     */
    @Nullable
    public DragonBattleRecord recordDragonBattle(@NotNull DragonBattleRecord record) {
        Preconditions.checkArgument(record != null, "record must not be null");

        DragonBattleRecord lastRecord = null;
        while (battleHistory.size() >= maxBattleHistorySize) {
            if (lastRecord == null) {
                lastRecord = battleHistory.pollLast();
                continue;
            }

            this.battleHistory.pollLast();
        }

        this.battleHistory.push(record);
        return lastRecord;
    }

    /**
     * Get a previous dragon battle at the given index. If the index is negative or exceeds
     * {@link #getMaxBattleHistorySize()}, null will be returned.
     *
     * @param index the index of the record to fetch. Must be between 0 and
     * {@link #getMaxBattleHistorySize()}
     *
     * @return the record. null if none
     */
    @Nullable
    public DragonBattleRecord getPreviousDragonBattle(int index) {
        return index >= 0 && index < getPreviousDragonBattleCount() ? battleHistory.get(index) : null;
    }

    /**
     * Get the most recent dragon battle. Equivalent to calling {@code getPreviousDragonBattle(0)}.
     *
     * @return the most recent dragon battle. null if none
     */
    @Nullable
    public DragonBattleRecord getPreviousDragonBattle() {
        return battleHistory.peek();
    }

    /**
     * Get a {@link List} of previous dragon battles where index 0 is the most recent dragon
     * battle and {@link Collection#size()} is the oldest.
     *
     * @return all previous dragon battles
     */
    @NotNull
    public List<@NotNull DragonBattleRecord> getPreviousDragonBattles() {
        return Collections.unmodifiableList(battleHistory);
    }

    /**
     * Get the amount of battles recorded in the battle history.
     *
     * @return the amount of battle records
     */
    public int getPreviousDragonBattleCount() {
        return battleHistory.size();
    }

    /**
     * Get the maximum amount of battles recordable by this world.
     *
     * @return the max battle history size
     */
    public int getMaxBattleHistorySize() {
        return maxBattleHistorySize;
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

        FileConfiguration config = DragonEggDrop.getInstance().getConfig();
        return WRAPPERS.computeIfAbsent(world.getUID(), uuid -> new EndWorldWrapper(world, config.getInt(DEDConstants.CONFIG_WORLD_HISTORY_SIZE, 5)));
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
        WRAPPERS.values().forEach(world -> world.dragonCheckRunnable.cancel());
        WRAPPERS.clear();
    }

}
