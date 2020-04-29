package com.ninjaguild.dragoneggdrop.commands;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.ninjaguild.dragoneggdrop.DragonEggDrop;
import com.ninjaguild.dragoneggdrop.dragon.DragonTemplate;

import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.util.StringUtil;

public final class DragonTemplateCmd implements TabExecutor {

    private static final NumberFormat DECIMAL_FORMAT = new DecimalFormat("#.##");
    private static final Map<BarColor, ChatColor> BAR_COLOURS = new EnumMap<>(BarColor.class);
    static {
        BAR_COLOURS.put(BarColor.BLUE, ChatColor.BLUE);
        BAR_COLOURS.put(BarColor.GREEN, ChatColor.GREEN);
        BAR_COLOURS.put(BarColor.PINK, ChatColor.LIGHT_PURPLE);
        BAR_COLOURS.put(BarColor.PURPLE, ChatColor.DARK_PURPLE);
        BAR_COLOURS.put(BarColor.RED, ChatColor.RED);
        BAR_COLOURS.put(BarColor.WHITE, ChatColor.WHITE);
        BAR_COLOURS.put(BarColor.YELLOW, ChatColor.YELLOW);
    }

    // /template <list|"template"> <(view/info)|edit>

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            if (label.endsWith("s")) {
                this.listTemplates(sender);
                return true;
            }

            DragonEggDrop.sendMessage(sender, "Please specify the name of a template, or \"list\" to list all templates");
            return true;
        }

        // List all existing templates
        if (args[0].equalsIgnoreCase("list")) {
            this.listTemplates(sender);
            return true;
        }

        // Template was identified
        DragonTemplate template = DragonTemplate.getById(args[0]);

        // No template found
        if (template == null) {
            DragonEggDrop.sendMessage(sender, "Could not find a template with the name \"" + ChatColor.YELLOW + args[0] + ChatColor.GRAY + "\"");
            return true;
        }

        if (args.length < 2) {
            DragonEggDrop.sendMessage(sender, "Missing arguments... " + ChatColor.YELLOW + "/" + label + " " + args[0] + " <view|info>");
            return true;
        }

        // "view/info" and "edit" params
        if (args[1].equalsIgnoreCase("view") || args[1].equalsIgnoreCase("info")) {
            if (!sender.hasPermission("dragoneggdrop.template.info")) {
                DragonEggDrop.sendMessage(sender, ChatColor.RED + "You have insufficient privileges to execute this command");
                return true;
            }

            double totalWeight = DragonTemplate.getAll().stream().mapToDouble(DragonTemplate::getSpawnWeight).sum();
            double chanceToSpawn = (template.getSpawnWeight() / totalWeight) * 100;

            sender.sendMessage(ChatColor.GRAY + "Dragon Name: " + ChatColor.GREEN + template.getName());
            sender.sendMessage(ChatColor.GRAY + "Bar Style: " + ChatColor.GREEN + template.getBarStyle());
            sender.sendMessage(ChatColor.GRAY + "Bar Color: " + BAR_COLOURS.get(template.getBarColor()) + template.getBarColor());
            sender.sendMessage(ChatColor.GRAY + "Spawn Weight: " + ChatColor.DARK_GREEN + template.getSpawnWeight() + ChatColor.GREEN + " (out of " + ChatColor.DARK_GREEN + totalWeight + ChatColor.GREEN + " - " + ChatColor.DARK_GREEN + DECIMAL_FORMAT.format(chanceToSpawn) + "% " + ChatColor.GREEN + "chance to spawn)");
            sender.sendMessage(ChatColor.GRAY + "Announce Spawn: " + (template.shouldAnnounceSpawn() ? ChatColor.GREEN : ChatColor.RED) + template.shouldAnnounceSpawn());
            sender.sendMessage(ChatColor.GRAY + "Loot table: " + ChatColor.YELLOW + (template.getLootTable() != null ? template.getLootTable().getId() : "N/A"));
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> options = new ArrayList<>();

        // Before completion: "/dragontemplate "
        if (args.length == 1) {
            List<String> possibleOptions = DragonTemplate.getAll().stream().map(DragonTemplate::getId).collect(Collectors.toList());
            possibleOptions.add(0, "list");
            StringUtil.copyPartialMatches(args[0], possibleOptions, options);
        }

        // Before completion: "/dragontemplate <template> "
        else if (args.length == 2 && !args[0].equals("list")) {
            StringUtil.copyPartialMatches(args[1], Arrays.asList("view"), options);
        }

        return options;
    }

    private void listTemplates(CommandSender sender) {
        if (!sender.hasPermission("dragoneggdrop.template.list")) {
            DragonEggDrop.sendMessage(sender, ChatColor.RED + "You have insufficient privileges to execute this command");
            return;
        }

        String templates = DragonTemplate.getAll().stream().map(t -> ChatColor.GREEN + t.getId()).collect(Collectors.joining(ChatColor.GRAY + ", "));
        DragonEggDrop.sendMessage(sender, ChatColor.GRAY + "Loaded Templates:");
        sender.sendMessage(templates);
        return;
    }

}
