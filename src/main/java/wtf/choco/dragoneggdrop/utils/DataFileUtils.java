package wtf.choco.dragoneggdrop.utils;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.boss.DragonBattle;
import org.bukkit.entity.EnderDragon;
import org.jetbrains.annotations.NotNull;

import wtf.choco.dragoneggdrop.DragonEggDrop;
import wtf.choco.dragoneggdrop.dragon.DragonTemplate;
import wtf.choco.dragoneggdrop.dragon.loot.DragonLootTable;
import wtf.choco.dragoneggdrop.particle.ParticleShapeDefinition;
import wtf.choco.dragoneggdrop.registry.Registry;
import wtf.choco.dragoneggdrop.world.DragonBattleRecord;
import wtf.choco.dragoneggdrop.world.DragonRespawnData;
import wtf.choco.dragoneggdrop.world.EndWorldWrapper;

/**
 * A utility class to ease access to data files provided by DragonEggDrop.
 *
 * @author Parker Hawke - Choco
 */
public final class DataFileUtils {

    private DataFileUtils() {}

    /**
     * Write temporary data from DragonEggDrop to the provided File.
     *
     * @param file the file to which temporary data should be written
     *
     * @throws IOException if an io exception occurred
     */
    public static void writeTempData(@NotNull File file) throws IOException {
        Preconditions.checkArgument(file != null, "file must not be null");

        if (!file.createNewFile()) {
            return;
        }

        JsonObject object = new JsonObject();

        for (EndWorldWrapper world : EndWorldWrapper.getAll()) {
            JsonObject objectWorld = new JsonObject();

            DragonTemplate respawningTemplate = world.getRespawningTemplate();
            if (respawningTemplate != null) {
                objectWorld.addProperty("respawnTemplate", respawningTemplate.getId());
            }

            DragonTemplate activeTemplate = world.getActiveTemplate();
            if (activeTemplate != null) {
                objectWorld.addProperty("activeTemplate", activeTemplate.getId());
            }

            DragonLootTable lootTableOverride = world.getLootTableOverride();
            if (lootTableOverride != null) {
                objectWorld.addProperty("lootTableOverride", lootTableOverride.getId());
            }

            DragonRespawnData respawnData = world.getDragonRespawnData();
            if (world.isRespawnInProgress() && respawnData != null) {
                objectWorld.addProperty("respawnStartTime", respawnData.getStartTime());
                objectWorld.addProperty("respawnDuration", respawnData.getDuration());
            }

            List<@NotNull DragonBattleRecord> previousDragonBattles = world.getPreviousDragonBattles();
            if (previousDragonBattles.size() >= 1) {
                JsonArray historyArray = new JsonArray();
                previousDragonBattles.forEach(record -> historyArray.add(record.toJson()));

                objectWorld.add("history", historyArray);
            }

            object.add(world.getWorld().getName(), objectWorld);
        }

        try (PrintWriter writer = new PrintWriter(file)) {
            DragonEggDrop.GSON.toJson(object, new JsonWriter(writer));
        } catch (JsonIOException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Read temporary data to DragonEggDrop from the provided File.
     *
     * @param plugin the plugin instance
     * @param file the file from which to read temporary data
     */
    public static void readTempData(@NotNull DragonEggDrop plugin, @NotNull File file) {
        Preconditions.checkArgument(plugin != null, "plugin must not be null");
        Preconditions.checkArgument(file != null, "file must not be null");

        JsonObject root = null;
        try (FileReader reader = new FileReader(file)) {
            root = DragonEggDrop.GSON.fromJson(reader, JsonObject.class);
        } catch (IOException | JsonIOException e) {
            e.printStackTrace();
        }

        if (root == null) {
            return;
        }

        Registry<@NotNull DragonTemplate> dragonTemplateRegistry = plugin.getDragonTemplateRegistry();
        Registry<@NotNull DragonLootTable> lootTableRegistry = plugin.getLootTableRegistry();

        for (Entry<String, JsonElement> entry : root.entrySet()) {
            World world = Bukkit.getWorld(entry.getKey());
            if (world == null) {
                plugin.getLogger().warning("Could not load temp data for world " + entry.getKey() + " (does it exist?). Deleting temporary data");
                return;
            }

            if (plugin.getConfig().getStringList(DEDConstants.CONFIG_DISABLED_WORLDS).contains(world.getName())) {
                return;
            }

            EndWorldWrapper worldWrapper = EndWorldWrapper.of(world);
            JsonObject worldObject = entry.getValue().getAsJsonObject();

            if (worldObject.has("respawnTemplate")) {
                DragonTemplate template = dragonTemplateRegistry.get(worldObject.get("respawnTemplate").getAsString());
                if (template != null) {
                    worldWrapper.setRespawningTemplate(template);
                }
            }

            Collection<@NotNull EnderDragon> dragons = world.getEntitiesByClass(EnderDragon.class);
            if (worldObject.has("activeTemplate") && !dragons.isEmpty()) {
                DragonTemplate template = dragonTemplateRegistry.get(worldObject.get("activeTemplate").getAsString());
                DragonBattle battle = world.getEnderDragonBattle();

                if (template != null && battle != null) {
                    worldWrapper.setActiveTemplate(template);
                    template.applyToBattle(Iterables.get(dragons, 0), battle);
                }
            }

            if (worldObject.has("history")) {
                JsonArray historyArray = worldObject.getAsJsonArray("history");
                historyArray.forEach(historyEntryElement -> {
                    if (!historyEntryElement.isJsonObject()) {
                        return;
                    }

                    JsonObject historyEntryObject = historyEntryElement.getAsJsonObject();
                    DragonBattleRecord dragonBattleRecord = DragonBattleRecord.fromJson(worldWrapper, historyEntryObject);

                    worldWrapper.recordDragonBattle(dragonBattleRecord);
                });
            }

            if (worldObject.has("lootTableOverride")) {
                DragonLootTable lootTable = lootTableRegistry.get(worldObject.get("lootTableOverride").getAsString());
                if (lootTable != null) {
                    worldWrapper.setLootTableOverride(lootTable);
                }
            }

            if (worldObject.has("respawnStartTime") && worldObject.has("respawnDuration")) {
                if (worldWrapper.isRespawnInProgress()) {
                    worldWrapper.stopRespawn();
                }

                long startTime = worldObject.get("respawnStartTime").getAsLong();
                long duration = worldObject.get("respawnDuration").getAsLong();

                worldWrapper.startRespawn(new DragonRespawnData(worldWrapper, startTime, duration));
            }

            // LEGACY DATA
            else if (worldObject.has("respawnTime")) {
                if (worldWrapper.isRespawnInProgress()) {
                    worldWrapper.stopRespawn();
                }

                worldWrapper.startRespawn(worldObject.get("respawnTime").getAsInt());
            }
            // LEGACY DATA END
        }
    }

    /**
     * Reload all data that is stored in memory. This includes:
     * <ul>
     *   <li>Dragon templates
     *   <li>Dragon loot tables
     *   <li>Particle shape definitions
     * </ul>
     *
     * @param plugin the plugin instance
     * @param log whether or not to log to console about the reloading process
     */
    public static void reloadInMemoryData(@NotNull DragonEggDrop plugin, boolean log) {
        Preconditions.checkArgument(plugin != null, "plugin must not be null");

        Logger logger = plugin.getLogger();

        // Load loot tables
        if (log) {
            logger.info("Loading loot tables...");
        }
        Registry<@NotNull DragonLootTable> lootTableRegistry = plugin.getLootTableRegistry();
        lootTableRegistry.clear();
        DragonLootTable.loadLootTables(plugin).forEach(lootTableRegistry::register);
        if (log) {
            logger.info("Done! Successfully loaded " + lootTableRegistry.size() + " loot tables");
        }

        // Load shape definitions
        if (log) {
            logger.info("Loading particle shape definitions...");
        }
        Registry<@NotNull ParticleShapeDefinition> particleRegistry = plugin.getParticleShapeDefinitionRegistry();
        particleRegistry.clear();
        for (File file : plugin.getParticleDirectory().listFiles((file, name) -> name.endsWith(".json") && !name.equals("possible_conditions.json"))) {
            ParticleShapeDefinition shapeDefinition = ParticleShapeDefinition.fromFile(file);
            particleRegistry.register(shapeDefinition);
        }
        if (log) {
            logger.info("Done! Successfully loaded " + particleRegistry.size() + " shape definitions");
        }

        // Load dragon templates
        if (log) {
            logger.info("Loading dragon templates...");
        }
        Registry<@NotNull DragonTemplate> dragonTemplateRegistry = plugin.getDragonTemplateRegistry();
        dragonTemplateRegistry.clear();
        for (File file : plugin.getDragonTemplateDirectory().listFiles((file, name) -> name.endsWith(".yml"))) {
            DragonTemplate dragonTemplate = DragonTemplate.fromFile(file);
            dragonTemplateRegistry.register(dragonTemplate);
        }
        logger.info("Done! Successfully loaded " + dragonTemplateRegistry.size() + " dragon templates");
    }

}
