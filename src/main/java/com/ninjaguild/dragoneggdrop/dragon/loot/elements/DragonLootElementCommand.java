package com.ninjaguild.dragoneggdrop.dragon.loot.elements;

import java.util.Random;

import com.google.gson.JsonObject;
import com.ninjaguild.dragoneggdrop.nms.DragonBattle;

import org.bukkit.Bukkit;
import org.bukkit.block.Chest;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;

public class DragonLootElementCommand implements IDragonLootElement {

    private final String command;
    private final double weight;

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
        if (command == null || (command.contains("%player%") && killer == null)) {
            return;
        }

        String contextualCommand = command.replace("%dragon%", dragon.getCustomName());
        if (killer != null) {
            contextualCommand = contextualCommand.replace("%player%", killer.getName());
        }

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), contextualCommand);
    }

    public static DragonLootElementCommand fromJson(JsonObject root) {
        double weight = root.has("weight") ? Math.max(root.get("weight").getAsDouble(), 0.0) : 1.0;
        String command = root.has("command") ? root.get("command").getAsString() : null;

        return new DragonLootElementCommand(command, weight);
    }

}
