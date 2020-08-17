package wtf.choco.dragoneggdrop.utils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.logging.Logger;

import com.google.common.collect.Iterables;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.EnderDragon;

import wtf.choco.dragoneggdrop.DragonEggDrop;
import wtf.choco.dragoneggdrop.dragon.DragonTemplate;
import wtf.choco.dragoneggdrop.dragon.loot.DragonLootTable;
import wtf.choco.dragoneggdrop.particle.ParticleShapeDefinition;
import wtf.choco.dragoneggdrop.registry.Registry;
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
    public static void writeTempData(File file) throws IOException {
        if (!file.createNewFile()) {
            return;
        }

        JsonObject root = new JsonObject();

        for (EndWorldWrapper world : EndWorldWrapper.getAll()) {
            if (!world.isRespawnInProgress() && world.getActiveTemplate() == null) {
                return;
            }

            JsonObject jsonWorld = new JsonObject();
            if (world.isRespawnInProgress()) {
                jsonWorld.addProperty("respawnTime", world.getTimeUntilRespawn());
            }
            if (world.getRespawningTemplate() != null) {
                jsonWorld.addProperty("respawnTemplate", world.getRespawningTemplate().getId());
            }
            if (world.getActiveTemplate() != null) {
                jsonWorld.addProperty("activeTemplate", world.getActiveTemplate().getId());
            }
            if (world.hasLootTableOverride()) {
                jsonWorld.addProperty("lootTableOverride", world.getLootTableOverride().getId());
            }

            root.add(world.getWorld().getName(), jsonWorld);
        }

        try (PrintWriter writer = new PrintWriter(file)) {
            DragonEggDrop.GSON.toJson(root, new JsonWriter(writer));
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
    public static void readTempData(DragonEggDrop plugin, File file) {
        JsonObject root = null;
        try (FileReader reader = new FileReader(file)) {
            root = DragonEggDrop.GSON.fromJson(reader, JsonObject.class);
        } catch (IOException | JsonIOException e) {
            e.printStackTrace();
        }

        if (root == null) {
            return;
        }

        Registry<DragonTemplate> dragonTemplateRegistry = plugin.getDragonTemplateRegistry();
        Registry<DragonLootTable> lootTableRegistry = plugin.getLootTableRegistry();

        for (Entry<String, JsonElement> entry : root.entrySet()) {
            World world = Bukkit.getWorld(entry.getKey());
            if (world == null) {
                plugin.getLogger().warning("Could not load temp data for world " + entry.getKey() + " (does it exist?). Deleting temporary data");
                return;
            }

            EndWorldWrapper worldWrapper = EndWorldWrapper.of(world);
            JsonObject element = entry.getValue().getAsJsonObject();

            if (element.has("respawnTime")) {
                if (worldWrapper.isRespawnInProgress()) {
                    worldWrapper.stopRespawn();
                }

                worldWrapper.startRespawn(element.get("respawnTime").getAsInt());
            }

            if (element.has("respawnTemplate")) {
                DragonTemplate template = dragonTemplateRegistry.get(element.get("respawnTemplate").getAsString());
                if (template != null) {
                    worldWrapper.setRespawningTemplate(template);
                }
            }

            Collection<EnderDragon> dragons = world.getEntitiesByClass(EnderDragon.class);
            if (element.has("activeTemplate") && !dragons.isEmpty()) {
                DragonTemplate template = dragonTemplateRegistry.get(element.get("respawnTemplate").getAsString());
                if (template != null) {
                    worldWrapper.setActiveTemplate(template);
                    template.applyToBattle(Iterables.get(dragons, 0), world.getEnderDragonBattle());
                }
            }

            if (element.has("lootTableOverride")) {
                DragonLootTable lootTable = lootTableRegistry.get(element.get("lootTableOverride").getAsString());
                if (lootTable != null) {
                    worldWrapper.setLootTableOverride(lootTable);
                }
            }
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
    public static void reloadInMemoryData(DragonEggDrop plugin, boolean log) {
        Logger logger = plugin.getLogger();

        // Load loot tables
        if (log) {
            logger.info("Loading loot tables...");
        }
        Registry<DragonLootTable> lootTableRegistry = plugin.getLootTableRegistry();
        DragonLootTable.loadLootTables(plugin).forEach(lootTableRegistry::register);
        if (log) {
            logger.info("Done! Successfully loaded " + lootTableRegistry.size() + " loot tables");
        }

        // Load shape definitions
        if (log) {
            logger.info("Loading particle shape definitions...");
        }
        Registry<ParticleShapeDefinition> particleRegistry = plugin.getParticleShapeDefinitionRegistry();
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
        Registry<DragonTemplate> dragonTemplateRegistry = plugin.getDragonTemplateRegistry();
        for (File file : plugin.getDragonTemplateDirectory().listFiles((file, name) -> name.endsWith(".yml"))) {
            DragonTemplate dragonTemplate = DragonTemplate.fromFile(file);
            dragonTemplateRegistry.register(dragonTemplate);
        }
        logger.info("Done! Successfully loaded " + dragonTemplateRegistry.size() + " dragon templates");
    }

}
