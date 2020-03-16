package com.ninjaguild.dragoneggdrop.utils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Map.Entry;

import com.google.common.collect.Iterables;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.ninjaguild.dragoneggdrop.DragonEggDrop;
import com.ninjaguild.dragoneggdrop.dragon.DragonTemplate;
import com.ninjaguild.dragoneggdrop.dragon.loot.DragonLootTable;
import com.ninjaguild.dragoneggdrop.dragon.loot.DragonLootTableRegistry;
import com.ninjaguild.dragoneggdrop.nms.NMSUtils;
import com.ninjaguild.dragoneggdrop.world.EndWorldWrapper;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.EnderDragon;

public final class TempDataUtils {

    private TempDataUtils() {}

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

        DragonLootTableRegistry lootTableRegistry = plugin.getLootTableRegistry();

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
                DragonTemplate template = DragonTemplate.getById(element.get("respawnTemplate").getAsString());
                if (template != null) {
                    worldWrapper.setRespawningTemplate(template);
                }
            }

            Collection<EnderDragon> dragons = world.getEntitiesByClass(EnderDragon.class);
            if (element.has("activeTemplate") && !dragons.isEmpty()) {
                DragonTemplate template = DragonTemplate.getById(element.get("activeTemplate").getAsString());
                if (template != null) {
                    worldWrapper.setActiveTemplate(template);
                    template.applyToBattle(Iterables.get(dragons, 0), NMSUtils.getEnderDragonBattleFromWorld(world));
                }
            }

            if (element.has("lootTableOverride")) {
                DragonLootTable lootTable = lootTableRegistry.getLootTable(element.get("lootTableOverride").getAsString());
                if (lootTable != null) {
                    worldWrapper.setLootTableOverride(lootTable);
                }
            }
        }
    }

}
