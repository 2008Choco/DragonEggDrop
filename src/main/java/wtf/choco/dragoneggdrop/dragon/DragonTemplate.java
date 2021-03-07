package wtf.choco.dragoneggdrop.dragon;

import com.google.common.base.Enums;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.boss.DragonBattle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EnderDragon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import wtf.choco.dragoneggdrop.DragonEggDrop;
import wtf.choco.dragoneggdrop.dragon.loot.DragonLootTable;
import wtf.choco.dragoneggdrop.particle.ParticleShapeDefinition;
import wtf.choco.dragoneggdrop.registry.Registerable;
import wtf.choco.dragoneggdrop.utils.DEDConstants;

/**
 * Represents an immutable template for a custom dragon to be spawned containing information
 * about its name, the style of its boss bar, as well as the loot table used to generate its
 * loot after it has been killed.
 *
 * @author Parker Hawke - Choco
 */
public class DragonTemplate implements Registerable {

    private final String id;
    private final ParticleShapeDefinition particleShapeDefinition;
    private final DragonLootTable lootTable;

    private final String name;
    private final BarStyle barStyle;
    private final BarColor barColour;

    private final double spawnWeight;
    private final List<@NotNull String> spawnAnnouncement;

    private final Map<@NotNull Attribute, @NotNull Double> attributes;

    /**
     * Construct a new DragonTemplate object.
     *
     * @param id the unique id for this template
     * @param particleShapeDefinition the dragon's particle shape definition. Can be null
     * @param lootTable the dragon's loot table. Can be null
     * @param spawnWeight this template's spawn weight. Must be >= 0.0
     * @param name the name of the Ender Dragon to use when it is spawned. Can be null
     * @param barStyle the boss bar style. Can be null
     * @param barColour the boss bar colour. Can be null
     * @param spawnAnnouncement the spawn announcement list. Can be null
     * @param attributes the dragon's attribute modifier values. Can be null
     *
     * @see DragonTemplateBuilder
     */
    DragonTemplate(@NotNull String id, @Nullable ParticleShapeDefinition particleShapeDefinition, @Nullable DragonLootTable lootTable, double spawnWeight, @Nullable String name, @Nullable BarStyle barStyle, @Nullable BarColor barColour, @Nullable List<@NotNull String> spawnAnnouncement, @Nullable Map<@NotNull Attribute, @NotNull Double> attributes) {
        Preconditions.checkArgument(id != null && !id.isEmpty(), "identifier must not be empty or null");
        Preconditions.checkArgument(!id.contains(" "), "Template identifiers must not have any spaces");
        Preconditions.checkArgument(spawnWeight >= 0.0, "spawnWeight must be >= 0");

        this.id = id;
        this.particleShapeDefinition = particleShapeDefinition;
        this.lootTable = lootTable;

        this.name = name;
        this.barStyle = (barStyle != null) ? barStyle : BarStyle.SOLID;
        this.barColour = (barColour != null) ? barColour : BarColor.PINK;

        this.spawnWeight = spawnWeight;
        this.spawnAnnouncement = (spawnAnnouncement != null) ? new ArrayList<>(spawnAnnouncement) : Collections.emptyList();
        this.attributes = (attributes != null) ? new EnumMap<>(attributes) : Collections.emptyMap();
    }

    @NotNull
    @Override
    public String getId() {
        return id;
    }

    /**
     * Get the name of the dragon.
     *
     * @return the dragon's name. null if none
     */
    @Nullable
    public String getName() {
        return name;
    }

    /**
     * Get the style of the boss bar.
     *
     * @return the boss bar style
     */
    @NotNull
    public BarStyle getBarStyle() {
        return barStyle;
    }

    /**
     * Get the colour of the boss bar.
     *
     * @return the boss bar colour
     */
    @NotNull
    public BarColor getBarColor() {
        return barColour;
    }

    /**
     * Get the particle shape definition to use when this dragon is killed.
     *
     * @return the shape definition
     */
    @Nullable
    public ParticleShapeDefinition getParticleShapeDefinition() {
        return particleShapeDefinition;
    }

    /**
     * Get the loot table used to generate the loot for this dragon once killed.
     *
     * @return the dragon loot table
     */
    @Nullable
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
    @NotNull
    public List<@NotNull String> getSpawnAnnouncement() {
        return spawnAnnouncement;
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
     * Get an immutable Map of all attributes and their values according to this template.
     *
     * @return the mapped attributes and values
     */
    @NotNull
    public Map<@NotNull Attribute, @NotNull Double> getAttributes() {
        return ImmutableMap.copyOf(attributes);
    }

    /**
     * Get the value to be applied for a specific attribute. If the provided attribute
     * value is not specified, -1 will be returned.
     *
     * @param attribute the attribute to check
     *
     * @return the value of the attribute, or -1 if non existent
     */
    public double getAttribute(@NotNull Attribute attribute) {
        return attributes.getOrDefault(attribute, -1.0);
    }

    /**
     * Check whether this template has specified an attribute's value.
     *
     * @param attribute the attribute to check
     *
     * @return true if defined. false otherwise
     */
    public boolean hasAttribute(@NotNull Attribute attribute) {
        return attributes.containsKey(attribute);
    }

    /**
     * Apply this templates data to an EnderDragonBattle object.
     *
     * @param dragon the dragon to modify
     * @param battle the battle to modify
     */
    public void applyToBattle(@NotNull EnderDragon dragon, @NotNull DragonBattle battle) {
        Preconditions.checkArgument(dragon != null, "dragon must not be null");
        Preconditions.checkArgument(battle != null, "battle must not be null");

        BossBar bossBar = battle.getBossBar();
        if (name != null) {
            dragon.setCustomName(name);
            bossBar.setTitle(name);
        }

        bossBar.setStyle(barStyle);
        bossBar.setColor(barColour);
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
     * Get a new {@link DragonTemplateBuilder} to construct an instance of {@link DragonTemplate}.
     *
     * @param id the template's unique id
     *
     * @return the template builder
     */
    @NotNull
    public static DragonTemplateBuilder builder(@NotNull String id) {
        return new DragonTemplateBuilder(id);
    }

    /**
     * Get a new {@link DragonTemplateBuilder} to construct an instance of {@link DragonTemplate}
     * with values copied from an existing template.
     *
     * @param id the template's unique id
     * @param template the template from which to copy values
     *
     * @return the template builder
     */
    @NotNull
    public static DragonTemplateBuilder buildCopy(@NotNull String id, @NotNull DragonTemplate template) {
        return new DragonTemplateBuilder(id, template);
    }

    /**
     * Load and create a {@link DragonTemplate} from a YAML {@link File}.
     *
     * @param file the file from which to parse a dragon template
     *
     * @return the dragon template
     */
    @NotNull
    public static DragonTemplate fromFile(@NotNull File file) {
        Preconditions.checkArgument(file != null, "file must not be null");

        String fileName = file.getName();
        if (!fileName.endsWith(".yml")) {
            throw new IllegalArgumentException("Expected .yml file. Got " + fileName.substring(fileName.lastIndexOf('.')) + " instead");
        }

        DragonEggDrop plugin = DragonEggDrop.getInstance();
        String id = fileName.substring(0, fileName.lastIndexOf('.')).replace(' ', '_');
        if (plugin.getDragonTemplateRegistry().isRegistered(id)) {
            throw new IllegalStateException("Template with id \"" + id + "\" has already been loaded and registered");
        }

        FileConfiguration templateFile = YamlConfiguration.loadConfiguration(file);
        DragonTemplateBuilder templateBuilder = new DragonTemplateBuilder(id);

        String particleShapeId = templateFile.getString(DEDConstants.TEMPLATE_PARTICLES);
        ParticleShapeDefinition particleShapeDefinition = plugin.getParticleShapeDefinitionRegistry().get(particleShapeId);
        if (particleShapeId == null) {
            plugin.getLogger().warning("Template with id \"" + id + "\" does not have a particles option defined. No particles will be played on death");
        }

        if (particleShapeDefinition == null) {
            plugin.getLogger().warning("Template with id \"" + id + "\" has declared an unknown particle shape, " + particleShapeId + ". Using default, open_ended_helix.");
            particleShapeDefinition = plugin.getParticleShapeDefinitionRegistry().get("open_ended_helix"); // If it's null here, whatever. It doesn't matter
        }

        templateBuilder.particleShapeDefinition(particleShapeDefinition);
        templateBuilder.lootTable(plugin.getLootTableRegistry().get(templateFile.getString(DEDConstants.TEMPLATE_LOOT)));

        // Loading less-necessary information from the template file
        String dragonName = templateFile.getString(DEDConstants.TEMPLATE_DRAGON_NAME);
        if (dragonName != null) {
            dragonName = ChatColor.translateAlternateColorCodes('&', dragonName);
            templateBuilder.name(dragonName);
        }

        String barStyleString = templateFile.getString(DEDConstants.TEMPLATE_BAR_COLOR, "SOLID");
        if (barStyleString != null) {
            templateBuilder.barStyle(Enums.getIfPresent(BarStyle.class, barStyleString.toUpperCase()).or(BarStyle.SOLID));
        }

        String barColorString = templateFile.getString(DEDConstants.TEMPLATE_BAR_STYLE, "PINK");
        if (barColorString != null) {
            templateBuilder.barColor(Enums.getIfPresent(BarColor.class, barColorString.toUpperCase()).or(BarColor.PINK));
        }

        templateBuilder.spawnWeight(templateFile.getDouble(DEDConstants.TEMPLATE_SPAWN_WEIGHT, 1));

        if (templateFile.isList(DEDConstants.TEMPLATE_SPAWN_ANNOUNCEMENT)) {
            List<@NotNull String> spawnAnnouncement = templateFile.getStringList(DEDConstants.TEMPLATE_SPAWN_ANNOUNCEMENT);

            if (dragonName != null) {
                for (int i = 0; i < spawnAnnouncement.size(); i++) {
                    spawnAnnouncement.set(i, spawnAnnouncement.get(i).replace("%dragon%", dragonName));
                }
            }

            for (int i = 0; i < spawnAnnouncement.size(); i++) {
                spawnAnnouncement.set(i, ChatColor.translateAlternateColorCodes('&', spawnAnnouncement.get(i)));
            }

            templateBuilder.spawnAnnouncement(spawnAnnouncement);
        }
        else if (templateFile.isString(DEDConstants.TEMPLATE_SPAWN_ANNOUNCEMENT)) {
            List<@NotNull String> spawnAnnouncement = null;
            String spawnAnnouncementString = templateFile.getString(DEDConstants.TEMPLATE_SPAWN_ANNOUNCEMENT);

            if (spawnAnnouncementString != null) {
                if (dragonName != null) {
                    spawnAnnouncementString = spawnAnnouncementString.replace("%dragon%", dragonName);
                }

                spawnAnnouncement = Arrays.asList(ChatColor.translateAlternateColorCodes('&', spawnAnnouncementString));
            }

            templateBuilder.spawnAnnouncement(spawnAnnouncement);
        }

        // Attribute modifier loading
        ConfigurationSection attributeSection = templateFile.getConfigurationSection(DEDConstants.TEMPLATE_ATTRIBUTES);
        if (attributeSection != null) {
            for (String attributeKey : attributeSection.getValues(false).keySet()) {
                Optional<@NotNull Attribute> attribute = Enums.getIfPresent(Attribute.class, attributeKey.toUpperCase());
                if (!attribute.isPresent()) {
                    plugin.getLogger().warning("Unknown attribute \"" + attributeKey + "\" for template \"" + file.getName() + "\". Ignoring...");
                    continue;
                }

                double value = templateFile.getDouble(DEDConstants.TEMPLATE_ATTRIBUTES + "." + attributeKey, -1);
                if (value == -1) {
                    plugin.getLogger().warning("Invalid double value specified at attribute \"" + attributeKey + "\" for template \"" + file.getName() + "\". Ignoring...");
                    continue;
                }

                templateBuilder.attribute(attribute.get(), value);
            }
        }

        return templateBuilder.build();
    }

}
