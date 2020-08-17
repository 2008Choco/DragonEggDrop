package wtf.choco.dragoneggdrop.dragon;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;

import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;

import wtf.choco.dragoneggdrop.dragon.loot.DragonLootTable;
import wtf.choco.dragoneggdrop.particle.ParticleShapeDefinition;

/**
 * A builder class for {@link DragonTemplate} instances.
 *
 * @author Parker Hawke - Choco
 *
 * @see DragonTemplate#builder(String)
 * @see DragonTemplate#buildCopy(String, DragonTemplate)
 */
public final class DragonTemplateBuilder {

    private ParticleShapeDefinition particleShapeDefinition;
    private DragonLootTable lootTable;
    private double spawnWeight;
    private String name;
    private BarStyle barStyle;
    private BarColor barColour;
    private List<String> spawnAnnouncement;
    private Map<Attribute, Double> attributes;

    private final String id;

    DragonTemplateBuilder(String id) {
        Preconditions.checkArgument(id != null && !id.isEmpty(), "id must not be empty or null");
        Preconditions.checkArgument(!id.contains(" "), "id must not have any spaces");

        this.id = id;
    }

    DragonTemplateBuilder(String id, DragonTemplate template) {
        this(id);

        this.particleShapeDefinition = template.getParticleShapeDefinition();
        this.lootTable = template.getLootTable();
        this.spawnWeight = template.getSpawnWeight();
        this.name = template.getName();
        this.barStyle = template.getBarStyle();
        this.barColour = template.getBarColor();
        this.spawnAnnouncement = template.getSpawnAnnouncement();
        this.attributes = template.getAttributes();
    }

    /**
     * Set the {@link ParticleShapeDefinition}.
     *
     * @param particleShapeDefinition the particle shape definition to set
     *
     * @return this instance. Allows for chained method calls
     */
    public DragonTemplateBuilder particleShapeDefinition(ParticleShapeDefinition particleShapeDefinition) {
        this.particleShapeDefinition = particleShapeDefinition;
        return this;
    }

    /**
     * Set the {@link DragonLootTable}.
     *
     * @param lootTable the loot table to set
     *
     * @return this instance. Allows for chained method calls
     */
    public DragonTemplateBuilder lootTable(DragonLootTable lootTable) {
        this.lootTable = lootTable;
        return this;
    }

    /**
     * Set the spawn weight.
     *
     * @param spawnWeight the spawn weight to set
     *
     * @return this instance. Allows for chained method calls
     */
    public DragonTemplateBuilder spawnWeight(double spawnWeight) {
        this.spawnWeight = spawnWeight;
        return this;
    }

    /**
     * Set the name.
     *
     * @param name the name to set
     *
     * @return this instance. Allows for chained method calls
     */
    public DragonTemplateBuilder name(String name) {
        this.name = name;
        return this;
    }

    /**
     * Set the boss bar style.
     *
     * @param barStyle the bar style to set
     *
     * @return this instance. Allows for chained method calls
     */
    public DragonTemplateBuilder barStyle(BarStyle barStyle) {
        this.barStyle = barStyle;
        return this;
    }

    /**
     * Set the boss bar colour.
     *
     * @param barColour the colour to set
     *
     * @return this instance. Allows for chained method calls
     */
    public DragonTemplateBuilder barColor(BarColor barColour) {
        this.barColour = barColour;
        return this;
    }

    /**
     * Set the spawn announcement.
     *
     * @param spawnAnnouncement the spawn announcement to set
     *
     * @return this instance. Allows for chained method calls
     */
    public DragonTemplateBuilder spawnAnnouncement(List<String> spawnAnnouncement) {
        this.spawnAnnouncement = spawnAnnouncement;
        return this;
    }

    /**
     * Add an attribute and apply to it the given base value.
     *
     * @param attribute the attribute to set
     * @param value the base value to set
     *
     * @return this instance. Allows for chained method calls
     */
    public DragonTemplateBuilder attribute(Attribute attribute, double value) {
        if (attributes == null) {
            this.attributes = new EnumMap<>(Attribute.class);
        }

        this.attributes.put(attribute, value);
        return this;
    }

    /**
     * Build the final {@link DragonTemplate} instance.
     *
     * @return the dragon template
     */
    public DragonTemplate build() {
        return new DragonTemplate(id, particleShapeDefinition, lootTable, spawnWeight, name, barStyle, barColour, spawnAnnouncement, attributes);
    }

}
