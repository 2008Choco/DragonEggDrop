package com.ninjaguild.dragoneggdrop.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.ninjaguild.dragoneggdrop.DragonEggDrop;
import com.ninjaguild.dragoneggdrop.dragon.DragonTemplate;
import com.ninjaguild.dragoneggdrop.dragon.loot.DragonLootTable;
import com.ninjaguild.dragoneggdrop.utils.DEDConstants;
import com.ninjaguild.dragoneggdrop.utils.math.MathUtils;
import com.ninjaguild.dragoneggdrop.world.EndWorldWrapper;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

public final class DragonRespawnCmd implements TabExecutor {

    /* /dragonrespawn
     *     <stop, interrupt, cancel> [world] - Stop any active respawn countdown
     *     start [time] [world] [template] [loot_table] - Start a respawn
     *     template - Get the template to spawn in the currnet world
     *         set <template> [world] - Set the dragon to spawn while the countdown is active
     *         [world] - Get the template to spawn in the specified world
     */

    private final DragonEggDrop plugin;

    public DragonRespawnCmd(DragonEggDrop plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            DragonEggDrop.sendMessage(sender, "Missing arguments... " + ChatColor.YELLOW + "/" + label + " <stop|start|template>");
            return true;
        }

        if (args[0].equalsIgnoreCase("stop") || args[0].equalsIgnoreCase("interrupt") || args[0].equalsIgnoreCase("cancel")) {
            if (!sender.hasPermission(DEDConstants.PERMISSION_COMMAND_RESPAWN_STOP)) {
                DragonEggDrop.sendMessage(sender, ChatColor.RED + "You have insufficient privileges to execute this command");
                return true;
            }

            World world = getWorldFromContext(sender, args, 1);
            if (world == null) {
                return true;
            }

            EndWorldWrapper worldWrapper = EndWorldWrapper.of(world);
            if (!worldWrapper.isRespawnInProgress()) {
                DragonEggDrop.sendMessage(sender, "No respawn is currently in progress");
                return true;
            }

            worldWrapper.stopRespawn();
            DragonEggDrop.sendMessage(sender, "The respawn in world " + ChatColor.YELLOW + world.getName() + ChatColor.GRAY + " has been stopped");
        }

        else if (args[0].equalsIgnoreCase("start")) {
            if (!sender.hasPermission(DEDConstants.PERMISSION_COMMAND_RESPAWN_START)) {
                DragonEggDrop.sendMessage(sender, ChatColor.RED + "You have insufficient privileges to execute this command");
                return true;
            }

            World world = getWorldFromContext(sender, args, 2);
            if (world == null) {
                return true;
            }

            EndWorldWrapper worldWrapper = EndWorldWrapper.of(world);
            if (worldWrapper.isRespawnInProgress()) {
                DragonEggDrop.sendMessage(sender, "A respawn is already in progress. It must be stopped (" + ChatColor.YELLOW + "/" + label + " <stop>" + ChatColor.GRAY + ") before starting another");
                return true;
            }

            int respawnSeconds = (args.length >= 2) ? MathUtils.parseRespawnSeconds(args[1]) : 300; // Default 5 minutes
            DragonTemplate template = plugin.getDragonTemplateRegistry().getRandomTemplate();
            if (args.length >= 4) {
                DragonTemplate templateArgument = plugin.getDragonTemplateRegistry().get(args[3]);
                if (templateArgument == null) {
                    DragonEggDrop.sendMessage(sender, "A template with the name " + ChatColor.YELLOW + args[3] + ChatColor.GRAY + " does not exist");
                    return true;
                }

                template = templateArgument;
            }

            DragonLootTable lootTable = null;
            if (args.length >= 5) {
                DragonLootTable lootTableArgument = plugin.getLootTableRegistry().get(args[4]);
                if (lootTableArgument == null) {
                    DragonEggDrop.sendMessage(sender, "A loot table with the id " + ChatColor.YELLOW + args[4] + ChatColor.GRAY + " does not exist");
                    return true;
                }

                lootTable = lootTableArgument;
            }

            if (!worldWrapper.startRespawn(respawnSeconds, template, lootTable)) {
                DragonEggDrop.sendMessage(sender, "A respawn could not be started... does a dragon already exist in this world?");
                return true;
            }

            DragonEggDrop.sendMessage(sender, "A respawn has been started in world " + ChatColor.YELLOW + world.getName() + ChatColor.GRAY + " with template " + ChatColor.GREEN + template.getId()
                    + (worldWrapper.hasLootTableOverride() ? ChatColor.GRAY + " (loot table override: " + ChatColor.LIGHT_PURPLE + worldWrapper.getLootTableOverride().getId() + ChatColor.GRAY + ")" : ""));
        }

        else if (args[0].equalsIgnoreCase("template")) {
            if (!sender.hasPermission(DEDConstants.PERMISSION_COMMAND_RESPAWN_TEMPLATE)) {
                DragonEggDrop.sendMessage(sender, ChatColor.RED + "You have insufficient privileges to execute this command");
                return true;
            }

            // /dragonrespawn template set <template> [world]
            if (args.length >= 2 && args[1].equalsIgnoreCase("set")) {
                World world = getWorldFromContext(sender, args, 3);
                if (world == null) {
                    return true;
                }

                EndWorldWrapper worldWrapper = EndWorldWrapper.of(world);
                if (!worldWrapper.isRespawnInProgress()) {
                    DragonEggDrop.sendMessage(sender, "No respawn is currently in progress, cannot set the template to spawn");
                    return true;
                }

                if (args.length < 3) {
                    DragonEggDrop.sendMessage(sender, "Please specify the template you would like to set for this world");
                    return true;
                }

                DragonTemplate template = plugin.getDragonTemplateRegistry().get(args[2]);
                if (template == null) {
                    DragonEggDrop.sendMessage(sender, "A template with the name " + ChatColor.YELLOW + args[2] + ChatColor.GRAY + " does not exist");
                    return true;
                }

                worldWrapper.setRespawningTemplate(template);
                DragonEggDrop.sendMessage(sender, "The dragon template " + ChatColor.YELLOW + template.getId() + ChatColor.GRAY + " will be spawned in the world " + ChatColor.GREEN + world.getName());
                return true;
            }

            // /dragonrespawn template [world]
            World world = getWorldFromContext(sender, args, 1);
            if (world == null) {
                return true;
            }

            EndWorldWrapper worldWrapper = EndWorldWrapper.of(world);
            DragonTemplate template = worldWrapper.getRespawningTemplate();
            if (template == null) {
                DragonEggDrop.sendMessage(sender, "No respawn is currently in progress, no template has yet been determined");
                return true;
            }

            DragonEggDrop.sendMessage(sender, "The template with ID " + ChatColor.YELLOW + template.getId() + ChatColor.GRAY + " will be spawned in the world " + ChatColor.GREEN + world.getName());
            return true;
        }

        else {
            DragonEggDrop.sendMessage(sender, "Unknown argument " + ChatColor.YELLOW + args[0] + ChatColor.GRAY + ". Usage: " + ChatColor.YELLOW + "/" + label + " <stop|start|template>");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> options = new ArrayList<>();

        if (args.length == 1) {
            StringUtil.copyPartialMatches(args[0], Arrays.asList("stop", "interrupt", "cancel", "start", "template"), options);
        }

        else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("stop") || args[0].equalsIgnoreCase("interrupt") || args[0].equalsIgnoreCase("cancel")) {
                StringUtil.copyPartialMatches(args[1], Bukkit.getWorlds().stream().filter(w -> w.getEnvironment() == Environment.THE_END)
                        .map(World::getName).collect(Collectors.toList()), options);
            }

            else if (args[0].equalsIgnoreCase("start")) {
                StringUtil.copyPartialMatches(args[1], Arrays.asList("30s", "5m", "1h", "7d", "2w", "2w7d1h5m30s"), options);
            }

            else if (args[0].equalsIgnoreCase("template")) {
                List<String> possibleOptions = new ArrayList<>();
                possibleOptions.add("set");
                Bukkit.getWorlds().forEach(world -> {
                    if (world.getEnvironment() == Environment.THE_END) {
                        possibleOptions.add(world.getName());
                    }
                });

                StringUtil.copyPartialMatches(args[1], possibleOptions, options);
            }
        }

        else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("start")) {
                StringUtil.copyPartialMatches(args[2], Bukkit.getWorlds().stream().filter(w -> w.getEnvironment() == Environment.THE_END)
                        .map(World::getName).collect(Collectors.toList()), options);
            }

            else if (args[0].equalsIgnoreCase("template") && args[1].equalsIgnoreCase("set")) {
                StringUtil.copyPartialMatches(args[2], new ArrayList<>(plugin.getDragonTemplateRegistry().keys()), options);
            }
        }

        else if (args.length == 4) {
            if (args[0].equalsIgnoreCase("start")) {
                StringUtil.copyPartialMatches(args[3], new ArrayList<>(plugin.getDragonTemplateRegistry().keys()), options);
            }
            else if (args[0].equalsIgnoreCase("template") && args[1].equalsIgnoreCase("set")) {
                StringUtil.copyPartialMatches(args[3], Bukkit.getWorlds().stream().filter(w -> w.getEnvironment() == Environment.THE_END)
                        .map(World::getName).collect(Collectors.toList()), options);
            }
        }

        else if (args.length == 5) {
            if (args[0].equalsIgnoreCase("start")) {
                StringUtil.copyPartialMatches(args[4], plugin.getLootTableRegistry().values().stream()
                        .map(DragonLootTable::getId).collect(Collectors.toList()), options);
            }
        }

        return options;
    }

    private World getWorldFromContext(CommandSender sender, String[] args, int argumentIndex) {
        if (args.length >= (argumentIndex + 1)) {
            World world = Bukkit.getWorld(args[argumentIndex]);
            if (world == null) {
                DragonEggDrop.sendMessage(sender, "Could not find a world with the name " + ChatColor.YELLOW + args[argumentIndex]);
                return null;
            }

            if (world.getEnvironment() != Environment.THE_END) {
                DragonEggDrop.sendMessage(sender, "The specified world (" + ChatColor.YELLOW + args[argumentIndex] + ChatColor.GRAY + ") is not an end world");
                return null;
            }

            return Bukkit.getWorld(args[argumentIndex]);
        }

        if (!(sender instanceof Player)) {
            DragonEggDrop.sendMessage(sender, "A world name must be specified when sending this command from the console");
            return null;
        }

        World world = ((Player) sender).getWorld();
        if (world.getEnvironment() != Environment.THE_END) {
            DragonEggDrop.sendMessage(sender, "The world in which you are executing this command is not an end world... please specify a world instead");
            return null;
        }

        return world;
    }

}
