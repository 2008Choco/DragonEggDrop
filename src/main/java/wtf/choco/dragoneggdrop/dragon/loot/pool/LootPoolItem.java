package wtf.choco.dragoneggdrop.dragon.loot.pool;

import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import wtf.choco.commons.util.MathUtil;
import wtf.choco.dragoneggdrop.dragon.loot.elements.DragonLootElementItem;

/**
 * Represents a {@link ILootPool} implementation for {@link DragonLootElementItem}s.
 *
 * @author Parker Hawke - Choco
 */
public class LootPoolItem extends AbstractLootPool<DragonLootElementItem> {

    /**
     * Create an item loot pool.
     *
     * @param name this pool's name. Can be null
     * @param chance the chance that this loot pool will generate
     * @param minRolls the minimum amount of times this loot pool should be rolled
     * (inclusive).
     * @param maxRolls the maximum amount of times this loot pool should be rolled
     * (inclusive).
     * @param itemElements the elements in this loot pool
     */
    public LootPoolItem(@Nullable String name, double chance, int minRolls, int maxRolls, @NotNull Collection<@Nullable DragonLootElementItem> itemElements) {
        super(name, chance, minRolls, maxRolls, itemElements);
    }

    /**
     * Create an item loot pool.
     *
     * @param name this pool's name. Can be null
     * @param chance the chance that this loot pool will generate
     * @param rolls the amount of times this loot pool should be rolled
     * @param itemElements the elements in this loot pool
     */
    public LootPoolItem(@Nullable String name, double chance, int rolls, @NotNull Collection<@Nullable DragonLootElementItem> itemElements) {
        this(name, chance, rolls, rolls, itemElements);
    }

    /**
     * Create an item loot pool.
     *
     * @param name this pool's name. Can be null
     * @param minRolls the minimum amount of times this loot pool should be rolled
     * (inclusive).
     * @param maxRolls the maximum amount of times this loot pool should be rolled
     * (inclusive).
     * @param itemElements the elements in this loot pool
     */
    public LootPoolItem(@Nullable String name, int minRolls, int maxRolls, @NotNull Collection<@Nullable DragonLootElementItem> itemElements) {
        this(name, 100.0, minRolls, maxRolls, itemElements);
    }

    /**
     * Create an item loot pool.
     *
     * @param name this pool's name. Can be null
     * @param rolls the amount of times this loot pool should be rolled
     * @param itemElements the elements in this loot pool
     */
    public LootPoolItem(@Nullable String name, int rolls, @NotNull Collection<@Nullable DragonLootElementItem> itemElements) {
        this(name, 100.0, rolls, rolls, itemElements);
    }

    @NotNull
    @Override
    public JsonObject toJson() {
        return new JsonObject(); // TODO: Write to JSON
    }

    /**
     * Parse a {@link LootPoolItem} instance from a {@link JsonObject}.
     *
     * @param root the root element that represents this pool
     *
     * @return the created instance
     *
     * @throws JsonParseException if parsing the object has failed
     */
    @NotNull
    public static LootPoolItem fromJson(@NotNull JsonObject root) throws JsonParseException {
        Preconditions.checkArgument(root != null, "root must not be null");

        int minRolls = 0, maxRolls = 0;
        String name = root.has("name") ? root.get("name").getAsString() : null;
        double chance = root.has("chance") ? MathUtil.clamp(root.get("chance").getAsDouble(), 0.0, 100.0) : 100.0;
        List<DragonLootElementItem> itemElements = new ArrayList<>();

        if (root.has("rolls")) {
            JsonElement rolls = root.get("rolls");
            if (rolls.isJsonPrimitive()) {
                minRolls = maxRolls = Math.max(rolls.getAsInt(), 0);
            }

            else if (rolls.isJsonObject()) {
                JsonObject rollsObject = rolls.getAsJsonObject();
                minRolls = rollsObject.has("min") ? Math.max(rollsObject.get("min").getAsInt(), 0) : 0;
                maxRolls = rollsObject.has("max") ? Math.max(rollsObject.get("max").getAsInt(), 0) : minRolls;
            }
        }

        if (!root.has("items") || !root.get("items").isJsonArray()) {
            throw new JsonParseException("Missing \"items\" array for loot pool with name " + name);
        }

        JsonArray itemsArray = root.getAsJsonArray("items");
        for (JsonElement element : itemsArray) {
            if (!element.isJsonObject()) {
                throw new JsonParseException("Invalid item for loot pool with name " + name + ". Expected object. Got " + element.getClass().getSimpleName());
            }

            itemElements.add(DragonLootElementItem.fromJson(element.getAsJsonObject()));
        }

        return new LootPoolItem(name, chance, minRolls, maxRolls, itemElements);
    }

}
