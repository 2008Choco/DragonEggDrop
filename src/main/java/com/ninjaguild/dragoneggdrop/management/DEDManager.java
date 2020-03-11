package com.ninjaguild.dragoneggdrop.management;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.google.common.base.Preconditions;
import com.ninjaguild.dragoneggdrop.DragonEggDrop;
import com.ninjaguild.dragoneggdrop.dragon.DragonTemplate;
import com.ninjaguild.dragoneggdrop.utils.RandomCollection;

import org.bukkit.World;

/**
 * The central manager holding all information regarding world wrappers, loaded dragon
 * templates, and the currently active dragon battle.
 */
public class DEDManager {

    private final RandomCollection<DragonTemplate> dragonTemplates = new RandomCollection<>();
    private final Map<UUID, EndWorldWrapper> worldWrappers = new HashMap<>();
    private final DragonEggDrop plugin;

    public DEDManager(DragonEggDrop plugin) {
        this.plugin = plugin;
    }

    /**
     * Register a template to the DEDManager in order for it to be used when generating
     * dragons in the respawn process.
     *
     * @param template the template to register
     */
    public void registerTemplate(DragonTemplate template) {
        Preconditions.checkArgument(template != null, "Cannot register null templates");

        for (DragonTemplate registeredTemplate : dragonTemplates.values()) {
            if (registeredTemplate.getIdentifier().equals(template.getIdentifier())) {
                throw new UnsupportedOperationException("Cannot register two templates with the same identifier (" + template.getIdentifier() + ")");
            }
        }

        this.dragonTemplates.add(template.getSpawnWeight(), template);
    }

    /**
     * Get a collection of all loaded dragon templates.
     *
     * @return all dragon templates
     */
    public Collection<DragonTemplate> getDragonTemplates() {
        return dragonTemplates.toCollection();
    }

    /**
     * Get a weighted random dragon template pooled from all loaded templates.
     *
     * @return a random dragon template. null if none
     *
     * @see #getDragonTemplates()
     */
    public DragonTemplate getRandomTemplate() {
        return dragonTemplates.next();
    }

    /**
     * Get a template based on its identifier (see {@link DragonTemplate#getIdentifier()}).
     * This search is case-sensitive.
     *
     * @param template the template's unique identifier
     *
     * @return the resulting template, or null if none exists
     */
    public DragonTemplate getTemplate(String template) {
        return dragonTemplates.toCollection().stream()
                .filter(t -> t.getIdentifier().equals(template))
                .findFirst().orElse(null);
    }

    /**
     * Clear all loaded dragon templates.
     */
    public void clearTemplates() {
        this.dragonTemplates.clear();
    }

    /**
     * Load and parse all dragon template files from the "dragons" folder.
     * This method implicitly invokes {@link #clearTemplates()} before loading any
     * other templates.
     */
    public void reloadDragonTemplates() {
        this.dragonTemplates.clear();
        DragonTemplate.loadTemplates().forEach(t -> dragonTemplates.add(t.getSpawnWeight(), t));
    }

    /**
     * Get the world wrapper for the specified world.
     *
     * @param world the world to get
     * @return the world's respective wrapper
     */
    public EndWorldWrapper getWorldWrapper(World world) {
        Preconditions.checkArgument(world != null, "Cannot get wrapper for non-existent (null) world");
        return worldWrappers.computeIfAbsent(world.getUID(), w -> new EndWorldWrapper(plugin, world));
    }

    /**
     * Get the map containing all world wrappers.
     *
     * @return all world wrappers
     */
    public Collection<EndWorldWrapper> getWorldWrappers() {
        return worldWrappers.values();
    }

    /**
     * Clear all world wrapper data. This deletes all information to do with
     * active battles, as well as the state of a world according to DragonEggDrop.
     */
    public void clearWorldWrappers() {
        this.worldWrappers.clear();
    }


    /**
     * The type of trigger that allowed an Ender Dragon to  commence its respawning process.
     */
    public enum RespawnType {

        /**
         * A player joined the world
         */
        JOIN,

        /**
         * The ender dragon was killed
         */
        DEATH,

    }

}
