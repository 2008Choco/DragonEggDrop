package com.ninjaguild.dragoneggdrop.dragon;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.base.Enums;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.ninjaguild.dragoneggdrop.DragonEggDrop;
import com.ninjaguild.dragoneggdrop.dragon.loot.DragonLootTable;
import com.ninjaguild.dragoneggdrop.nms.DragonBattle;

import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EnderDragon;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Represents a template for a custom dragon to be spawned containing information about
 * its name, the style of its boss bar, as well as the loot table used to generate its loot
 * after it has been killed.
 *
 * @author Parker Hawke - Choco
 */
public class DragonTemplate {

    public static final File DRAGONS_FOLDER = new File(DragonEggDrop.getInstance().getDataFolder(), "dragons/");

    protected final File file;
    protected final FileConfiguration configFile;

    private final DragonLootTable lootTable;
    private final String identifier;

    private String name;
    private BarStyle barStyle;
    private BarColor barColour;

    private double spawnWeight;
    private List<String> spawnAnnouncement;

    private final Map<Attribute, Double> attributes = new EnumMap<>(Attribute.class);

    /**
     * Construct a new DragonTemplate object.
     *
     * @param file the file holding this template data
     */
    public DragonTemplate(File file) {
        Preconditions.checkArgument(file != null, "File cannot be null. See DragonTemplate(String, String, BarStyle, BarColor) for null files");

        this.file = file;
        this.configFile = YamlConfiguration.loadConfiguration(file);
        this.identifier = file.getName().substring(0, file.getName().lastIndexOf('.')).replace(' ', '_');

        this.name = (configFile.contains("dragon-name") ? ChatColor.translateAlternateColorCodes('&', configFile.getString("dragon-name")) : null);
        this.barStyle = Enums.getIfPresent(BarStyle.class, configFile.getString("bar-style", "SOLID").toUpperCase()).or(BarStyle.SOLID);
        this.barColour = Enums.getIfPresent(BarColor.class, configFile.getString("bar-colour", "PINK").toUpperCase()).or(BarColor.PINK);
        this.lootTable = DragonEggDrop.getInstance().getLootTableRegistry().getLootTable(configFile.getString("loot"));
    }

    /**
     * Construct a new DragonTemplate object.
     *
     * @param identifier the name to identify this template
     * @param name the name of the dragon. Can be null
     * @param barStyle the style of the bar. Can be null
     * @param barColour the colour of the bar. Can be null
     * @param lootTable the dragon's loot table. Can be null
     */
    public DragonTemplate(String identifier, String name, BarStyle barStyle, BarColor barColour, DragonLootTable lootTable) {
        Validate.notEmpty(identifier, "Idenfitier must not be empty or null");
        Preconditions.checkArgument(identifier.contains(" "), "Template identifiers must not have any spaces");

        this.file = null;
        this.configFile = null;
        this.identifier = identifier;

        this.name = (name != null ? ChatColor.translateAlternateColorCodes('&', name) : null);
        this.barStyle = (barStyle != null ? barStyle : BarStyle.SOLID);
        this.barColour = (barColour != null ? barColour : BarColor.PINK);
        this.lootTable = lootTable;
    }

    /**
     * Get the string that identifies this template. If a file was passed
     * as a parameter in the creation of this template, the file's name
     * will be used as the identifier.
     *
     * @return the unique template identifier
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Get the file in the "dragons" folder that holds information for
     * this dragon template.
     *
     * @return the dragon template file
     */
    public File getFile() {
        return file;
    }

    /**
     * Get the name of the dragon.
     *
     * @return the dragon's name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the style of the boss bar.
     *
     * @return the boss bar style
     */
    public BarStyle getBarStyle() {
        return barStyle;
    }

    /**
     * Get the colour of the boss bar.
     *
     * @return the boss bar colour
     */
    public BarColor getBarColor() {
        return barColour;
    }

    /**
     * Get the loot table used to generate the loot for this dragon once killed.
     *
     * @return the dragon loot table
     */
    public DragonLootTable getLootTable() {
        return lootTable;
    }

    /**
     * Get the weight of this dragon's spawn percentage.
     *
     * @return the spawn weight
     */
    public double getSpawnWeight() {
        return spawnWeight;
    }

    /**
     * Get the messages to be announced to all players when this template spawns.
     *
     * @return the spawn announcement
     */
    public List<String> getSpawnAnnouncement() {
        return (spawnAnnouncement != null) ? new ArrayList<>(spawnAnnouncement) : null;
    }

    /**
     * Check whether this dragon's name should be announced as it spawns.
     *
     * @return true if it should be announced, false otherwise
     */
    public boolean shouldAnnounceSpawn() {
        return spawnAnnouncement != null && !spawnAnnouncement.isEmpty();
    }

    /**
     * Get an immutable Map of all attributes and their values according to
     * this template.
     *
     * @return the mapped attributes and values
     */
    public Map<Attribute, Double> getAttributes() {
        return ImmutableMap.copyOf(attributes);
    }

    /**
     * Get the value to be applied for a specific attribute. If the provided
     * attribute value is not specified, -1 will be returned.
     *
     * @param attribute the attribute to check
     *
     * @return the value of the attribute, or -1 if non existent
     */
    public double getAttribute(Attribute attribute) {
        return attributes.getOrDefault(attribute, -1.0);
    }

    /**
     * Check whether this template has specified an attribute's value.
     *
     * @param attribute the attribute to check
     *
     * @return true if defined. false otherwise
     */
    public boolean hasAttribute(Attribute attribute) {
        return attributes.containsKey(attribute);
    }

    /**
     * Apply this templates data to an EnderDragonBattle object.
     *
     * @param dragon the dragon to modify
     * @param battle the battle to modify
     */
    public void applyToBattle(EnderDragon dragon, DragonBattle battle) {
        Preconditions.checkArgument(dragon != null, "Ender Dragon cannot be null");
        Preconditions.checkArgument(battle != null, "Instance of DragonBattle cannot be null");

        if (name != null) {
            dragon.setCustomName(name);
            battle.setBossBarTitle(name);
        }

        battle.setBossBarStyle(barStyle, barColour);
        this.attributes.forEach((a, v) -> {
            AttributeInstance attribute = dragon.getAttribute(a);
            if (attribute != null) {
                attribute.setBaseValue(v);
            }
        });

        // Set health... max health attribute doesn't do that for me. -,-
        if (attributes.containsKey(Attribute.GENERIC_MAX_HEALTH)) {
            dragon.setHealth(attributes.get(Attribute.GENERIC_MAX_HEALTH));
        }
    }

    /**
     * Load and parse all DragonTemplate objects from the dragons folder.
     *
     * @return all parsed DragonTemplate objects
     */
    public static List<DragonTemplate> loadTemplates() {
        DragonEggDrop plugin = JavaPlugin.getPlugin(DragonEggDrop.class);
        List<DragonTemplate> templates = new ArrayList<>();

        // Return empty list if the folder was just created
        if (DRAGONS_FOLDER.mkdir()) {
            return templates;
        }

        for (File file : DRAGONS_FOLDER.listFiles((file, name) -> name.endsWith(".yml"))) {
            if (file.getName().contains(" ")) {
                plugin.getLogger().warning("Dragon template files must not contain spaces (File=\"" + file.getName() + "\")! Ignoring...");
                continue;
            }

            DragonTemplate template = new DragonTemplate(file);
            template.spawnWeight = template.configFile.getDouble("spawn-weight", 1);

            if (template.configFile.isList("spawn-announcement")) {
                template.spawnAnnouncement = template.configFile.getStringList("spawn-announcement").stream()
                        .map(s -> ChatColor.translateAlternateColorCodes('&', s.replace("%dragon%", template.getName())))
                        .collect(Collectors.toList());
            } else if (template.configFile.isString("spawn-announcement")) {
                template.spawnAnnouncement = Arrays.asList(ChatColor.translateAlternateColorCodes('&', template.configFile.getString("spawn-announcement").replace("%dragon%", template.getName())));
            }

            // Attribute modifier loading
            if (template.configFile.contains("attributes")) {
                for (String attributeKey : template.configFile.getConfigurationSection("attributes").getValues(false).keySet()) {
                    Attribute attribute = Enums.getIfPresent(Attribute.class, attributeKey.toUpperCase()).orNull();
                    if (attribute == null) {
                        plugin.getLogger().warning("Unknown attribute \"" + attributeKey + "\" for template \"" + file.getName() + "\". Ignoring...");
                        continue;
                    }

                    double value = template.configFile.getDouble("attributes." + attributeKey, -1);
                    if (value == -1) {
                        plugin.getLogger().warning("Invalid double value specified at attribute \"" + attributeKey + "\" for template \"" + file.getName() + "\". Ignoring...");
                        continue;
                    }

                    template.attributes.put(attribute, value);
                }
            }

            // Checking for existing templates
            if (templates.contains(template)) {
                JavaPlugin.getPlugin(DragonEggDrop.class).getLogger().warning("Duplicate dragon template with file name " + file.getName() + ". Ignoring");
                continue;
            }

            templates.add(template);
        }

        return templates;
    }

}
