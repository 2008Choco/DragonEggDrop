package com.ninjaguild.dragoneggdrop.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.ninjaguild.dragoneggdrop.DragonEggDrop;
import com.ninjaguild.dragoneggdrop.dragon.DragonTemplate;
import com.ninjaguild.dragoneggdrop.dragon.loot.DragonLootTable;
import com.ninjaguild.dragoneggdrop.management.DEDManager;
import com.ninjaguild.dragoneggdrop.management.EndWorldWrapper;
import com.ninjaguild.dragoneggdrop.utils.math.MathUtils;

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
     *     start [time] [world] [template] - Start a respawn
     *     template
     *         set <template> [world] - Set the dragon to spawn while the countdown is active
     *         get [world] - Get the template to spawn
     */

    private final DragonEggDrop plugin;

    public DragonRespawnCmd(DragonEggDrop plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            this.plugin.sendMessage(sender, "Missing arguments... " + ChatColor.YELLOW + "/" + label + " <stop|start|template>");
            return true;
        }

        DEDManager manager = plugin.getDEDManager();
        if (args[0].equalsIgnoreCase("stop") || args[0].equalsIgnoreCase("interrupt") || args[0].equalsIgnoreCase("cancel")) {
            if (!sender.hasPermission("dragoneggdrop.respawn.stop")) {
                this.plugin.sendMessage(sender, ChatColor.RED + "You have insufficient privileges to execute this command");
                return true;
            }

            World world = getWorldFromContext(sender, args, 1);
            if (world == null) {
                return true;
            }

            EndWorldWrapper worldWrapper = manager.getWorldWrapper(world);
            if (!worldWrapper.isRespawnInProgress()) {
                this.plugin.sendMessage(sender, "No respawn is currently in progress");
                return true;
            }

            worldWrapper.stopRespawn();
            this.plugin.sendMessage(sender, "The respawn in world " + ChatColor.YELLOW + world.getName() + ChatColor.GRAY + " has been stopped");
        }

        else if (args[0].equalsIgnoreCase("start")) {
            if (!sender.hasPermission("dragoneggdrop.respawn.start")) {
                this.plugin.sendMessage(sender, ChatColor.RED + "You have insufficient privileges to execute this command");
                return true;
            }

            World world = getWorldFromContext(sender, args, 2);
            if (world == null) {
                return true;
            }

            EndWorldWrapper worldWrapper = manager.getWorldWrapper(world);
            if (worldWrapper.isRespawnInProgress()) {
                this.plugin.sendMessage(sender, "A respawn is already in progress. It must be stopped (" + ChatColor.YELLOW + "/" + label + " <stop>" + ChatColor.GRAY + ") before starting another");
                return true;
            }

            int respawnSeconds = (args.length >= 2) ? MathUtils.parseRespawnSeconds(args[1]) : 300; // Default 5 minutes
            DragonTemplate template = manager.getRandomTemplate();
            if (args.length >= 4) {
                DragonTemplate templateArgument = manager.getTemplate(args[3]);
                if (templateArgument == null) {
                    this.plugin.sendMessage(sender, "A template with the name " + ChatColor.YELLOW + args[2] + ChatColor.GRAY + " does not exist");
                    return true;
                }

                template = templateArgument;
            }

            DragonLootTable lootTable = null;
            if (args.length >= 5) {
                DragonLootTable lootTableArgument = plugin.getLootTableRegistry().getLootTable(args[4]);
                if (lootTableArgument == null) {
                    this.plugin.sendMessage(sender, "A loot table with the id " + ChatColor.YELLOW + args[4] + ChatColor.GRAY + " does not exist");
                    return true;
                }

                lootTable = lootTableArgument;
            }

            if (!worldWrapper.startRespawn(respawnSeconds, template, lootTable)) {
                this.plugin.sendMessage(sender, "A respawn could not be started... does a dragon already exist in this world?");
                return true;
            }

            this.plugin.sendMessage(sender, "A respawn has been started in world " + ChatColor.YELLOW + world.getName() + ChatColor.GRAY + " with template " + ChatColor.GREEN + template.getIdentifier()
                    + (worldWrapper.hasLootTableOverride() ? ChatColor.GRAY + " (loot table override: " + ChatColor.LIGHT_PURPLE + worldWrapper.getLootTableOverride().getId() + ChatColor.GRAY + ")" : ""));
        }

        else if (args[0].equalsIgnoreCase("template")) {
            if (!sender.hasPermission("dragoneggdrop.respawn.template")) {
                this.plugin.sendMessage(sender, ChatColor.RED + "You have insufficient privileges to execute this command");
                return true;
            }

            if (args.length < 2) {
                this.plugin.sendMessage(sender, "Missing arguments... " + ChatColor.YELLOW + "/" + label + " " + args[0] + "<set|get>");
                return true;
            }

            if (args[1].equalsIgnoreCase("set")) {
                World world = getWorldFromContext(sender, args, 3);
                if (world == null) {
                    return true;
                }

                EndWorldWrapper worldWrapper = manager.getWorldWrapper(world);
                if (!worldWrapper.isRespawnInProgress()) {
                    this.plugin.sendMessage(sender, "No respawn is currently in progress, cannot set the template to spawn");
                    return true;
                }

                if (args.length < 3) {
                    this.plugin.sendMessage(sender, "Please specify the template you would like to set for this world");
                    return true;
                }

                DragonTemplate template = manager.getTemplate(args[2]);
                if (template == null) {
                    this.plugin.sendMessage(sender, "A template with the name " + ChatColor.YELLOW + args[2] + ChatColor.GRAY + " does not exist");
                    return true;
                }

                worldWrapper.setActiveBattle(template);
                this.plugin.sendMessage(sender, "The dragon template " + ChatColor.YELLOW + template.getIdentifier() + ChatColor.GRAY + " will be spawned in the world " + ChatColor.GREEN + world.getName());
            }
            else if (args[1].equalsIgnoreCase("get")) {
                World world = getWorldFromContext(sender, args, 2);
                if (world == null) {
                    return true;
                }

                EndWorldWrapper worldWrapper = manager.getWorldWrapper(world);
                if (!worldWrapper.isRespawnInProgress()) {
                    this.plugin.sendMessage(sender, "No respawn is currently in progress, no template has yet been determined");
                    return true;
                }

                this.plugin.sendMessage(sender, "The template with ID " + ChatColor.YELLOW + worldWrapper.getActiveBattle().getIdentifier() + ChatColor.GRAY + " will be spawned in the world " + ChatColor.GREEN + world.getName());
            }
            else {
                this.plugin.sendMessage(sender, "Unknown argument " + ChatColor.YELLOW + args[1] + ChatColor.GRAY + ". Usage: " + ChatColor.YELLOW + "/" + label + " " + args[0] + " <set|get>");
            }
        }

        else {
            this.plugin.sendMessage(sender, "Unknown argument " + ChatColor.YELLOW + args[0] + ChatColor.GRAY + ". Usage: " + ChatColor.YELLOW + "/" + label + "<stop|start|template>");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], Arrays.asList("stop", "interrupt", "cancel", "start", "template"), new ArrayList<>());
        }

        else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("stop") || args[0].equalsIgnoreCase("interrupt") || args[0].equalsIgnoreCase("cancel")) {
                return StringUtil.copyPartialMatches(args[1], Bukkit.getWorlds().stream().filter(w -> w.getEnvironment() == Environment.THE_END)
                        .map(World::getName).collect(Collectors.toList()), new ArrayList<>());
            }

            else if (args[0].equalsIgnoreCase("start")) {
                return StringUtil.copyPartialMatches(args[1], Arrays.asList("30s", "5m", "1h", "7d", "2w", "2w7d1h5m30s"), new ArrayList<>());
            }

            else if (args[0].equalsIgnoreCase("template")) {
                return StringUtil.copyPartialMatches(args[1], Arrays.asList("set", "get"), new ArrayList<>());
            }
        }

        else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("start") || (args[0].equalsIgnoreCase("template") && args[1].equalsIgnoreCase("get"))) {
                return StringUtil.copyPartialMatches(args[2], Bukkit.getWorlds().stream().filter(w -> w.getEnvironment() == Environment.THE_END)
                        .map(World::getName).collect(Collectors.toList()), new ArrayList<>());
            }

            else if (args[0].equalsIgnoreCase("template") && args[1].equalsIgnoreCase("set")) {
                return StringUtil.copyPartialMatches(args[2], plugin.getDEDManager().getDragonTemplates().stream()
                        .map(DragonTemplate::getIdentifier).collect(Collectors.toList()), new ArrayList<>());
            }
        }

        else if (args.length == 4) {
            if (args[0].equalsIgnoreCase("start")) {
                return StringUtil.copyPartialMatches(args[3], plugin.getDEDManager().getDragonTemplates().stream()
                        .map(DragonTemplate::getIdentifier).collect(Collectors.toList()), new ArrayList<>());
            }
            else if (args[0].equalsIgnoreCase("template") && args[1].equalsIgnoreCase("set")) {
                return StringUtil.copyPartialMatches(args[3], Bukkit.getWorlds().stream().filter(w -> w.getEnvironment() == Environment.THE_END)
                        .map(World::getName).collect(Collectors.toList()), new ArrayList<>());
            }
        }

        else if (args.length == 5) {
            if (args[0].equalsIgnoreCase("start")) {
                return StringUtil.copyPartialMatches(args[4], plugin.getLootTableRegistry().getLootTables().stream()
                        .map(DragonLootTable::getId).collect(Collectors.toList()), new ArrayList<>());
            }
        }

        return null;
    }

    private World getWorldFromContext(CommandSender sender, String[] args, int argumentIndex) {
        if (args.length >= (argumentIndex + 1)) {
            World world = Bukkit.getWorld(args[argumentIndex]);
            if (world == null) {
                this.plugin.sendMessage(sender, "Could not find a world with the name " + ChatColor.YELLOW + args[argumentIndex]);
                return null;
            }

            if (world.getEnvironment() != Environment.THE_END) {
                this.plugin.sendMessage(sender, "The specified world (" + args[argumentIndex] + ") is not an end world");
                return null;
            }

            return Bukkit.getWorld(args[argumentIndex]);
        }

        if (!(sender instanceof Player)) {
            this.plugin.sendMessage(sender, "A world name must be specified when sending this command from the console");
            return null;
        }

        World world = ((Player) sender).getWorld();
        if (world.getEnvironment() != Environment.THE_END) {
            this.plugin.sendMessage(sender, "The world in which you are executing this command is not an end world... please specify a world instead");
            return null;
        }

        return world;
    }

}
