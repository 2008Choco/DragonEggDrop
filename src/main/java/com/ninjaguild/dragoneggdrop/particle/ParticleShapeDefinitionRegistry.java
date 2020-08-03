package com.ninjaguild.dragoneggdrop.particle;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a registry for all {@link ParticleShapeDefinition} instances.
 *
 * @author Parker Hawke - Choco
 */
public class ParticleShapeDefinitionRegistry {

    private final Map<String, ParticleShapeDefinition> definitions = new HashMap<>();

    /**
     * Register a shape definition.
     *
     * @param definition the shape definition to register
     */
    public void register(ParticleShapeDefinition definition) {
        this.definitions.put(definition.getId(), definition);
    }

    /**
     * Unregister a shape definition with the given id.
     *
     * @param id the id to unregister
     */
    public void unregister(String id) {
        this.definitions.remove(id);
    }

    /**
     * Unregister the specified shape definition.
     *
     * @param definition the shape definition to unregister
     */
    public void unregister(ParticleShapeDefinition definition) {
        if (definition == null) {
            return;
        }

        this.definitions.remove(definition.getId());
    }

    /**
     * Get a shape definition based on its (case-sensitive) id.
     *
     * @param id the shape definition's id
     *
     * @return the shape definition. null if none with the given id exists
     */
    public ParticleShapeDefinition getParticleShapeDefinition(String id) {
        return definitions.get(id);
    }

    /**
     * Get a copy of all registered JsonParticleShapeDefinitions.
     *
     * @return all shape definitions
     */
    public List<ParticleShapeDefinition> getParticleShapeDefinitions() {
        return new ArrayList<>(definitions.values());
    }

    /**
     * Clear all shape definitions from the registry.
     */
    public void clear() {
        this.definitions.clear();
    }

    /**
     * Load and parse all particle shape definition files from the "loot_tables" folder.
     * This method implicitly invokes {@link #clear()} before loading any other shape definitions.
     */
    public void reload() {
        this.clear();

        for (File file : ParticleShapeDefinition.PARTICLES_FOLDER.listFiles((file, name) -> name.endsWith(".json") && !name.equals("possible_conditions.json"))) {
            this.register(new ParticleShapeDefinition(file));
        }
    }

}
