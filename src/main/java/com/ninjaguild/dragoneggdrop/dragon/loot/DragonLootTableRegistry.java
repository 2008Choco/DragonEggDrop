package com.ninjaguild.dragoneggdrop.dragon.loot;

import java.util.HashMap;
import java.util.Map;

import com.ninjaguild.dragoneggdrop.dragon.DragonTemplate;

public class DragonLootTableRegistry {

    private final Map<String, DragonLootTable> tables = new HashMap<>();

    public void register(String id, DragonLootTable table) {
        this.tables.put(id, table);
    }

    public void unregister(String id) {
        this.tables.remove(id);
    }

    public DragonLootTable getLootTable(String id) {
        return tables.get(id);
    }

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
        DragonTemplate.loadLootTables().forEach(t -> tables.put(t.getId(), t));
    }

}
