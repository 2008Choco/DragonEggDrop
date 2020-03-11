package com.ninjaguild.dragoneggdrop.dragon.loot;

import java.util.HashMap;
import java.util.Map;

import com.ninjaguild.dragoneggdrop.dragon.DragonTemplate;
import com.ninjaguild.dragoneggdrop.management.DEDManager;

/**
 * Represents a registry for all {@link DragonLootTable} instances. It is from here that
 * {@link DragonTemplate}s will pull their loot tables.
 *
 * @author Parker Hawke - Choco
 */
public class DragonLootTableRegistry {

    private final Map<String, DragonLootTable> tables = new HashMap<>();

    /**
     * Register a loot table.
     *
     * @param table the table to register
     */
    public void register(DragonLootTable table) {
        this.tables.put(table.getId(), table);
    }

    /**
     * Unregister a loot table with the given id.
     *
     * @param id the id to unregister
     */
    public void unregister(String id) {
        this.tables.remove(id);
    }

    /**
     * Unregister the specified loot table.
     *
     * @param lootTable the loot table to unregister
     */
    public void unregister(DragonLootTable lootTable) {
        if (lootTable == null) {
            return;
        }

        this.tables.remove(lootTable.getId());
    }

    /**
     * Get a loot table based on its (case-sensitive) id.
     *
     * @param id the loot table's id
     *
     * @return the loot table. null if none with the given id exists
     */
    public DragonLootTable getLootTable(String id) {
        return tables.get(id);
    }

    /**
     * Clear all loot tables from the registry. This will not remove loot tables from any
     * dragon templates that have already been loaded by the {@link DEDManager}.
     */
    public void clear() {
        this.tables.clear();
    }

    /**
     * Load and parse all dragon loot table files from the "loot_tables" folder.
     * This method implicitly invokes {@link #clear()} before loading any other
     * loot tables.
     */
    public void reloadDragonLootTables() {
        this.clear();
        DragonLootTable.loadLootTables().forEach(this::register);
    }

}
