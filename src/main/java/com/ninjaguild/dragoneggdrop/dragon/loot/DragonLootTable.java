package com.ninjaguild.dragoneggdrop.dragon.loot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.ninjaguild.dragoneggdrop.DragonEggDrop;
import com.ninjaguild.dragoneggdrop.dragon.loot.elements.DragonLootElementCommand;
import com.ninjaguild.dragoneggdrop.dragon.loot.elements.DragonLootElementEgg;
import com.ninjaguild.dragoneggdrop.dragon.loot.elements.DragonLootElementItem;
import com.ninjaguild.dragoneggdrop.dragon.loot.elements.IDragonLootElement;
import com.ninjaguild.dragoneggdrop.dragon.loot.pool.ILootPool;
import com.ninjaguild.dragoneggdrop.dragon.loot.pool.LootPoolCommand;
import com.ninjaguild.dragoneggdrop.dragon.loot.pool.LootPoolItem;
import com.ninjaguild.dragoneggdrop.utils.math.MathUtils;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.boss.DragonBattle;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.projectiles.ProjectileSource;

/**
 * Represents a dragon's loot table. These tables are used to randomly generate unique
 * loot for every dragon while being reusable for multiple dragons.
 *
 * @author Parker Hawke - Choco
 */
public class DragonLootTable {

    public static final File LOOT_TABLES_FOLDER = new File(DragonEggDrop.getInstance().getDataFolder(), "loot_tables/");

    private double chestChance;
    private String chestName;
    private DragonLootElementEgg egg;

    private final String id;
    private final List<ILootPool<DragonLootElementCommand>> commandPools;
    private final List<ILootPool<DragonLootElementItem>> chestPools;

    /**
     * Create a {@link DragonLootTable}.
     *
     * @param id the loot table's unique id
     * @param egg the egg element. If null, no egg will be generated
     * @param commandPools the command loot pools
     * @param chestPools the chest loot pools
     *
     * @see ILootPool
     * @see #fromJsonFile(File)
     */
    public DragonLootTable(String id, DragonLootElementEgg egg, List<ILootPool<DragonLootElementCommand>> commandPools, List<ILootPool<DragonLootElementItem>> chestPools) {
        this.id = id;
        this.egg = (egg != null) ? egg : new DragonLootElementEgg(0.0);
        this.commandPools = (commandPools != null) ? new ArrayList<>(commandPools) : Collections.EMPTY_LIST;
        this.chestPools = (chestPools != null) ? new ArrayList<>(chestPools) : Collections.EMPTY_LIST;
    }

    /**
     * Get this loot table's unique id.
     *
     * @return the loot table id
     */
    public String getId() {
        return id;
    }

    /**
     * Get the chance (0.0 - 100.0) that a chest will be generated and item loot pools
     * will be rolled.
     *
     * @return the chest chance
     */
    public double getChestChance() {
        return chestChance;
    }

    /**
     * Get the custom name of the generated chest.
     *
     * @return the chest's name
     */
    public String getChestName() {
        return chestName;
    }

    /**
     * Get the egg loot element.
     *
     * @return the egg element
     */
    public DragonLootElementEgg getEgg() {
        return egg;
    }

    /**
     * Get an immutable list of the command loot pools.
     *
     * @return an immutable command pool list
     */
    public List<ILootPool<DragonLootElementCommand>> getCommandPools() {
        return ImmutableList.copyOf(commandPools);
    }

    /**
     * Get an immutable list of the chest loot pools.
     *
     * @return an immutable chest pool list
     */
    public List<ILootPool<DragonLootElementItem>> getChestPools() {
        return ImmutableList.copyOf(chestPools);
    }

    /**
     * Generate loot for the given {@link DragonBattle} and {@link EnderDragon}. All loot
     * pools will be rolled and generated. The egg will be generated first (and put in a
     * chest if necessary), then the chest item pools, followed by the command pools.
     *
     * @param battle the battle for which to generate loot
     * @param dragon the dragon for which to generate loot
     */
    public void generate(DragonBattle battle, EnderDragon dragon) {
        Preconditions.checkArgument(battle != null, "Attempted to generate loot for null dragon battle");
        Preconditions.checkArgument(dragon != null, "Attempted to generate loot for null ender dragon");

        Chest chest = null;
        Player killer = findDragonKiller(dragon);
        Location endPortalLocation = battle.getEndPortalLocation().add(0, 4, 0);

        ThreadLocalRandom random = ThreadLocalRandom.current();
        DragonEggDrop plugin = DragonEggDrop.getInstance();

        Block block = endPortalLocation.getBlock();
        block.breakNaturally(); // If there's a block already present, break it

        if (random.nextDouble(100) < chestChance) {
            block.setType(Material.CHEST);

            chest = (Chest) block.getState();
            if (chestName != null && !chestName.isEmpty()) {
                chest.setCustomName(chestName);
                chest.update();
            }
        }

        // Generate the egg
        this.egg.generate(battle, dragon, killer, random, chest);

        // Generate the item loot pools
        this.generateLootPools(chestPools, plugin, battle, dragon, killer, random, chest);

        // Execute the command loot pools
        this.generateLootPools(commandPools, plugin, battle, dragon, killer, random, chest);
    }

    /**
     * Write this loot table as a JsonObject.
     *
     * @return the JSON representation
     */
    public JsonObject asJson() {
        return new JsonObject();
    }

    private Player findDragonKiller(EnderDragon dragon) {
        EntityDamageEvent lastDamageCause = dragon.getLastDamageCause();
        if (!(lastDamageCause instanceof EntityDamageByEntityEvent)) {
            return null;
        }

        Entity damager = ((EntityDamageByEntityEvent) lastDamageCause).getDamager();
        if (damager instanceof Player) {
            return (Player) damager;
        }

        else if (damager instanceof Projectile) {
            ProjectileSource projectileSource = ((Projectile) damager).getShooter();
            if (!(projectileSource instanceof Player)) {
                return null; // Give up
            }

            return (Player) projectileSource;
        }

        return null;
    }

    private <T extends IDragonLootElement> void generateLootPools(List<ILootPool<T>> pools, DragonEggDrop plugin, DragonBattle battle, EnderDragon dragon, Player killer, ThreadLocalRandom random, Chest chest) {
        if (pools == null || pools.isEmpty()) {
            return;
        }

        for (ILootPool<T> lootPool : pools) {
            if (random.nextDouble(100) >= lootPool.getChance()) {
                continue;
            }

            int rolls = random.nextInt(lootPool.getMinRolls(), lootPool.getMaxRolls() + 1);
            for (int i = 0; i < rolls; i++) {
                IDragonLootElement loot = lootPool.roll(random);
                if (loot == null) {
                    plugin.getLogger().warning("Attempted to generate null loot element for loot pool with name \"" + lootPool.getName() + "\" (loot table: \"" + id + "\"). Ignoring...");
                    continue;
                }

                loot.generate(battle, dragon, killer, random, chest);
            }
        }
    }

    /**
     * Parse a {@link DragonLootTable} instance from a JSON file. The file extension from
     * the specified file is validated. If the file is not terminated by .json, an
     * {@link IllegalArgumentException} will be thrown.
     *
     * @param file the file from which to parse the loot table
     *
     * @return the parsed loot table
     *
     * @throws IllegalArgumentException if the file is invalid
     * @throws JsonParseException if the parsing at all fails
     */
    public static DragonLootTable fromJsonFile(File file) throws JsonParseException {
        String fileName = file.getName();
        if (!fileName.endsWith(".json")) {
            throw new IllegalArgumentException("Expected .json file. Got " + fileName.substring(fileName.lastIndexOf('.')) + " instead");
        }

        JsonObject root = null;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            root = DragonEggDrop.GSON.fromJson(reader, JsonObject.class);
        } catch (IOException e) {
            throw new JsonParseException(e.getMessage(), e.getCause());
        }

        if (root == null) {
            throw new JsonParseException("Invalid root element");
        }

        String id = fileName.substring(0, fileName.lastIndexOf('.'));
        String chestName = null;
        double chestChance = root.has("chest") ? 100.0 : 0.0;

        DragonLootElementEgg egg = (root.has("egg") && root.get("egg").isJsonObject()) ? DragonEggDrop.GSON.fromJson(root.getAsJsonObject("egg"), DragonLootElementEgg.class) : new DragonLootElementEgg();

        List<ILootPool<DragonLootElementCommand>> commandPools = new ArrayList<>();
        List<ILootPool<DragonLootElementItem>> chestPools = new ArrayList<>();

        if (root.has("command_pools") && root.get("command_pools").isJsonArray()) {
            JsonArray commandPoolsRoot = root.getAsJsonArray("command_pools");
            for (JsonElement element : commandPoolsRoot) {
                if (!element.isJsonObject()) {
                    throw new JsonParseException("Invalid command pool. Expected object, got " + element.getClass().getSimpleName());
                }

                commandPools.add(LootPoolCommand.fromJson(element.getAsJsonObject()));
            }
        }

        if (root.has("chest") && root.get("chest").isJsonObject()) {
            JsonObject chestRoot = root.getAsJsonObject("chest");

            if (chestRoot.has("chance")) {
                chestChance = MathUtils.clamp(chestRoot.get("chance").getAsDouble(), 0.0, 100.0);
            }

            if (chestRoot.has("name")) {
                chestName = ChatColor.translateAlternateColorCodes('&', chestRoot.get("name").getAsString());
            }

            if (chestRoot.has("pools") && chestRoot.get("pools").isJsonArray()) {
                JsonArray chestPoolsRoot = chestRoot.getAsJsonArray("pools");
                for (JsonElement element : chestPoolsRoot) {
                    if (!element.isJsonObject()) {
                        throw new JsonParseException("Invalid item pool. Expected object, got " + element.getClass().getSimpleName());
                    }

                    chestPools.add(LootPoolItem.fromJson(element.getAsJsonObject()));
                }
            }
        }

        DragonLootTable lootTable = new DragonLootTable(id, egg, commandPools, chestPools);
        lootTable.chestName = chestName;
        lootTable.chestChance = chestChance;
        return lootTable;
    }

    /**
     * Load and parse all DragonLootTable objects from the dragons folder.
     *
     * @return all parsed DragonLootTable objects
     */
    public static List<DragonLootTable> loadLootTables() {
        Logger logger = DragonEggDrop.getInstance().getLogger();
        List<DragonLootTable> lootTables = new ArrayList<>();

        // Return empty list if the folder was just created
        if (LOOT_TABLES_FOLDER.mkdir()) {
            return lootTables;
        }

        boolean suggestLinter = false;

        for (File file : LOOT_TABLES_FOLDER.listFiles((file, name) -> name.endsWith(".json"))) {
            if (file.getName().contains(" ")) {
                logger.warning("Dragon loot table files must not contain spaces (File=\"" + file.getName() + "\")! Ignoring...");
                continue;
            }

            try {
                DragonLootTable lootTable = DragonLootTable.fromJsonFile(file);

                // Checking for existing templates
                if (lootTables.stream().anyMatch(t -> t.getId().matches(lootTable.getId()))) {
                    logger.warning("Duplicate dragon loot table with file name " + file.getName() + ". Ignoring...");
                    continue;
                }

                lootTables.add(lootTable);
            } catch (JsonParseException e) {
                logger.warning("Could not load loot table \"" + file.getName() + "\"");
                logger.warning(e.getMessage());
                suggestLinter = true;
            }
        }

        if (suggestLinter) {
            logger.warning("Ensure all values are correct and run the JSON through a validator such as https://jsonformatter.curiousconcept.com/");
        }

        return lootTables;
    }

}
