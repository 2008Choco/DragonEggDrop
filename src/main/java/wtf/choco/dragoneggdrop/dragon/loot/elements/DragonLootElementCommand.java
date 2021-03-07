package wtf.choco.dragoneggdrop.dragon.loot.elements;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.block.Chest;
import org.bukkit.boss.DragonBattle;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import wtf.choco.dragoneggdrop.dragon.DragonTemplate;
import wtf.choco.dragoneggdrop.placeholder.DragonEggDropPlaceholders;

/**
 * An implementation of {@link IDragonLootElement} to represent a command executable by
 * the console.
 *
 * @author Parker Hawke - Choco
 */
public class DragonLootElementCommand implements IDragonLootElement {

    private final String command;
    private final double weight;

    /**
     * Create a {@link DragonLootElementCommand}.
     *
     * @param command the command to execute. null if none
     * @param weight this element's weight in the loot pool
     */
    public DragonLootElementCommand(@Nullable String command, double weight) {
        Preconditions.checkArgument(command != null, "command must not be null");

        this.command = command;
        this.weight = weight;
    }

    @Override
    public double getWeight() {
        return weight;
    }

    @Override
    public void generate(@Nullable DragonBattle battle, @NotNull DragonTemplate template, @Nullable Player killer, @NotNull Random random, @Nullable Chest chest) {
        if (command == null) {
            return;
        }

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), DragonEggDropPlaceholders.inject(killer, command.replace("%dragon%", template.getName())));
    }

    /**
     * Parse a {@link DragonLootElementCommand} instance from a {@link JsonObject}.
     *
     * @param root the root element that represents this element
     *
     * @return the created instance
     *
     * @throws JsonParseException if parsing the object has failed
     */
    @NotNull
    public static DragonLootElementCommand fromJson(@NotNull JsonObject root) {
        Preconditions.checkArgument(root != null, "root must not be null");

        double weight = root.has("weight") ? Math.max(root.get("weight").getAsDouble(), 0.0) : 1.0;
        String command = root.has("command") ? root.get("command").getAsString() : null;

        return new DragonLootElementCommand(command, weight);
    }

}
