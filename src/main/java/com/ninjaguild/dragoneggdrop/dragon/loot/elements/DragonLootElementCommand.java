package com.ninjaguild.dragoneggdrop.dragon.loot.elements;

import java.util.Random;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.ninjaguild.dragoneggdrop.placeholder.DragonEggDropPlaceholders;

import org.bukkit.Bukkit;
import org.bukkit.block.Chest;
import org.bukkit.boss.DragonBattle;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;

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
    public DragonLootElementCommand(String command, double weight) {
        this.command = command;
        this.weight = weight;
    }

    @Override
    public double getWeight() {
        return weight;
    }

    @Override
    public void generate(DragonBattle battle, EnderDragon dragon, Player killer, Random random, Chest chest) {
        if (command == null) {
            return;
        }

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), DragonEggDropPlaceholders.inject(null, command));
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
    public static DragonLootElementCommand fromJson(JsonObject root) {
        double weight = root.has("weight") ? Math.max(root.get("weight").getAsDouble(), 0.0) : 1.0;
        String command = root.has("command") ? root.get("command").getAsString() : null;

        return new DragonLootElementCommand(command, weight);
    }

}
