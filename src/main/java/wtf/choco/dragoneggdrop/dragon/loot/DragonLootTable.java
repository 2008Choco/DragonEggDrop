package wtf.choco.dragoneggdrop.dragon.loot;

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

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.boss.DragonBattle;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;

import wtf.choco.dragoneggdrop.DragonEggDrop;
import wtf.choco.dragoneggdrop.dragon.DragonTemplate;
import wtf.choco.dragoneggdrop.dragon.loot.elements.DragonLootElementCommand;
import wtf.choco.dragoneggdrop.dragon.loot.elements.DragonLootElementEgg;
import wtf.choco.dragoneggdrop.dragon.loot.elements.DragonLootElementItem;
import wtf.choco.dragoneggdrop.dragon.loot.elements.IDragonLootElement;
import wtf.choco.dragoneggdrop.dragon.loot.pool.ILootPool;
import wtf.choco.dragoneggdrop.dragon.loot.pool.LootPoolCommand;
import wtf.choco.dragoneggdrop.dragon.loot.pool.LootPoolItem;
import wtf.choco.dragoneggdrop.registry.Registerable;
import wtf.choco.dragoneggdrop.utils.math.MathUtils;

/**
 * Represents a dragon's loot table. These tables are used to randomly generate unique
 * loot for every dragon while being reusable for multiple dragons.
 *
 * @author Parker Hawke - Choco
 */
public class DragonLootTable implements Registerable {

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
     * @see #fromFile(File)
     */
    public DragonLootTable(String id, DragonLootElementEgg egg, List<ILootPool<DragonLootElementCommand>> commandPools, List<ILootPool<DragonLootElementItem>> chestPools) {
        this.id = id;
        this.egg = (egg != null) ? egg : new DragonLootElementEgg(0.0);
        this.commandPools = (commandPools != null) ? new ArrayList<>(commandPools) : Collections.EMPTY_LIST;
        this.chestPools = (chestPools != null) ? new ArrayList<>(chestPools) : Collections.EMPTY_LIST;
    }

    @Override
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
     * @param template the template for which to generate loot
     * @param killer the player that has slain the dragon. May be null
     */
    public void generate(DragonBattle battle, DragonTemplate template, Player killer) {
        Preconditions.checkArgument(battle != null, "Attempted to generate loot for null dragon battle");
        Preconditions.checkArgument(template != null, "Attempted to generate loot for null dragon template");

        Chest chest = null;
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
        this.egg.generate(battle, template, killer, random, chest);

        // Generate the item loot pools
        this.generateLootPools(chestPools, plugin, battle, template, killer, random, chest);

        // Execute the command loot pools
        this.generateLootPools(commandPools, plugin, battle, template, killer, random, chest);
    }

    /**
     * Generate item loot for this loot table and place it in a chest to be set at the
     * given Block position.
     *
     * @param block the block at which to set the chest
     * @param template the template for which to generate loot.
     * @param player the player for whom to generate the loot. May be null
     */
    public void generate(Block block, DragonTemplate template, Player player) {
        Preconditions.checkArgument(template != null, "Attempted to generate loot for null dragon template");

        block.setType(Material.CHEST);

        Chest chest = (Chest) block.getState();
        this.egg.generate(null, template, player, ThreadLocalRandom.current(), chest);
        this.generateLootPools(chestPools, DragonEggDrop.getInstance(), null, template, player, ThreadLocalRandom.current(), chest);
    }

    /**
     * Write this loot table as a JsonObject.
     *
     * @return the JSON representation
     */
    public JsonObject asJson() {
        return new JsonObject();
    }

    private <T extends IDragonLootElement> void generateLootPools(List<ILootPool<T>> pools, DragonEggDrop plugin, DragonBattle battle, DragonTemplate template, Player killer, ThreadLocalRandom random, Chest chest) {
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

                loot.generate(battle, template, killer, random, chest);
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
    public static DragonLootTable fromFile(File file) throws JsonParseException {
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
     * @param plugin the plugin instance
     *
     * @return all parsed DragonLootTable objects
     */
    public static List<DragonLootTable> loadLootTables(DragonEggDrop plugin) {
        Logger logger = plugin.getLogger();
        List<DragonLootTable> lootTables = new ArrayList<>();

        // Return empty list if the folder was just created
        if (plugin.getLootTableDirectory().mkdir()) {
            return lootTables;
        }

        boolean suggestLinter = false;

        for (File file : plugin.getLootTableDirectory().listFiles((file, name) -> name.endsWith(".json"))) {
            if (file.getName().contains(" ")) {
                logger.warning("Dragon loot table files must not contain spaces (File=\"" + file.getName() + "\")! Ignoring...");
                continue;
            }

            try {
                DragonLootTable lootTable = DragonLootTable.fromFile(file);

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
