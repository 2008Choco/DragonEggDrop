package com.ninjaguild.dragoneggdrop.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.ninjaguild.dragoneggdrop.DragonEggDrop;
import com.ninjaguild.dragoneggdrop.dragon.DragonTemplate;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.util.StringUtil;

public final class DragonTemplateCmd implements TabExecutor {

    // /template <list|"template"> <(view/info)|edit>

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            DragonEggDrop.sendMessage(sender, "Please specify the name of a template, or \"list\" to list all templates");
            return true;
        }

        // List all existing templates
        if (args[0].equalsIgnoreCase("list")) {
            if (!sender.hasPermission("dragoneggdrop.template.list")) {
                DragonEggDrop.sendMessage(sender, ChatColor.RED + "You have insufficient privileges to execute this command");
                return true;
            }

            String[] templateNames = DragonTemplate.getAll().stream().map(t -> ChatColor.GREEN + t.getId()).toArray(String[]::new);

            DragonEggDrop.sendMessage(sender, ChatColor.GRAY + "Active Templates:\n" + String.join(ChatColor.GRAY + ", ", templateNames));
            return true;
        }

        // Template was identified
        DragonTemplate template = DragonTemplate.getById(args[0]);

        // No template found
        if (template == null) {
            DragonEggDrop.sendMessage(sender, "Could not find a template with the name \"" + args[0] + "\"");
            return true;
        }

        if (args.length < 2) {
            DragonEggDrop.sendMessage(sender, cmd.getLabel() + " " + args[0] + " <(view/info)|edit>");
            return true;
        }

        // "view/info" and "edit" params
        if (args[1].equalsIgnoreCase("view") || args[1].equalsIgnoreCase("info")) {
            if (!sender.hasPermission("dragoneggdrop.template.info")) {
                DragonEggDrop.sendMessage(sender, ChatColor.RED + "You have insufficient privileges to execute this command");
                return true;
            }

            sender.sendMessage(ChatColor.GRAY + "Dragon Name: " + ChatColor.GREEN + template.getName());
            sender.sendMessage(ChatColor.GRAY + "Bar Style: " + ChatColor.GREEN + template.getBarStyle());
            sender.sendMessage(ChatColor.GRAY + "Bar Color: " + ChatColor.GREEN + template.getBarColor());
            sender.sendMessage(ChatColor.GRAY + "Spawn Weight: " + ChatColor.GREEN + template.getSpawnWeight());
            sender.sendMessage(ChatColor.GRAY + "Announce Spawn: " + (template.shouldAnnounceSpawn() ? ChatColor.GREEN : ChatColor.RED) + template.shouldAnnounceSpawn());
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
        else if (args.length == 2) {
            StringUtil.copyPartialMatches(args[1], Arrays.asList("view"), options);
        }

        return options;
    }

}
