package com.ninjaguild.dragoneggdrop.dragon.loot.pool;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.ninjaguild.dragoneggdrop.dragon.loot.elements.DragonLootElementCommand;
import com.ninjaguild.dragoneggdrop.utils.math.MathUtils;

public class LootPoolCommand extends AbstractLootPool<DragonLootElementCommand> {

    public LootPoolCommand(String name, double chance, int minRolls, int maxRolls, Collection<DragonLootElementCommand> commandElements) {
        super(name, chance, minRolls, maxRolls, commandElements);
    }

    public LootPoolCommand(String name, double chance, int rolls, Collection<DragonLootElementCommand> commandElements) {
        this(name, chance, rolls, rolls, commandElements);
    }

    public LootPoolCommand(String name, int minRolls, int maxRolls, Collection<DragonLootElementCommand> commandElements) {
        this(name, 100.0, minRolls, maxRolls, commandElements);
    }

    public LootPoolCommand(String name, int rolls, Collection<DragonLootElementCommand> commandElements) {
        this(name, 100.0, rolls, commandElements);
    }

    @Override
    public JsonObject toJson() {
        return new JsonObject(); // TODO: Write to JSON
    }

    public static LootPoolCommand fromJson(JsonObject root) {
        int minRolls = 0, maxRolls = 0;
        String name = root.has("name") ? root.get("name").getAsString() : null;
        double chance = root.has("chance") ? MathUtils.clamp(root.get("chance").getAsDouble(), 0.0, 100.0) : 100.0;
        List<DragonLootElementCommand> commandElements = new ArrayList<>();

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

        if (!root.has("commands") || !root.get("commands").isJsonArray()) {
            throw new JsonParseException("Missing \"commands\" array for loot pool with name " + name);
        }

        JsonArray commandsArray = root.getAsJsonArray("commands");
        for (JsonElement element : commandsArray) {
            if (!element.isJsonObject()) {
                throw new JsonParseException("Invalid command for loot pool with name " + name + ". Expected object. Got " + element.getClass().getSimpleName());
            }

            commandElements.add(DragonLootElementCommand.fromJson(element.getAsJsonObject()));
        }

        return new LootPoolCommand(name, chance, minRolls, maxRolls, commandElements);
    }

}
